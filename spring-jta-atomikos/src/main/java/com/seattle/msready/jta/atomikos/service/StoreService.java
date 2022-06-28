package com.seattle.msready.jta.atomikos.service;


import com.seattle.msready.jta.atomikos.domain.customer.Customer;
import com.seattle.msready.jta.atomikos.domain.order.Order;
import com.seattle.msready.jta.atomikos.exception.NoRollbackException;
import com.seattle.msready.jta.atomikos.exception.StoreException;

public interface StoreService {
	
	void store(Customer customer, Order order) throws Exception;
	
	void storeWithStoreException(Customer customer, Order order) throws StoreException;
	
	void storeWithNoRollbackException(Customer customer, Order order) throws NoRollbackException;
	
	void transferWithStoreException() throws StoreException;
	void transferWithNoRollbackException() throws NoRollbackException;
	void transfer();

}
