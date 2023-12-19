package net.nanthrax.karaf.arrow;

import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.holders.NullableVarCharHolder;
import org.apache.arrow.vector.table.Row;
import org.apache.arrow.vector.table.Table;
import org.junit.jupiter.api.Test;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigDatasetTest {

    @Test
    public void vectorPopulationTest() throws Exception {
        BufferAllocator allocator = new RootAllocator();
        Configuration configuration = new Configuration() {
            @Override
            public String getPid() {
                return "net.nanthrax.karaf.arrow.test";
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
                // no-op
            }

            @Override
            public void delete() throws IOException {
                // no-op
            }

            @Override
            public String getFactoryPid() {
                return null;
            }

            @Override
            public void update() throws IOException {
                // no-op
            }

            @Override
            public boolean updateIfDifferent(Dictionary<String, ?> properties) throws IOException {
                return false;
            }

            @Override
            public void setBundleLocation(String location) {
                // no-op
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
                // no-op
            }

            @Override
            public Set<ConfigurationAttribute> getAttributes() {
                return null;
            }

            @Override
            public void removeAttributes(ConfigurationAttribute... attrs) throws IOException {
                // no-op
            }
        };
        ConfigDataset dataset = new ConfigDataset(configuration, allocator);
        Table table = dataset.getTable();

        assertEquals(3, table.getRowCount());

        Row row = table.immutableRow();

        Row first = row.setPosition(2);
        assertEquals("first", first.getVarCharObj("key"));
        assertEquals("1", first.getVarCharObj("value"));

        Row second = row.setPosition(1);
        assertEquals("second", second.getVarCharObj("key"));
        assertEquals("2", second.getVarCharObj("value"));

        Row third = row.setPosition(0);
        assertEquals("third", third.getVarCharObj("key"));
        assertEquals("3", third.getVarCharObj("value"));
    }

}
