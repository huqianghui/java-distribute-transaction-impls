package com.seattle.msready.jta.atomikos.config;

import java.util.HashMap;

import javax.sql.DataSource;

import com.mysql.cj.jdbc.MysqlXADataSource;
import com.seattle.msready.jta.atomikos.repository.order.OrderDatasourceProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import com.atomikos.jdbc.AtomikosDataSourceBean;

@Configuration
@DependsOn("transactionManager")
@EnableJpaRepositories(basePackages = "com.seattle.msready.jta.atomikos.repository.order", entityManagerFactoryRef = "orderEntityManager", transactionManagerRef = "transactionManager")
@EnableConfigurationProperties(OrderDatasourceProperties.class)
public class OrderConfig {

	@Autowired
	private JpaVendorAdapter jpaVendorAdapter;

	@Autowired
	private OrderDatasourceProperties orderDatasourceProperties;

	@Bean(name = "orderDataSource", initMethod = "init", destroyMethod = "close")
	public DataSource orderDataSource() throws Exception{
		 MysqlXADataSource mysqlXaDataSource = new MysqlXADataSource();
		 mysqlXaDataSource.setUrl(orderDatasourceProperties.getUrl());
		 mysqlXaDataSource.setPinGlobalTxToPhysicalConnection(true);
		 mysqlXaDataSource.setPassword(orderDatasourceProperties.getPassword());
		 mysqlXaDataSource.setUser(orderDatasourceProperties.getUsername());
		 mysqlXaDataSource.setPinGlobalTxToPhysicalConnection(true);
		
		 AtomikosDataSourceBean xaDataSource = new AtomikosDataSourceBean();
		 xaDataSource.setXaDataSource(mysqlXaDataSource);
		 xaDataSource.setUniqueResourceName("xads2");
		 return xaDataSource;
	}

	@Bean(name = "orderEntityManager")
	public LocalContainerEntityManagerFactoryBean orderEntityManager() throws Throwable {

		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put("hibernate.transaction.jta.platform", AtomikosJtaPlatform.class.getName());
		properties.put("javax.persistence.transactionType", "JTA");

		LocalContainerEntityManagerFactoryBean entityManager = new LocalContainerEntityManagerFactoryBean();
		entityManager.setJtaDataSource(orderDataSource());
		entityManager.setJpaVendorAdapter(jpaVendorAdapter);
		entityManager.setPackagesToScan("com.seattle.msready.jta.atomikos.domain.order");
		entityManager.setPersistenceUnitName("orderPersistenceUnit");
		entityManager.setJpaPropertyMap(properties);
		return entityManager;
	}

}
