package com.joutvhu.dynamic.r2dbc;

import com.joutvhu.dynamic.commons.DynamicQueryTemplates;
import com.joutvhu.dynamic.r2dbc.support.DynamicR2dbcRepositoryFactoryBean;
import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

@SpringBootApplication
@EnableR2dbcRepositories(
        basePackages = {"com.joutvhu.dynamic.r2dbc.repository"},
        repositoryFactoryBeanClass = DynamicR2dbcRepositoryFactoryBean.class
)
public class R2dbcDynamicApplication {
    public static void main(String[] args) {
        SpringApplication.run(R2dbcDynamicApplication.class);
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        return new H2ConnectionFactory(
                H2ConnectionConfiguration.builder()
                        .url("mem:testdb;DB_CLOSE_DELAY=-1;")
                        .username("sa")
                        .build()
        );
    }

    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);

        CompositeDatabasePopulator populator = new CompositeDatabasePopulator();
        populator.addPopulators(new ResourceDatabasePopulator(new ClassPathResource("sql/table.sql")));
        initializer.setDatabasePopulator(populator);

        return initializer;
    }

    @Bean
    public DynamicQueryTemplates dynamicQueryTemplates() {
        DynamicQueryTemplates queryTemplates = new DynamicQueryTemplates();
        queryTemplates.setSuffix(".dsql");
        return queryTemplates;
    }
}
