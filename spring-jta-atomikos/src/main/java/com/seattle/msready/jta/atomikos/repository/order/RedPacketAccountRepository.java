package com.seattle.msready.jta.atomikos.repository.order;


import com.seattle.msready.jta.atomikos.domain.order.RedPacketAccount;
import org.springframework.data.repository.CrudRepository;


public interface RedPacketAccountRepository extends CrudRepository<RedPacketAccount, Long> {


}
