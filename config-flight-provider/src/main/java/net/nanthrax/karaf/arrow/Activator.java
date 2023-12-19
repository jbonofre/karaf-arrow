package net.nanthrax.karaf.arrow;

import org.apache.arrow.flight.FlightServer;
import org.apache.arrow.flight.Location;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private final static Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private BufferAllocator allocator;
    private ConfigProducer configProducer;
    private FlightServer flightServer;

    @Override
    public void start(BundleContext context) throws Exception {
        allocator = new RootAllocator();
        Location location = Location.forGrpcInsecure("0.0.0.0", 33333);
        configProducer = new ConfigProducer(allocator, location);
        configProducer.init(context);
        flightServer = FlightServer.builder(allocator, location, configProducer).build();
        flightServer.start();
        LOGGER.info("Starting Arrow Flight Server on port {}", flightServer.getPort());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        LOGGER.info("Stopping Arrow Flight Server");
        flightServer.shutdown();
        configProducer.close();
        allocator.close();
    }
}
