package com.seattle.msready.chained.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = {"com.seattle.msready.chained.transaction"})
@EntityScan(basePackages={"com.seattle.msready.chained.transaction.primary"})
public class ChainedTransactionApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChainedTransactionApplication.class, args);
	}
	 
}
