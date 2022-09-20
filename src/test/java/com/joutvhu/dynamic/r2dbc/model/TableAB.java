package com.joutvhu.dynamic.r2dbc.model;

import org.springframework.data.relational.core.mapping.Column;

public class TableAB {
    @Column("FIELD_A")
    private Long fieldA;

    @Column("FIELD_B")
    private Long fieldB;

    @Column("FIELD_C")
    private String fieldC;

    @Column("FIELD_D")
    private Long fieldD;

    @Column("FIELD_E")
    private String fieldE;

    public Long getFieldA() {
        return fieldA;
    }

    public void setFieldA(Long fieldA) {
        this.fieldA = fieldA;
    }

    public Long getFieldB() {
        return fieldB;
    }

    public void setFieldB(Long fieldB) {
        this.fieldB = fieldB;
    }

    public String getFieldC() {
        return fieldC;
    }

    public void setFieldC(String fieldC) {
        this.fieldC = fieldC;
    }

    public Long getFieldD() {
        return fieldD;
    }

    public void setFieldD(Long fieldD) {
        this.fieldD = fieldD;
    }

    public String getFieldE() {
        return fieldE;
    }

    public void setFieldE(String fieldE) {
        this.fieldE = fieldE;
    }
}
