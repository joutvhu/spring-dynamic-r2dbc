package com.joutvhu.dynamic.r2dbc.support;

import com.joutvhu.dynamic.commons.util.ApplicationContextHolder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.ReactiveDataAccessStrategy;
import org.springframework.data.r2dbc.repository.support.R2dbcRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.r2dbc.core.DatabaseClient;

import java.io.Serializable;

/**
 * Special adapter for Springs {@link DynamicR2dbcRepositoryFactoryBean} interface to allow easy setup of
 * repository factories via Spring configuration.
 *
 * @author Giao Ho
 * @since 1.5.0
 */
public class DynamicR2dbcRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable> extends R2dbcRepositoryFactoryBean<T, S, ID> {
    /**
     * Creates a new {@link DynamicR2dbcRepositoryFactoryBean} for the given repository interface.
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    public DynamicR2dbcRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    protected RepositoryFactorySupport getFactoryInstance(DatabaseClient client, ReactiveDataAccessStrategy dataAccessStrategy) {
        return new DynamicR2dbcRepositoryFactory(client, dataAccessStrategy);
    }

    protected RepositoryFactorySupport getFactoryInstance(R2dbcEntityOperations operations) {
        return new DynamicR2dbcRepositoryFactory(operations);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextHolder.appContext = applicationContext;
        super.setApplicationContext(applicationContext);
    }
}
