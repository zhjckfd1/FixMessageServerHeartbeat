package com.mes;

public class Field {
    private String name, type;
    private boolean required;
    private int tag;

    public Field(String name, String type, boolean required, int tag) {
        this.name = name;
        this.type = type;
        this.required = required;
        this.tag = tag;
    }

    //need?
    public void setName(String name) {
        this.name = name;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setTag(int tag) {
        this.tag = tag;
    }
    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getTag() {
        return tag;
    }

    public Boolean isRequired() {
        return required;
    }

}
