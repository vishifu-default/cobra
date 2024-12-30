package org.cobra.core.serialization;

import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.util.DefaultClassResolver;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class SerdeClassResolver extends DefaultClassResolver {

    private final Set<Integer> selfRegistration = new HashSet<>();

    @Override
    public Registration register(Registration registration) {
        if (!registration.getType().isPrimitive()) {
            selfRegistration.add(registration.getId());
        }
        return super.register(registration);
    }

    public Set<SerdeRegistration> registrationValues() {
        Set<SerdeRegistration> registrations = new HashSet<>();
        for (Integer id : selfRegistration) {
            registrations.add(new SerdeRegistration(getRegistration(id)));
        }

        return registrations;
    }

    public Map<String, Integer> registrationClassTypeEntries() {
        return registrationValues()
                .stream()
                .collect(Collectors.toMap(x -> x.getRegisteredClazz().getTypeName(), SerdeRegistration::getRegisteredId));
    }
}
