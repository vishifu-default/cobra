package org.cobra.core.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.unsafe.UnsafeInput;
import com.esotericsoftware.kryo.unsafe.UnsafeOutput;
import org.cobra.commons.errors.CobraException;
import org.cobra.commons.utils.Utils;
import org.cobra.core.ModelSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecordSerdeImpl implements RecordSerde {

    // todo: shrink output buffer size
    // todo: need focus on limit of record

    private static final Logger log = LoggerFactory.getLogger(RecordSerdeImpl.class);

    private static final int LIMIT_CAPACITY_OF_OBJECT = 1 << 22; // approximate 4MiB


    private final SerdeContext serdeContext = SerdeContext.getInstance();

    @Override
    public void register(ModelSchema schema) {
        serdeContext.register(schema);
    }

    @Override
    public void register(Class<?> clazz, int id) {
        serdeContext.register(clazz, id);
    }

    public RecordSerdeImpl() {

    }

    @Override
    public byte[] serialize(Object object) {
        // todo: if throws exception due to not register inner class, do register and serialize again
        Kryo kryo = this.serdeContext.obtain();
        Output output = this.serdeContext.outputPool.obtain();
        try {
            kryo.writeClassAndObject(output, object);
            byte[] serialized = output.toBytes();

            return serialized;
        } catch (Exception e) {
            log.error("Failed to serialize object {}", object, e);
            throw new CobraException(e);
        } finally {
            this.serdeContext.free(kryo);
            this.serdeContext.outputPool.free(output);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes) {
        Kryo kryo = this.serdeContext.obtain();
        Input input = this.serdeContext.inputPool.obtain();
        try {
            input.setBuffer(bytes);
            Object result = kryo.readClassAndObject(input);
            input.reset();

            return Utils.uncheckedCast(result);
        } catch (Exception e) {
            log.error("Failed to deserialize object {}", bytes, e);
            throw new CobraException(e);
        } finally {
            this.serdeContext.free(kryo);
            this.serdeContext.inputPool.free(input);
        }
    }

    @Override
    public SerdeContext serdeContext() {
        return this.serdeContext;
    }
}
