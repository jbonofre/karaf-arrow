package net.nanthrax.karaf.arrow;

import org.apache.arrow.flight.FlightDescriptor;
import org.apache.arrow.flight.Location;
import org.apache.arrow.flight.NoOpFlightProducer;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.util.AutoCloseables;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConfigProducer extends NoOpFlightProducer implements AutoCloseable {

    private final BufferAllocator allocator;
    private final Location location;
    private final ConcurrentMap<FlightDescriptor, ConfigDataset> datasets;

    public ConfigProducer(BufferAllocator allocator, Location location) {
        this.allocator = allocator;
        this.location = location;
        this.datasets = new ConcurrentHashMap<>();
    }

    

    @Override
    public void close() throws Exception {
        AutoCloseables.close(datasets.values());
    }
}
