package net.nanthrax.karaf.arrow;

import org.apache.arrow.flight.CallStatus;
import org.apache.arrow.flight.Criteria;
import org.apache.arrow.flight.FlightDescriptor;
import org.apache.arrow.flight.FlightEndpoint;
import org.apache.arrow.flight.FlightInfo;
import org.apache.arrow.flight.Location;
import org.apache.arrow.flight.NoOpFlightProducer;
import org.apache.arrow.flight.Ticket;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.util.AutoCloseables;
import org.apache.arrow.vector.VarCharVector;
import org.apache.arrow.vector.VectorLoader;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.VectorUnloader;
import org.apache.arrow.vector.ipc.message.ArrowRecordBatch;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.FieldType;
import org.apache.arrow.vector.types.pojo.Schema;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConfigProducer extends NoOpFlightProducer implements AutoCloseable {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConfigProducer.class);

    private final BufferAllocator allocator;
    private final Location location;
    private final ConcurrentMap<FlightDescriptor, ConfigDataset> datasets;

    public ConfigProducer(BufferAllocator allocator, Location location) {
        this.allocator = allocator;
        this.datasets = new ConcurrentHashMap<>();
        this.location = location;
    }

    public void init(BundleContext bundleContext) throws IllegalStateException, IOException, InvalidSyntaxException {
        ServiceReference<ConfigurationAdmin> serviceReference = bundleContext.getServiceReference(ConfigurationAdmin.class);
        if (serviceReference == null) {
            throw new IllegalStateException("ConfigurationAdmin service is not available");
        }
        ConfigurationAdmin configurationAdmin = bundleContext.getService(serviceReference);
        if (configurationAdmin == null) {
            throw new IllegalStateException("ConfigurationAdmin service is not available");
        }
        init(configurationAdmin);
    }

    public void init(ConfigurationAdmin configurationAdmin) throws IllegalStateException, IOException, InvalidSyntaxException {
        Configuration[] configurations = configurationAdmin.listConfigurations(null);
        for (Configuration configuration : configurations) {
            FlightDescriptor flightDescriptor = FlightDescriptor.path(configuration.getPid());
            LOGGER.info("Processing configuration {}", flightDescriptor.getPath());
            ConfigDataset dataset = new ConfigDataset(configuration, allocator);
            datasets.put(flightDescriptor, dataset);
        }
    }

    @Override
    public void getStream(CallContext callContext, Ticket ticket, ServerStreamListener listener) {
        String pid = new String(ticket.getBytes(), StandardCharsets.UTF_8);
        LOGGER.info("Get stream for PID {}", pid);
        FlightDescriptor flightDescriptor = FlightDescriptor.path(pid);
        ConfigDataset dataset = this.datasets.get(flightDescriptor);
        if (dataset == null) {
            throw CallStatus.NOT_FOUND.withDescription("PID not found").toRuntimeException();
        }
        listener.start(dataset.getRoot());
        LOGGER.info("   Row count: {}", dataset.getRoot().getRowCount());
        listener.putNext();
        listener.completed();
    }

    @Override
    public FlightInfo getFlightInfo(CallContext context, FlightDescriptor descriptor) {
        FlightEndpoint flightEndpoint = new FlightEndpoint(new Ticket(descriptor.getPath().get(0).getBytes(StandardCharsets.UTF_8)), location);
        return new FlightInfo(
                datasets.get(descriptor).getSchema(),
                descriptor,
                Collections.singletonList(flightEndpoint),
                -1,
                datasets.get(descriptor).getRows()
        );
    }

    @Override
    public void listFlights(CallContext context, Criteria criteria, StreamListener<FlightInfo> listener) {
        datasets.forEach((k, v) -> { listener.onNext(getFlightInfo(null, k));});
        listener.onCompleted();
    }

    public ConfigDataset getConfigDataset(String pid) {
        FlightDescriptor flightDescriptor = FlightDescriptor.path(pid);
        return datasets.get(flightDescriptor);
    }

    @Override
    public void close() throws Exception {
        AutoCloseables.close(datasets.values());
    }
}
