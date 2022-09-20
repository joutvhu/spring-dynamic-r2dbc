package com.joutvhu.dynamic.r2dbc;

import com.joutvhu.dynamic.r2dbc.support.DynamicR2dbcRepositoryFactoryBean;
import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableR2dbcRepositories(
        basePackages = {"com.joutvhu.dynamic.r2dbc.repository"},
        repositoryFactoryBeanClass = DynamicR2dbcRepositoryFactoryBean.class
)
public class JpaDynamicApplication {
    public static void main(String[] args) {
        SpringApplication.run(JpaDynamicApplication.class);
    }

    @Bean
    @ConfigurationProperties("spring.r2dbc")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public ConnectionFactory connectionFactory(DataSourceProperties dataSourceProperties) {
        return new H2ConnectionFactory(
                H2ConnectionConfiguration.builder()
                        .url(dataSourceProperties.getUrl())
                        .username(dataSourceProperties.getUsername())
                        .password(dataSourceProperties.getPassword())
                        .build()
        );
    }

    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);

        CompositeDatabasePopulator populator = new CompositeDatabasePopulator();
        populator.addPopulators(new ResourceDatabasePopulator(new ClassPathResource("table.sql")));
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
