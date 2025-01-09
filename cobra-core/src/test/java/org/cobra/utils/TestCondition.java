package org.cobra.utils;

@FunctionalInterface
public interface TestCondition {

    boolean reachCondition() throws Exception;
}
