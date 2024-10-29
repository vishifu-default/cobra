package org.cobra.core.serialization;

import com.esotericsoftware.kryo.Registration;

public class SerdeRegistration {
    private final Registration kryoRegistration;

    public SerdeRegistration(Registration kryoRegistration) {
        this.kryoRegistration = kryoRegistration;
    }

    public int getRegisteredId(){
        return this.kryoRegistration.getId();
    }

    public Class<?> getRegisteredClazz() {
        return this.kryoRegistration.getType();
    }
}
