package com.seattle.msready.chained.transaction.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class FirstDataSourceConfig {

	@Primary
	@Bean(name="firstDatasource")
	@ConfigurationProperties(prefix="datasource.mysql.primary")
	public DataSource firstDataSource(){
	    return DataSourceBuilder.create().build();
	}

    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier("firstDatasource") DataSource dataSource) {
    	DataSourceTransactionManager txManager = new DataSourceTransactionManager(dataSource);
    	return txManager;
    }

}
