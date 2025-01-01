package org.cobra.producer;

public class TypeA {
    int id;
    String name;
    boolean flag;
    TypeB typeB;

    public TypeA() {}

    public TypeA(int id, String name, boolean flag, TypeB typeB) {
        this.id = id;
        this.name = name;
        this.flag = flag;
        this.typeB = typeB;
    }
}
