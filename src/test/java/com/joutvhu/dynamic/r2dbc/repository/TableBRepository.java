package com.joutvhu.dynamic.r2dbc.repository;

import com.joutvhu.dynamic.r2dbc.DynamicQuery;
import com.joutvhu.dynamic.r2dbc.entity.TableB;
import com.joutvhu.dynamic.r2dbc.model.ModelC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.List;

public interface TableBRepository extends ReactiveCrudRepository<TableB, Long> {
    @DynamicQuery
    List<TableB> findB1(String fieldE);

    @DynamicQuery
    List<TableB> findB2(Long maxD, Pageable pageable);

    @DynamicQuery
    Page<TableB> findB3(Long maxD, Pageable pageable);

    @DynamicQuery()
    Long sumB1(Long maxD);

    @DynamicQuery("select t from TableB t\n" +
            "<#if modelC.fieldC?has_content>\n" +
            "  where t.fieldE = :#{#modelC.fieldC}\n" +
            "</#if>")
    List<TableB> findB4(ModelC modelC);
}
