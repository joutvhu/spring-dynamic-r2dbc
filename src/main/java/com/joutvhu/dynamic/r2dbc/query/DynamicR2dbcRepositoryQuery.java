package com.joutvhu.dynamic.r2dbc.query;

import com.joutvhu.dynamic.r2dbc.DynamicQuery;
import freemarker.template.Template;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.ReactiveDataAccessStrategy;
import org.springframework.data.r2dbc.repository.query.DynamicR2dbcParameterAccessor;
import org.springframework.data.r2dbc.repository.query.DynamicStringBasedR2dbcQuery;
import org.springframework.data.relational.repository.query.RelationalParameterAccessor;
import org.springframework.data.repository.query.ReactiveQueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.expression.ExpressionParser;
import org.springframework.r2dbc.core.PreparedOperation;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * {@link RepositoryQuery} implementation that inspects a {@link DynamicR2dbcQueryMethod}
 * for the existence of an {@link DynamicQuery} annotation and creates a R2dbc {@link DynamicQuery} from it.
 *
 * @author Giao Ho
 * @since 1.5.0
 */
public class DynamicR2dbcRepositoryQuery extends DynamicStringBasedR2dbcQuery {
    private final DynamicR2dbcQueryMethod method;

    public DynamicR2dbcRepositoryQuery(
            DynamicR2dbcQueryMethod method,
            R2dbcEntityOperations entityOperations,
            R2dbcConverter converter,
            ReactiveDataAccessStrategy dataAccessStrategy,
            ExpressionParser expressionParser,
            ReactiveQueryMethodEvaluationContextProvider evaluationContextProvider
    ) {
        super(method, entityOperations, converter, dataAccessStrategy, expressionParser, evaluationContextProvider);
        this.method = method;
    }

    @Override
    protected boolean isModifyingQuery() {
        return getQueryMethod().isModifyingQuery();
    }

    @Override
    protected boolean isCountQuery() {
        return false;
    }

    @Override
    protected boolean isExistsQuery() {
        return false;
    }

    @Override
    protected Mono<PreparedOperation<?>> createQuery(RelationalParameterAccessor accessor) {
        String queryString = buildQuery(method.getQueryTemplate(), accessor);
        return super.createQuery(queryString, accessor);
    }

    protected String buildQuery(Template template, RelationalParameterAccessor accessor) {
        try {
            if (template != null) {
                Map<String, Object> model = DynamicR2dbcParameterAccessor.of(method, accessor).getParamModel();
                String queryString = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
                queryString = queryString
                        .replaceAll("\n", " ")
                        .replaceAll("\t", " ")
                        .replaceAll(" +", " ")
                        .trim();
                return queryString.isEmpty() ? null : queryString;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
