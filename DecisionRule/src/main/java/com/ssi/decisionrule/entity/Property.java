package com.ssi.decisionrule.entity;

public class Property {
    private String name;
    private String value;
    private int id;

    public Property(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Property(int id, String name, String value) {
        this.name = name;
        this.value = value;
        this.id = id;
    }

    public Property(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", id=" + id +
                '}';
    }
}
