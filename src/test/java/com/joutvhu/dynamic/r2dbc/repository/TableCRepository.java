package com.joutvhu.dynamic.r2dbc.repository;

import com.joutvhu.dynamic.r2dbc.DynamicQuery;
import com.joutvhu.dynamic.r2dbc.entity.TableC;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import reactor.core.publisher.Flux;

import java.util.List;

public interface TableCRepository extends ReactiveSortingRepository<TableC, Long> {
    @DynamicQuery("select * from Table_C i\n" +
            "<@where>\n" +
            "   <#if fieldA??>\n" +
            "       i.field_A = :fieldA\n" +
            "   </#if>\n" +
            "   <#if fieldB??>\n" +
            "       and i.field_B like concat('%',:fieldB,'%')\n" +
            "   </#if>\n" +
            "   <#if fieldCs??>\n" +
            "       and i.field_C in (:fieldCs)\n" +
            "   </#if>\n" +
            "</@where>")
    Flux<TableC> search(Long fieldA, String fieldB, List<Long> fieldCs);
}
