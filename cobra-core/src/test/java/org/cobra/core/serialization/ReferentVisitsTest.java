package org.cobra.core.serialization;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("ALL")
class ReferentVisitsTest {

    @Test
    void visitsAllPrimitive_shouldOnlyItself() {
        Set<Class<?>> visitedReferents = ReferentVisits.visits(AllPrimitiveDataType.class, new HashSet<>(), 0);
        assertEquals(1, visitedReferents.size());
    }

    @Test
    void visitsWrapper_shouldOnlyItself() {
        Set<Class<?>> visitedReferents = ReferentVisits.visits(CompositionWrapper.class, new HashSet<>(), 0);
        assertEquals(1, visitedReferents.size());
    }

    @Test
    void visitReference() {
        Set<Class<?>> visitedReferents = ReferentVisits.visits(CompositionReference.class, new HashSet<>(), 0);
        assertEquals(3, visitedReferents.size());
        assertTrue(visitedReferents.contains(AllPrimitiveDataType.class));
        assertTrue(visitedReferents.contains(CompositionWrapper.class));
        assertTrue(visitedReferents.contains(CompositionReference.class));
    }

    @Test
    void visitReference_multiNestedLevel() {
        Set<Class<?>> visitedReferents = ReferentVisits.visits(CompositionReference.class, new HashSet<>(), 0);
        assertEquals(3, visitedReferents.size());
    }

    private static final class MultipleNestedReference {
        CompositionReference compositionReference;
        CompositionWrapper compositionWrapper;
    }

    private static final class CompositionReference {
        int intVal;
        CompositionWrapper compositionWrapper;
        AllPrimitiveDataType allPrimitiveDataType;
    }

    private static final class CompositionWrapper {
        int intVal;
        Integer boxIntVal;
        Long boxLongVal;
        String boxStrVal;
    }

    private static final class AllPrimitiveDataType {
        int intVal;
        long longVal;
        double doubleVal;
    }
}