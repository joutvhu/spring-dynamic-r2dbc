package com.joutvhu.dynamic.r2dbc.repository;

import com.joutvhu.dynamic.r2dbc.DynamicQuery;
import com.joutvhu.dynamic.r2dbc.entity.TableC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TableCRepository extends JpaRepository<TableC, Long> {
    @DynamicQuery("select i from TableC i\n" +
            "<@where>\n" +
            "   <#if fieldA??>\n" +
            "       i.fieldA = :fieldA\n" +
            "   </#if>\n" +
            "   <#if fieldB??>\n" +
            "       and i.fieldB like concat('%',:fieldB,'%')\n" +
            "   </#if>\n" +
            "   <#if fieldCs??>\n" +
            "       and i.fieldC in :fieldCs\n" +
            "   </#if>\n" +
            "</@where>")
    Page<TableC> search(Long fieldA, String fieldB, List<Long> fieldCs, Pageable pageable);
}
