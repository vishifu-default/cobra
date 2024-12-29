package org.cobra.core.serialization;

import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.util.DefaultClassResolver;
import com.esotericsoftware.kryo.util.IntMap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class SerdeClassResolver extends DefaultClassResolver {

    public Set<SerdeRegistration> registrationValues() {
        Set<SerdeRegistration> result = new HashSet<>();
        for (IntMap.Entry<Registration> entry : super.idToRegistration) {
            result.add(new SerdeRegistration(entry.value));
        }

        return result;
    }

    public Map<String, Integer> registrationClassTypeEntries() {
        return registrationValues()
                .stream()
                .collect(Collectors.toMap(x -> x.getRegisteredClazz().getTypeName(), SerdeRegistration::getRegisteredId));
    }
}
