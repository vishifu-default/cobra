package org.cobra.core.serialization;

import java.lang.reflect.Field;
import java.util.Set;

public class ReferentVisits {

    /**
     * Visit all member of a clazz type, if it's test as non-exclusive, add to returned list.
     * <p>
     * Conceptually, we only add reference type (except WrapperClass, String, ...)
     */
    public static Set<Class<?>> visits(Class<?> clazz, Set<Class<?>> visited, int depth) {
        if (visited.contains(clazz))
            return visited; // stop circular reference

        visited.add(clazz);
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Class<?> type = field.getType();
            if (isEnum(type)) {
                visited.add(type);
                continue;
            }

            if (!isExclusive(type))
                visits(type, visited, depth + 1);
        }

        return visited;
    }

    private static boolean isExclusive(Class<?> type) {
        if (isPrimitive(type) || isWrapperClass(type))
            return true;

        return false;
    }

    private static final Set<Class<?>> WRAPPER_CLAZZ = Set.of(
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            Boolean.class,
            Character.class,
            String.class
    );

    private static boolean isWrapperClass(Class<?> clazz) {
        return WRAPPER_CLAZZ.contains(clazz);
    }

    public static boolean isEnum(Class<?> clazz) {
        return Enum.class.isAssignableFrom(clazz);
    }

    public static boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive();
    }

}
