package com.joutvhu.dynamic.r2dbc.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("TABLE_B")
public class TableB {
    @Id
    @Column("FIELD_A")
    private Long fieldA;

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
