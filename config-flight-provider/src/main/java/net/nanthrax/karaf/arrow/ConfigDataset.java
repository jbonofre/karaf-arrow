package net.nanthrax.karaf.arrow;

import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.vector.VarCharVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.table.Table;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.FieldType;
import org.apache.arrow.vector.types.pojo.Schema;
import org.osgi.service.cm.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;

public class ConfigDataset implements AutoCloseable {

    private final Schema schema;
    private final Table table;
    private final long rows;

    public ConfigDataset(Configuration configuration, BufferAllocator allocator) {
        this.schema = new Schema(Arrays.asList(
                new Field("key", FieldType.notNullable(new ArrowType.Utf8()), null),
                new Field("value", FieldType.nullable(new ArrowType.Utf8()), null)
        ));
        VarCharVector keyVector = new VarCharVector("key", allocator);
        keyVector.allocateNew(configuration.getProperties().size());
        int keyIndex = 0;
        Enumeration keys = configuration.getProperties().keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            keyVector.set(keyIndex, key.getBytes(StandardCharsets.UTF_8));
            keyIndex++;
        }
        keyVector.setValueCount(configuration.getProperties().size());

        VarCharVector valueVector = new VarCharVector("value", allocator);
        valueVector.allocateNew(configuration.getProperties().size());
        int valueIndex = 0;
        Enumeration values = configuration.getProperties().elements();
        while (values.hasMoreElements()) {
            String value = values.nextElement().toString();
            valueVector.set(valueIndex, value.getBytes(StandardCharsets.UTF_8));
            valueIndex++;
        }
        valueVector.setValueCount(configuration.getProperties().size());

        this.table = new Table(Arrays.asList(keyVector, valueVector));

        this.rows = configuration.getProperties().size();
    }

    public Schema getSchema() {
        return this.schema;
    }

    public Table getTable() {
        return this.table;
    }

    public long getRows() {
        return this.rows;
    }

    @Override
    public void close() {
        table.close();
    }

}
