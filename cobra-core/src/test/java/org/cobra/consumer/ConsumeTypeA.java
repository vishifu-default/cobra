package org.cobra.consumer;

public class ConsumeTypeA {
    int id;
    String name;
    boolean flag;
    ConsumeTypeB typeB;

    public ConsumeTypeA() {
    }

    public ConsumeTypeA(int id, String name, boolean flag, ConsumeTypeB typeB) {
        this.id = id;
        this.name = name;
        this.flag = flag;
        this.typeB = typeB;
    }
}
