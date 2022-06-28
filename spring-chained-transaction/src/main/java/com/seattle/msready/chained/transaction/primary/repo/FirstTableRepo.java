package com.seattle.msready.chained.transaction.primary.repo;

import com.seattle.msready.chained.transaction.primary.entity.FirstTableEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface FirstTableRepo extends CrudRepository<FirstTableEntity, Integer> {

}
