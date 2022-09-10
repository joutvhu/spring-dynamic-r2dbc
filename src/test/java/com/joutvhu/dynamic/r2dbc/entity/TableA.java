package com.joutvhu.dynamic.r2dbc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "TABLE_A")
public class TableA {
    @Id
    @Column(name = "FIELD_A")
    private Long fieldA;

    @Column(name = "FIELD_B")
    private Long fieldB;

    @Column(name = "FIELD_C")
    private String fieldC;

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
}
