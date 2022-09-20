package com.joutvhu.dynamic.r2dbc.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("TABLE_C")
public class TableC {
    @Id
    @Column("FIELD_A")
    private Long fieldA;

    @Column("FIELD_B")
    private String fieldB;

    @Column("FIELD_C")
    private Long fieldC;

    public Long getFieldA() {
        return fieldA;
    }

    public void setFieldA(Long fieldA) {
        this.fieldA = fieldA;
    }

    public String getFieldB() {
        return fieldB;
    }

    public void setFieldB(String fieldB) {
        this.fieldB = fieldB;
    }

    public Long getFieldC() {
        return fieldC;
    }

    public void setFieldC(Long fieldC) {
        this.fieldC = fieldC;
    }
}
