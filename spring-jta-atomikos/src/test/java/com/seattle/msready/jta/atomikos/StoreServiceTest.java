package com.seattle.msready.jta.atomikos;

import com.seattle.msready.jta.atomikos.config.MainConfig;
import com.seattle.msready.jta.atomikos.domain.customer.Customer;
import com.seattle.msready.jta.atomikos.domain.order.Order;
import com.seattle.msready.jta.atomikos.exception.NoRollbackException;
import com.seattle.msready.jta.atomikos.exception.StoreException;
import com.seattle.msready.jta.atomikos.repository.customer.CustomerRepository;
import com.seattle.msready.jta.atomikos.repository.order.OrderRepository;
import com.seattle.msready.jta.atomikos.service.StoreService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;



@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainConfig.class)
@WebAppConfiguration
public class StoreServiceTest {

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private StoreService storeService;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Test
	@Transactional
	public void testStore() throws Exception {
		Customer c = new Customer();
		c.setName("test");
		c.setAge(30);

		Order o = new Order();
		o.setCode(1);
		o.setQuantity(7);

		storeService.store(c, o);

		Assert.assertNotNull(c.getId());
		Assert.assertNotNull(o.getId());

		Assert.assertEquals(1, customerRepository.findAll().size());
		Assert.assertEquals(1, orderRepository.findAll().size());
	}

//	@Test(expected = StoreException.class)
	public void testStoreWithStoreException() throws StoreException {
		Customer c = new Customer();
		c.setName("test");
		c.setAge(30);

		Order o = new Order();
		o.setCode(1);
		o.setQuantity(7);

		Assert.assertEquals(0, customerRepository.findAll().size());
		Assert.assertEquals(0, orderRepository.findAll().size());

		storeService.storeWithStoreException(c, o);
	}

//	@Test(expected = NoRollbackException.class)
//	@Transactional
	public void testStoreWithNoRollbackException() throws NoRollbackException {
		Customer c = new Customer();
		c.setName("test");
		c.setAge(30);

		Order o = new Order();
		o.setCode(1);
		o.setQuantity(7);

		Assert.assertEquals(0, customerRepository.findAll().size());
		Assert.assertEquals(0, orderRepository.findAll().size());

		try {
			storeService.storeWithNoRollbackException(c, o);
		} catch (NoRollbackException e) {
			e.printStackTrace();
			Assert.assertEquals(1, customerRepository.findAll().size());
			Assert.assertEquals(1, orderRepository.findAll().size());
			throw e;
		}
	}

}
