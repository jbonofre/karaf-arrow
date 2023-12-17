package net.nanthrax.karaf.arrow;

import org.apache.arrow.vector.ipc.message.ArrowRecordBatch;
import org.apache.arrow.vector.types.pojo.Schema;

import java.util.List;

public class ConfigDataset implements AutoCloseable{

    private final List<ArrowRecordBatch> batches;
    private final Schema schema;
    private final long rows;

    public ConfigDataset(List<ArrowRecordBatch> batches, Schema schema, long rows) {
        this.batches = batches;
        this.schema = schema;
        this.rows = rows;
    }

    public List<ArrowRecordBatch> getBatches() {
        return this.batches;
    }

    public Schema getSchema() {
        return this.schema;
    }

    public long getRows() {
        return this.rows;
    }

    @Override
    public void close() {

    }

}
