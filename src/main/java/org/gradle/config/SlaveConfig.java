package org.gradle.config;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef="slaveEntityManagerFactory",
        transactionManagerRef="slaveTransactionManager",
        basePackages= { "org.gradle.slave.repo" }) //Repository Package
public class SlaveConfig {

    @Autowired
    private JpaProperties jpaProperties;

    @Bean(name = "slaveDataSource")
    @Qualifier("slaveDataSource")
    @ConfigurationProperties(prefix="spring.datasource.slave")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "slaveEntityManager")
    public EntityManager entityManager(EntityManagerFactoryBuilder builder
    		,@Qualifier("slaveDataSource")DataSource dataSource) {
        return slaveEntityManagerFactory(builder,dataSource).getObject().createEntityManager();
    }

    @Bean(name = "slaveEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean slaveEntityManagerFactory (EntityManagerFactoryBuilder builder
    		,DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .properties(jpaProperties.getHibernateProperties(dataSource))
                .packages("org.gradle.slave.entity") //Entity Package
                .persistenceUnit("slavePersistenceUnit")
                .build();
    }

    @Bean(name = "slaveTransactionManager")
    PlatformTransactionManager slaveTransactionManager(EntityManagerFactoryBuilder builder
    		,@Qualifier("slaveDataSource")DataSource dataSource) {
        return new JpaTransactionManager(slaveEntityManagerFactory(builder,dataSource).getObject());
    }

    @Bean(name = "slaveJdbcTemplate")
    public JdbcTemplate slaveJdbcTemplate(
            @Qualifier("slaveDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
