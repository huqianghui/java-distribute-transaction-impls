package com.seattle.msready.mq.transaction.compensating;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MqWithTransactionCompensatingApplication {

	public static void main(String[] args) {
		SpringApplication.run(MqWithTransactionCompensatingApplication.class, args);
	}

}
