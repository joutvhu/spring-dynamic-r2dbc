package com.joutvhu.dynamic.r2dbc.repository;

import com.joutvhu.dynamic.r2dbc.DynamicQuery;
import com.joutvhu.dynamic.r2dbc.entity.TableB;
import com.joutvhu.dynamic.r2dbc.model.ModelC;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface TableBRepository extends ReactiveCrudRepository<TableB, Long> {
    @DynamicQuery
    Flux<TableB> findB1(String fieldE);

    @DynamicQuery
    Flux<TableB> findB2(Long maxD);

    @Query("select * from Table_B")
    Flux<TableB> findB3(String fieldE);

    @DynamicQuery
    Flux<Long> sumB1(Long maxD);

    @DynamicQuery("select * from Table_B t\n" +
            "<#if modelC.fieldC?has_content>\n" +
            "  where t.field_E = :#{#modelC.fieldC}\n" +
            "</#if>")
    Flux<TableB> findB4(ModelC modelC);
}
