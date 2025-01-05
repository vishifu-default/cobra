package org.cobra.core.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.util.IntMap;
import com.esotericsoftware.kryo.util.Pool;
import lombok.Getter;
import org.cobra.core.ModelSchema;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SerdeContext {

    @Getter
    private static final SerdeContext instance = new SerdeContext();

    private final Map<Class<?>, Integer> registries = new ConcurrentHashMap<>();
    private final IntMap<Class<?>> idToClazz = new IntMap<>();
    private int nextRegisterId = 0;


    private final Pool<Kryo> kryoPool = new Pool<>(true, true, 64) {
        @Override
        protected Kryo create() {
            Kryo kryo = new Kryo();

            kryo.setDefaultSerializer(FieldSerializer.class);

            for (SerdeRegistration serdeRegistration : DefaultRegistrations.DEFAULT_SERDE_REGISTRATIONS) {
                kryo.register(serdeRegistration.getClazz(),
                        serdeRegistration.getSerializer(),
                        serdeRegistration.getId());
            }

            for (Map.Entry<Class<?>, Integer> entry : registries.entrySet()) {
                kryo.register(entry.getKey(), entry.getValue());
            }

            return kryo;
        }
    };

    public final Pool<Input> inputPool = new Pool<>(true, true, 64) {

        @Override
        protected Input create() {
            return new Input();
        }
    };

    public final Pool<Output> outputPool = new Pool<>(true, true, 64) {

        @Override
        protected Output create() {
            return new Output(4096, -1);
        }
    };

    /* visible to test */
    SerdeContext() {
    }

    public Kryo obtain() {
        return kryoPool.obtain();
    }

    public void free(Kryo kryo) {
        kryoPool.free(kryo);
    }


    public void register(ModelSchema schema) {
        Set<Class<?>> visitAllTypes = ReferentVisits.visits(schema.getClazz(), new HashSet<>(), 0);
        for (Class<?> clazz : visitAllTypes) {
            doRegister(clazz);
        }
    }

    public void register(Class<?> clazz, int id) {
        doRegister(clazz, id);
    }

    public Map<Class<?>, Integer> collectRegistrations() {
        return registries;
    }

    private void doRegister(Class<?> clazz) {
        if (registries.containsKey(clazz)) return;

        int id = getNextRegisterId();
        doRegister(clazz, id);
    }

    private void doRegister(Class<?> clazz, int id) {
        if (registries.containsKey(clazz)) return;

        // todo: xu ly loi
        if (idToClazz.get(id) != null) return;

        registries.put(clazz, id);
        idToClazz.put(id, clazz);
    }

    private int getNextRegisterId() {
        while (this.nextRegisterId != -2) {
            if (getRegisteredClazz(this.nextRegisterId) == null)
                return this.nextRegisterId;

            ++this.nextRegisterId;
        }

        throw new IllegalStateException("Something went wrong");
    }

    private Class<?> getRegisteredClazz(int id) {
        return idToClazz.get(id);
    }

    /* test */
    Map<Class<?>, Integer> getClazzRegistries() {
        return this.registries;
    }
}
