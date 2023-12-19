package net.nanthrax.karaf.arrow;

import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.table.Row;
import org.junit.jupiter.api.Test;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigProducerTest {

    @Test
    public void getStream() throws Exception {
        ConfigurationAdmin configurationAdmin = new ConfigAdminTest();
        BufferAllocator allocator = new RootAllocator();
        ConfigProducer configProducer = new ConfigProducer(allocator, null);
        configProducer.init(configurationAdmin);
        ConfigDataset dataset = configProducer.getConfigDataset("my.first.pid");

        assertEquals(3, dataset.getTable().getRowCount());

        Row first = dataset.getTable().immutableRow().setPosition(2);
        assertEquals("first", first.getVarCharObj("key"));
        assertEquals("1", first.getVarCharObj("value"));

        Row second = dataset.getTable().immutableRow().setPosition(1);
        assertEquals("second", second.getVarCharObj("key"));
        assertEquals("2", second.getVarCharObj("value"));

        Row third = dataset.getTable().immutableRow().setPosition(0);
        assertEquals("third", third.getVarCharObj("key"));
        assertEquals("3", third.getVarCharObj("value"));
    }

    class ConfigurationTest implements Configuration {

        private String pid;

        public ConfigurationTest(String pid) {
            this.pid = pid;
        }

        @Override
        public String getPid() {
            return pid;
        }

        @Override
        public Dictionary<String, Object> getProperties() {
            Hashtable<String, Object> properties = new Hashtable<>();
            properties.put("first", "1");
            properties.put("second", "2");
            properties.put("third", "3");
            return properties;
        }

        @Override
        public Dictionary<String, Object> getProcessedProperties(ServiceReference<?> reference) {
            return null;
        }

        @Override
        public void update(Dictionary<String, ?> properties) throws IOException {

        }

        @Override
        public void delete() throws IOException {

        }

        @Override
        public String getFactoryPid() {
            return null;
        }

        @Override
        public void update() throws IOException {

        }

        @Override
        public boolean updateIfDifferent(Dictionary<String, ?> properties) throws IOException {
            return false;
        }

        @Override
        public void setBundleLocation(String location) {

        }

        @Override
        public String getBundleLocation() {
            return null;
        }

        @Override
        public long getChangeCount() {
            return 0;
        }

        @Override
        public void addAttributes(ConfigurationAttribute... attrs) throws IOException {

        }

        @Override
        public Set<ConfigurationAttribute> getAttributes() {
            return null;
        }

        @Override
        public void removeAttributes(ConfigurationAttribute... attrs) throws IOException {

        }
    }

    class ConfigAdminTest implements ConfigurationAdmin {

        private Map<String, Configuration> store = new HashMap<>();

        public ConfigAdminTest() {
            store.put("my.first.pid", new ConfigurationTest("my.first.pid"));
            store.put("my.second.pid", new ConfigurationTest("my.second.pid"));
        }

        @Override
        public Configuration createFactoryConfiguration(String factoryPid) throws IOException {
            return null;
        }

        @Override
        public Configuration createFactoryConfiguration(String factoryPid, String location) throws IOException {
            return null;
        }

        @Override
        public Configuration getConfiguration(String pid, String location) throws IOException {
            return null;
        }

        @Override
        public Configuration getConfiguration(String pid) throws IOException {
            return store.get(pid);
        }

        @Override
        public Configuration getFactoryConfiguration(String factoryPid, String name, String location) throws IOException {
            return null;
        }

        @Override
        public Configuration getFactoryConfiguration(String factoryPid, String name) throws IOException {
            return null;
        }

        @Override
        public Configuration[] listConfigurations(String filter) throws IOException, InvalidSyntaxException {
            return store.values().toArray(new Configuration[0]);
        }
    }

}
