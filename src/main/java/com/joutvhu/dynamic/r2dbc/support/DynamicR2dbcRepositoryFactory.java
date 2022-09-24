package com.joutvhu.dynamic.r2dbc.support;

import com.joutvhu.dynamic.r2dbc.query.DynamicR2dbcQueryLookupStrategy;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.core.ReactiveDataAccessStrategy;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.r2dbc.repository.support.R2dbcRepositoryFactory;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.ReactiveQueryMethodEvaluationContextProvider;
import org.springframework.r2dbc.core.DatabaseClient;

import java.util.Optional;

/**
 * Factory to create {@link R2dbcRepository} instances.
 *
 * @author Giao Ho
 * @since 1.5.0
 */
public class DynamicR2dbcRepositoryFactory extends R2dbcRepositoryFactory {
    private final ReactiveDataAccessStrategy dataAccessStrategy;
    private final R2dbcConverter converter;
    private final R2dbcEntityOperations operations;

    public DynamicR2dbcRepositoryFactory(DatabaseClient databaseClient, ReactiveDataAccessStrategy dataAccessStrategy) {
        this(new R2dbcEntityTemplate(databaseClient, dataAccessStrategy));
    }

    public DynamicR2dbcRepositoryFactory(R2dbcEntityOperations operations) {
        super(operations);
        this.dataAccessStrategy = operations.getDataAccessStrategy();
        this.converter = dataAccessStrategy.getConverter();
        this.operations = operations;
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(QueryLookupStrategy.Key key, QueryMethodEvaluationContextProvider evaluationContextProvider) {
        Optional<QueryLookupStrategy> queryLookupStrategy = super.getQueryLookupStrategy(key, evaluationContextProvider);
        if (queryLookupStrategy.isPresent()) {
            return Optional.of(new DynamicR2dbcQueryLookupStrategy(
                    this.operations,
                    (ReactiveQueryMethodEvaluationContextProvider) evaluationContextProvider,
                    this.converter,
                    this.dataAccessStrategy,
                    queryLookupStrategy.get()
            ));
        }
        return queryLookupStrategy;
    }
}
