package com.seattle.msready.chained.transaction.service;

import com.seattle.msready.chained.transaction.primary.entity.FirstTableEntity;
import com.seattle.msready.chained.transaction.primary.repo.FirstTableRepo;
import com.seattle.msready.chained.transaction.second.dao.SecondTableDao;
import com.seattle.msready.chained.transaction.third.dao.ThirdTableDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
public class FillTables {

	@Autowired
	private SecondTableDao secondTableDao;
	
	@Autowired
	private ThirdTableDao thirdTableDao;
	
	@Autowired
	private FirstTableRepo firstTableRepo;

	@Transactional(transactionManager="globalTransactionManager")
	public void fill(){
		firstTableRepo.save(FirstTableEntity.createNew());
		secondTableDao.insert();
		thirdTableDao.insert();
	}
	
}	
