package net.nanthrax.karaf.arrow.client;

import org.apache.arrow.flight.Criteria;
import org.apache.arrow.flight.FlightClient;
import org.apache.arrow.flight.FlightDescriptor;
import org.apache.arrow.flight.FlightInfo;
import org.apache.arrow.flight.FlightStream;
import org.apache.arrow.flight.Location;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.VectorSchemaRoot;

import java.nio.charset.StandardCharsets;

public class ConfigFlightClient {

    public final static void main(String... args) throws Exception {
        BufferAllocator allocator = new RootAllocator();
        Location location = Location.forGrpcInsecure("localhost", 33333);
        try (FlightClient flightClient = FlightClient.builder(allocator, location).build()) {
            System.out.println("Client connected to " + location.getUri());
            FlightInfo flightInfo = flightClient.getInfo(FlightDescriptor.path("org.apache.karaf.shell"));
            System.out.println("Flight info: " + flightInfo);
            try (FlightStream flightStream = flightClient.getStream(flightInfo.getEndpoints().get(0).getTicket())) {
                try (VectorSchemaRoot vectorSchemaRoot = flightStream.getRoot()) {
                    System.out.println(vectorSchemaRoot.contentToTSVString());
                }
            }
        }
    }

}
