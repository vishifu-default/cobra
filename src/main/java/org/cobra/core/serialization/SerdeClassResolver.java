package org.cobra.core.serialization;

import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.util.DefaultClassResolver;
import com.esotericsoftware.kryo.util.IntMap;

import java.util.HashSet;
import java.util.Set;

public final class SerdeClassResolver extends DefaultClassResolver {

    public Set<SerdeRegistration> registrationEntries() {
        Set<SerdeRegistration> result = new HashSet<>();
        for (IntMap.Entry<Registration> entry : super.idToRegistration) {
            result.add(new SerdeRegistration(entry.value));
        }

        return result;
    }
}
