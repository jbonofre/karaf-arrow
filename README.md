# Apache Karaf & Apache Arrow

This simple project shows how to use Apache Arrow and Apache Arrow Flight in Apache Karaf runtime.

It exposes the Karaf ConfigurationAdmin service with Arrow Flight and provide a simple Arrow Flight client to retrieve configurations.

The `config-flight-provider` module is an OSGi bundle you can deploy in Apache Karaf:
* `ConfigDataset` represents a `Configuration` as Arrow schema and vectors.
* `ConfigProducer` is Arrow `FlightProducer` exposing the Flight streams for each `Configuration` (the `Configuration PID` is the Arrow Flight ticket).
* the Bundle `Activator` is loading the `ConfigProducer` and start the Arrow Flight server.

For convenience, `features` module provide a Karaf feature repository to easily install the provider.

The `config-flight-client` is a simple jar with a Arrow Flight client retrieving a `Configuration` from Apache Karaf runtime.

# Prerequisites

With Java 9+, for Apache Arrow, we need to allow all modules to open `java.nio` package. It means we need to add the following JVM argument:

```
--add-opens=java.base/java.nio=ALL-UNNAMED
```

We need to add this in `bin/karaf` or `bin/setenv`.

# Installation

We can start Karaf with `bin/karaf`, then you register the `karaf-arrow` features repository:

```
karaf@root()> feature:repo-add mvn:net.nanthrax.karaf.arrow/karaf-arrow/1.0-SNAPSHOT/xml/features
Adding feature url mvn:net.nanthrax.karaf.arrow/karaf-arrow/1.0-SNAPSHOT/xml/features
```

Then, we can install `karaf-arrow-server` feature:

```
karaf@root()> feature:install karaf-arrow-server
```

You can see the Arrow Flight server started in Karaf:

```
...
16:51:07.306 INFO [FelixStartLevel] Processing configuration [org.apache.karaf.command.acl.shell]
16:51:07.306 INFO [FelixStartLevel] Processing configuration [org.ops4j.pax.url.mvn]
16:51:07.306 INFO [FelixStartLevel] Processing configuration [jmx.acl.org.apache.karaf.bundle]
16:51:07.307 INFO [FelixStartLevel] Processing configuration [org.apache.karaf.command.acl.system]
16:51:07.307 INFO [FelixStartLevel] Processing configuration [org.ops4j.pax.logging]
16:51:07.308 INFO [FelixStartLevel] Processing configuration [jmx.acl]
16:51:07.308 INFO [FelixStartLevel] Processing configuration [jmx.acl.org.apache.camel]
16:51:07.308 INFO [FelixStartLevel] Processing configuration [org.apache.karaf.command.acl.kar]
16:51:07.308 INFO [FelixStartLevel] Processing configuration [org.apache.karaf.command.acl.feature]
16:51:07.379 INFO [FelixStartLevel] Starting Arrow Flight Server on port 33333
```

# Usage

Once you have installed `karaf-arrow-server` feature in Apache Karaf, you can use `config-flight-client` to get a Configuration for a given PID.

By default, the client retrieves the `org.apache.karaf.shell` configuration. Else, you can pass the PID as argument.

For instance, retrieving `org.apache.karaf.shell` configuration with the client, you will see the following on the console:

```
[main] INFO org.apache.arrow.memory.BaseAllocator - Debug mode disabled. Enable with the VM option -Darrow.memory.debug.allocator=true.
[main] INFO org.apache.arrow.memory.DefaultAllocationManagerOption - allocation manager type not specified, using netty as the default type
[main] INFO org.apache.arrow.memory.CheckAllocator - Using DefaultAllocationManager at memory-netty/14.0.1/arrow-memory-netty-14.0.1.jar!/org/apache/arrow/memory/DefaultAllocationManagerFactory.class
Client connected to grpc+tcp://localhost:33333
Flight info: FlightInfo{schema=Schema<key: Utf8 not null, value: Utf8>, descriptor=org.apache.karaf.shell, endpoints=[FlightEndpoint{locations=[Location{uri=grpc+tcp://0.0.0.0:33333}], ticket=org.apache.arrow.flight.Ticket@3369b90d, expirationTime=(none)}], bytes=-1, records=12, ordered=false}
Row count: 12
key	value
completionMode	GLOBAL
disableEofExit	false
disableLogout	false
felix.fileinstall.filename	file:/Users/jbonofre/Downloads/apache-karaf-4.4.4/etc/org.apache.karaf.shell.cfg
hostKey	/Users/jbonofre/Downloads/apache-karaf-4.4.4/etc/host.key
service.pid	org.apache.karaf.shell
sftpEnabled	true
sshHost	0.0.0.0
sshIdleTimeout	1800000
sshPort	8101
sshRealm	karaf
sshRole	ssh
```
