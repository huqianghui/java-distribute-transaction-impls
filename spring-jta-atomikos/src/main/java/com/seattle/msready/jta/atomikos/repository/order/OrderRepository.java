package com.seattle.msready.jta.atomikos.repository.order;

import com.seattle.msready.jta.atomikos.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OrderRepository extends JpaRepository<Order, Integer> {

}
