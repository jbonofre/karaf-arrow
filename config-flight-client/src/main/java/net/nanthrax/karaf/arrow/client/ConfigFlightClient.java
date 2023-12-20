package net.nanthrax.karaf.arrow.client;

import org.apache.arrow.flight.FlightClient;
import org.apache.arrow.flight.FlightDescriptor;
import org.apache.arrow.flight.FlightInfo;
import org.apache.arrow.flight.FlightStream;
import org.apache.arrow.flight.Location;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.VectorSchemaRoot;

public class ConfigFlightClient {

    public final static void main(String[] args) throws Exception {
        String pid = "org.apache.karaf.shell";
        if (args.length > 1) {
            pid = args[1];
        }
        BufferAllocator allocator = new RootAllocator();
        Location location = Location.forGrpcInsecure("localhost", 33333);
        try (FlightClient flightClient = FlightClient.builder(allocator, location).build()) {
            System.out.println("Client connected to " + location.getUri());
            FlightInfo flightInfo = flightClient.getInfo(FlightDescriptor.path(pid));
            System.out.println("Flight info: " + flightInfo);
            try (FlightStream flightStream = flightClient.getStream(flightInfo.getEndpoints().get(0).getTicket())) {
                try (VectorSchemaRoot vectorSchemaRoot = flightStream.getRoot()) {
                    while (flightStream.next()) {
                        System.out.println("Row count: " + vectorSchemaRoot.getRowCount());
                        System.out.println(vectorSchemaRoot.contentToTSVString());
                    }
                }
            }
        }
    }

}
