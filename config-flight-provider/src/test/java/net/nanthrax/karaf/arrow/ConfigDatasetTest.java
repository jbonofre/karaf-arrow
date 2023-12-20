package net.nanthrax.karaf.arrow;

import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.VectorSchemaRoot;
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
        VectorSchemaRoot root = dataset.getRoot();

        assertEquals(3, root.getRowCount());

        FieldVector keysVector = root.getVector("key");
        assertEquals("third", keysVector.getObject(0).toString());
        assertEquals("second", keysVector.getObject(1).toString());
        assertEquals("first", keysVector.getObject(2).toString());

        FieldVector valuesVector = root.getVector("value");
        assertEquals("3", valuesVector.getObject(0).toString());
        assertEquals("2", valuesVector.getObject(1).toString());
        assertEquals("1", valuesVector.getObject(2).toString());
    }

}
