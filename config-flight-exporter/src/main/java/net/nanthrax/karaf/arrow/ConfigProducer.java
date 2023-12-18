package net.nanthrax.karaf.arrow;

import org.apache.arrow.flight.CallStatus;
import org.apache.arrow.flight.FlightDescriptor;
import org.apache.arrow.flight.Location;
import org.apache.arrow.flight.NoOpFlightProducer;
import org.apache.arrow.flight.Ticket;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.util.AutoCloseables;
import org.apache.arrow.vector.VectorLoader;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.VectorUnloader;
import org.apache.arrow.vector.ipc.message.ArrowRecordBatch;
import org.apache.arrow.vector.table.Row;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConfigProducer extends NoOpFlightProducer implements AutoCloseable {

    private final BufferAllocator allocator;
    private final ConcurrentMap<FlightDescriptor, ConfigDataset> datasets;

    public ConfigProducer(BufferAllocator allocator) {
        this.allocator = allocator;
        this.datasets = new ConcurrentHashMap<>();
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
            FlightDescriptor flightDescriptor = FlightDescriptor.command(configuration.getPid().getBytes(StandardCharsets.UTF_8));
            ConfigDataset dataset = new ConfigDataset(configuration, allocator);
            datasets.put(flightDescriptor, dataset);
        }
    }

    @Override
    public void getStream(CallContext callContext, Ticket ticket, ServerStreamListener listener) {
        FlightDescriptor flightDescriptor = FlightDescriptor.command(ticket.getBytes());
        ConfigDataset dataset = this.datasets.get(flightDescriptor);
        if (dataset == null) {
            throw CallStatus.NOT_FOUND.withDescription("PID not found").toRuntimeException();
        }
        VectorUnloader unloader = new VectorUnloader(dataset.getTable().toVectorSchemaRoot());
        try (VectorSchemaRoot root = VectorSchemaRoot.create(this.datasets.get(flightDescriptor).getSchema(), allocator)) {
            VectorLoader loader = new VectorLoader(root);
            listener.start(root);
            ArrowRecordBatch record = unloader.getRecordBatch();
            loader.load(record);
            listener.putNext();
            listener.completed();
        }
    }

    public ConfigDataset getConfigDataset(String pid) {
        FlightDescriptor flightDescriptor = FlightDescriptor.command(pid.getBytes());
        return datasets.get(flightDescriptor);
    }

    @Override
    public void close() throws Exception {
        AutoCloseables.close(datasets.values());
    }
}
