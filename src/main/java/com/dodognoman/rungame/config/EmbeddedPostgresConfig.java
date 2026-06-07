package com.dodognoman.rungame.config;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@Configuration
@Profile("local")
public class EmbeddedPostgresConfig {

    @Value("${local.db.port:15432}")
    private int port;

    @Bean(destroyMethod = "close")
    public EmbeddedPostgres embeddedPostgres() throws IOException {
        EmbeddedPostgres pg = EmbeddedPostgres.builder()
                .setPort(port)
                .start();

        // 確保 schema 在 Hibernate 初始化前已建立
        Flyway.configure()
                .dataSource("jdbc:postgresql://localhost:" + port + "/postgres", "postgres", "")
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load()
                .migrate();

        return pg;
    }

    // 確保 dataSource 在 embeddedPostgres 之後才建立連線
    @Bean
    public static BeanDefinitionRegistryPostProcessor embeddedPostgresDependsOn() {
        return new BeanDefinitionRegistryPostProcessor() {
            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
                if (registry.containsBeanDefinition("dataSource")) {
                    BeanDefinition ds = registry.getBeanDefinition("dataSource");
                    ds.setDependsOn("embeddedPostgres");
                }
            }

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {}
        };
    }
}
