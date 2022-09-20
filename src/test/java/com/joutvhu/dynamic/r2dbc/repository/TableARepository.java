package com.joutvhu.dynamic.r2dbc.repository;

import com.joutvhu.dynamic.r2dbc.DynamicQuery;
import com.joutvhu.dynamic.r2dbc.entity.TableA;
import com.joutvhu.dynamic.r2dbc.model.TableAB;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface TableARepository extends R2dbcRepository<TableA, Long> {
    @DynamicQuery(value = "select * from Table_A t where t.field_B = :fieldB\n" +
            "<#if fieldC?has_content>\n" +
            "  and t.field_C = :fieldC\n" +
            "</#if>"
    )
    Flux<TableA> findA1(Long fieldB, String fieldC);

    @Query(value = "select * from Table_A t where t.field_A = :fieldA and t.field_C = :fieldC")
    Flux<TableA> findA2(Long fieldA, String fieldC);

    @DynamicQuery(value = "select * from Table_A a inner join Table_B b\n" +
            "on a.field_A = b.field_A\n" +
            "<#if fieldB??>\n" +
            "  and a.field_B = :fieldB\n" +
            "</#if>" +
            "<#if fieldD??>\n" +
            "  and b.field_D = :fieldD\n" +
            "</#if>"
    )
    Flux<TableAB> findJ(Long fieldB, Long fieldD);
}
