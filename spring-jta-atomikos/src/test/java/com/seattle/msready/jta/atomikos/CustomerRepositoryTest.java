package com.seattle.msready.jta.atomikos;

import javax.transaction.Transactional;

import com.seattle.msready.jta.atomikos.config.MainConfig;
import com.seattle.msready.jta.atomikos.domain.customer.Customer;
import com.seattle.msready.jta.atomikos.repository.customer.CustomerRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainConfig.class)
@WebAppConfiguration
public class CustomerRepositoryTest {

	@Autowired
	private CustomerRepository customerRepository;

	@Test
	public void testCustomerConfig() {

	}

	@Test
	public void save() {
		Customer c = new Customer();
		c.setName("test-name");
		c.setAge(30);
		Customer cust = customerRepository.save(c);
		Assert.assertNotNull(cust.getId());
	}

}
