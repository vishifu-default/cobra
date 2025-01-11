package org.cobra.consumer.internal;

import org.cobra.commons.CobraConstants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TransitionPlan implements Iterable<VersionTransition> {

    private final List<VersionTransition> transitions = new ArrayList<>();

    @Override
    public @NotNull Iterator<VersionTransition> iterator() {
        return transitions.iterator();
    }

    public List<VersionTransition> getTransitions() {
        return transitions;
    }

    public int number() {
        return transitions.size();
    }

    public VersionTransition getTransition(int i) {
        return transitions.get(i);
    }

    public long getFinalDestinationVersion() {
        return transitions.isEmpty() ? CobraConstants.VERSION_NULL : transitions.getLast().getVersion();
    }

    public void add(VersionTransition transition) {
        transitions.add(transition);
    }

    public void add(TransitionPlan transitionPlan) {
        transitions.addAll(transitionPlan.getTransitions());
    }

    @Override
    public String toString() {
        return "TransitionPlan(" +
                "transitions_size=" + transitions.size() +
                ')';
    }
}
