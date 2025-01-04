package org.cobra.core.serialization;

import com.esotericsoftware.kryo.Kryo;
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


    private SerdeContext serdeContext = SerdeContext.getInstance();

    private UnsafeOutput unsafeOutput;
    private UnsafeInput unsafeInput;

    @Override
    public void register(ModelSchema schema) {
        serdeContext.register(schema);
    }

    @Override
    public void register(Class<?> clazz, int id) {
        serdeContext.register(clazz, id);
    }

    public RecordSerdeImpl() {
        this.unsafeOutput = new UnsafeOutput(4_096, LIMIT_CAPACITY_OF_OBJECT);
        this.unsafeInput = new UnsafeInput();
    }

    @Override
    public byte[] serialize(Object object) {
        // todo: if throws exception due to not register inner class, do register and serialize again
        Kryo kryo = this.serdeContext.obtain();
        try {
            kryo.writeClassAndObject(this.unsafeOutput, object);
            byte[] serialized = this.unsafeOutput.toBytes();
            this.unsafeOutput.reset();

            return serialized;
        } catch (Exception e) {
            log.error("Failed to serialize object {}", object, e);
            throw new CobraException(e);
        } finally {
            this.serdeContext.free(kryo);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes) {
        Kryo kryo = this.serdeContext.obtain();
        try {
            this.unsafeInput.setBuffer(bytes);
            Object result = kryo.readClassAndObject(this.unsafeInput);
            this.unsafeInput.reset();

            return Utils.uncheckedCast(result);
        } catch (Exception e) {
            log.error("Failed to deserialize object {}", bytes, e);
            throw new CobraException(e);
        } finally {
            this.serdeContext.free(kryo);
        }
    }

    @Override
    public SerdeContext serdeContext() {
        return this.serdeContext;
    }
}
