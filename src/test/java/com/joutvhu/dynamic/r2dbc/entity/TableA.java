package com.joutvhu.dynamic.r2dbc.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("TABLE_A")
public class TableA {
    @Id
    @Column("FIELD_A")
    private Long fieldA;

    @Column("FIELD_B")
    private Long fieldB;

    @Column("FIELD_C")
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
