package com.joutvhu.dynamic.r2dbc.model;

import org.springframework.data.relational.core.mapping.Column;

public class ModelC {
    @Column("FIELD_E")
    private Long fieldA;

    @Column("FIELD_C")
    private String fieldC;

    public ModelC() {
    }

    public ModelC(Long fieldA, String fieldC) {
        this.fieldA = fieldA;
        this.fieldC = fieldC;
    }

    public Long getFieldA() {
        return fieldA;
    }

    public void setFieldA(Long fieldA) {
        this.fieldA = fieldA;
    }

    public String getFieldC() {
        return fieldC;
    }

    public void setFieldC(String fieldC) {
        this.fieldC = fieldC;
    }
}
