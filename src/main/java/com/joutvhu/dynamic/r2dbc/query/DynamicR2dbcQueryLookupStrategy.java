package com.joutvhu.dynamic.r2dbc.query;

import com.joutvhu.dynamic.r2dbc.DynamicQuery;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.ReactiveDataAccessStrategy;
import org.springframework.data.r2dbc.repository.support.DynamicCachingExpressionParser;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.ReactiveQueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;

/**
 * {@link QueryLookupStrategy} that tries to detect a dynamic query declared via {@link DynamicQuery} annotation.
 *
 * @author Giao Ho
 * @since 1.5.0
 */
public class DynamicR2dbcQueryLookupStrategy implements QueryLookupStrategy {
    private static final SpelExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    private final R2dbcEntityOperations entityOperations;
    private final ReactiveQueryMethodEvaluationContextProvider evaluationContextProvider;
    private final R2dbcConverter converter;
    private final ReactiveDataAccessStrategy dataAccessStrategy;
    private final ExpressionParser parser = new DynamicCachingExpressionParser(EXPRESSION_PARSER);
    private final QueryLookupStrategy r2dbcQueryLookupStrategy;

    public DynamicR2dbcQueryLookupStrategy(
            R2dbcEntityOperations entityOperations,
            ReactiveQueryMethodEvaluationContextProvider evaluationContextProvider,
            R2dbcConverter converter,
            ReactiveDataAccessStrategy dataAccessStrategy,
            QueryLookupStrategy r2dbcQueryLookupStrategy
    ) {
        this.entityOperations = entityOperations;
        this.evaluationContextProvider = evaluationContextProvider;
        this.converter = converter;
        this.dataAccessStrategy = dataAccessStrategy;
        this.r2dbcQueryLookupStrategy = r2dbcQueryLookupStrategy;
    }

    @Override
    public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory, NamedQueries namedQueries) {
        if (isMethodDynamicJpaHandle(method)) {
            DynamicR2dbcQueryMethod queryMethod = new DynamicR2dbcQueryMethod(method, metadata, factory, this.converter.getMappingContext());
            return new DynamicR2dbcRepositoryQuery(
                    queryMethod,
                    this.entityOperations,
                    this.converter,
                    this.dataAccessStrategy,
                    this.parser,
                    this.evaluationContextProvider
            );
        } else {
            return r2dbcQueryLookupStrategy.resolveQuery(method, metadata, factory, namedQueries);
        }
    }

    private boolean isMethodDynamicJpaHandle(Method method) {
        DynamicQuery annotation = method.getAnnotation(DynamicQuery.class);
        return annotation != null;
    }
}
