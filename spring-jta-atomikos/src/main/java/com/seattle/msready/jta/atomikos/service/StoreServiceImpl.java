package com.seattle.msready.jta.atomikos.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.seattle.msready.jta.atomikos.domain.customer.CapitalAccount;
import com.seattle.msready.jta.atomikos.domain.customer.Customer;
import com.seattle.msready.jta.atomikos.domain.order.Order;
import com.seattle.msready.jta.atomikos.domain.order.RedPacketAccount;
import com.seattle.msready.jta.atomikos.exception.NoRollbackException;
import com.seattle.msready.jta.atomikos.exception.StoreException;
import com.seattle.msready.jta.atomikos.repository.customer.CapitalAccountRepository;
import com.seattle.msready.jta.atomikos.repository.customer.CustomerRepository;
import com.seattle.msready.jta.atomikos.repository.order.OrderRepository;
import com.seattle.msready.jta.atomikos.repository.order.RedPacketAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class StoreServiceImpl implements StoreService {
	
	@Autowired
	private CustomerRepository customerRepository;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	CapitalAccountRepository capitalAccountRepository;
	
	@Autowired
	RedPacketAccountRepository redPacketAccountRepository;
	
	@Override
	@Transactional
	public void store(Customer customer, Order order) {
		customerRepository.save(customer);
		orderRepository.save(order);
	}

	@Transactional(rollbackFor = StoreException.class)
	@Override
	public void storeWithStoreException(Customer customer, Order order) throws StoreException {
		customerRepository.save(customer);
		orderRepository.save(order);
		throw new StoreException();
	}

	@Transactional(noRollbackFor = NoRollbackException.class, rollbackFor = StoreException.class)
	@Override
	public void storeWithNoRollbackException(Customer customer, Order order) throws NoRollbackException {
		customerRepository.save(customer);
		orderRepository.save(order);
		throw new NoRollbackException();
	}

	@Transactional()
	public void transfer() {
		CapitalAccount ca1 = capitalAccountRepository.findById(Integer.valueOf(1)).get();
		CapitalAccount ca2 = capitalAccountRepository.findById(Integer.valueOf(2)).get();
		RedPacketAccount rp1 = redPacketAccountRepository.findById(1l).get();
		RedPacketAccount rp2 = redPacketAccountRepository.findById(2l).get();
		BigDecimal capital = BigDecimal.TEN;
		BigDecimal red = BigDecimal.TEN;
		ca1.transferFrom(capital);
		ca2.transferTo(capital);
		capitalAccountRepository.save(ca1);
		capitalAccountRepository.save(ca2);
		rp2.transferFrom(red);
		rp1.transferTo(red);
		redPacketAccountRepository.save(rp1);
		redPacketAccountRepository.save(rp2);
		
	}
	
	@Transactional(rollbackFor = StoreException.class)
	public void transferWithStoreException() throws StoreException {
		CapitalAccount ca1 = capitalAccountRepository.findById(Integer.valueOf(1)).get();
		CapitalAccount ca2 = capitalAccountRepository.findById(Integer.valueOf(2)).get();
		RedPacketAccount rp1 = redPacketAccountRepository.findById(1l).get();
		RedPacketAccount rp2 = redPacketAccountRepository.findById(2l).get();
		
		BigDecimal capital = BigDecimal.TEN;
		BigDecimal red = BigDecimal.TEN;
		
		ca1.transferFrom(capital);
		ca2.transferTo(capital);
		capitalAccountRepository.save(ca1);
		capitalAccountRepository.save(ca2);

		if (rp2.getBalanceAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new StoreException();
		}
		rp2.transferFrom(red);
		rp1.transferTo(red);
		redPacketAccountRepository.save(rp1);
		redPacketAccountRepository.save(rp2);
		
	}
	
	@Transactional(noRollbackFor = NoRollbackException.class, rollbackFor = StoreException.class)
	public void transferWithNoRollbackException() throws NoRollbackException {
		CapitalAccount ca1 = capitalAccountRepository.findById(1).get();
		CapitalAccount ca2 = capitalAccountRepository.findById(2).get();
		RedPacketAccount rp1 = redPacketAccountRepository.findById(1l).get();
		RedPacketAccount rp2 = redPacketAccountRepository.findById(2l).get();
		
		BigDecimal capital = BigDecimal.TEN;
		BigDecimal red = BigDecimal.TEN;
		
		ca1.transferFrom(capital);
		ca2.transferTo(capital);
		capitalAccountRepository.save(ca1);
		capitalAccountRepository.save(ca2);
		if (rp2.getBalanceAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new NoRollbackException();
		}
		rp2.transferFrom(red);
		rp1.transferTo(red);
		redPacketAccountRepository.save(rp1);
		redPacketAccountRepository.save(rp2);
		
	}
}
