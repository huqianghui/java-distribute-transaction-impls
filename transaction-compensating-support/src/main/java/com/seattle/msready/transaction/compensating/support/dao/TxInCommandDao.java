package com.seattle.msready.transaction.compensating.support.dao;

import java.util.Date;
import java.util.List;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;


import com.seattle.msready.transaction.compensating.support.utils.PageableExecutionUtils;
import com.seattle.msready.transaction.compensating.support.config.TransactionCompensatingAutoConfig;
import com.seattle.msready.transaction.compensating.support.entity.TxInCommandEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

@Repository
public class TxInCommandDao {

	public Class<TxInCommandEntity> getEntityClass() {
		return TxInCommandEntity.class;
	}

	public TxInCommandEntity findByCommandUUID(String commandUUID) {
		String sql = "from TxInCommandEntity where commandUUID=:commandUUID";
		Query query = TransactionCompensatingAutoConfig.getEntityManager().createQuery(sql, TxInCommandEntity.class);
		query.setParameter("commandUUID", commandUUID);
		List<TxInCommandEntity> list = query.getResultList();
		if (CollectionUtils.isEmpty(list) || list.size() == 0) {
			return null;
		} else if (list.size() == 1) {
			return list.get(0);
		}
		return null;
	}

	public Page<TxInCommandEntity> findByisSuccessful$CommandUUID$globalTransactionId$transactionType$transactionTime$pageIndex$pageSize(
			String isSuccessful, String globalTransactionId, long transactionType, String commandUUID,
			Date processStartTime, Date processEndTime,  Integer pageIndex,
			Integer pageSize) {

		CriteriaBuilder critBuilderCount = TransactionCompensatingAutoConfig.getEntityManager().getCriteriaBuilder();
		CriteriaQuery<Long> critQueryCount = critBuilderCount.createQuery(Long.class);
		Root<TxInCommandEntity> rootCount = critQueryCount.from(TxInCommandEntity.class);
		critQueryCount.select(critBuilderCount.countDistinct(rootCount));

		CriteriaBuilder criteriaBuilder = TransactionCompensatingAutoConfig.getEntityManager().getCriteriaBuilder();
		CriteriaQuery<TxInCommandEntity> criteriaQuery = criteriaBuilder.createQuery(TxInCommandEntity.class);
		Root<TxInCommandEntity> root = criteriaQuery.from(TxInCommandEntity.class);
		criteriaQuery.select(root);

		Predicate restrictions = criteriaBuilder.conjunction();
		Predicate restrictionsCount = critBuilderCount.conjunction();

		if (isSuccessful != null && !"".equals(isSuccessful)) {
			restrictions = criteriaBuilder.and(restrictions,
					criteriaBuilder.equal(root.get("isSuccessful").as(String.class), isSuccessful));
			restrictionsCount = critBuilderCount.and(restrictionsCount,
					critBuilderCount.equal(root.get("isSuccessful").as(String.class), isSuccessful));
		}

		if (commandUUID != null && !"0".equals(commandUUID)) {
			restrictions = criteriaBuilder.and(restrictions,
					criteriaBuilder.like(root.get("commandUUID").as(String.class), commandUUID));
			restrictionsCount = critBuilderCount.and(restrictionsCount,
					critBuilderCount.like(root.get("commandUUID").as(String.class), commandUUID));
		}
		if (!"0".equals(globalTransactionId) ) {
			restrictions = criteriaBuilder.and(restrictions,
					criteriaBuilder.equal(root.get("globalTransactionId").as(String.class), globalTransactionId));
			restrictionsCount = critBuilderCount.and(restrictionsCount,
					critBuilderCount.equal(root.get("globalTransactionId").as(String.class), globalTransactionId));
		}
		if (transactionType != 0) {
			restrictions = criteriaBuilder.and(restrictions,
					criteriaBuilder.equal(root.get("transactionType").as(long.class), transactionType));
			restrictionsCount = critBuilderCount.and(restrictionsCount,
					critBuilderCount.equal(root.get("transactionType").as(long.class), transactionType));
		}
		if (processStartTime != null && processEndTime != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder
					.between(root.get("insertTime").as(Date.class), processStartTime, processEndTime));
			restrictionsCount = critBuilderCount.and(restrictionsCount, critBuilderCount
					.between(root.get("insertTime").as(Date.class), processStartTime, processEndTime));
		}
//		if (mqIntegrationPoint != null && !"".equals(mqIntegrationPoint)) {
//			restrictions = criteriaBuilder.and(restrictions,
//					criteriaBuilder.equal(root.get("mqIntegerationPoint").as(String.class), mqIntegrationPoint));
//			restrictionsCount = critBuilderCount.and(restrictionsCount,
//					critBuilderCount.equal(root.get("mqIntegerationPoint").as(String.class), mqIntegrationPoint));
//
//		}

		criteriaQuery.where(restrictions);
		criteriaQuery.orderBy(criteriaBuilder.desc(root.get("insertTime")));
		Query query = TransactionCompensatingAutoConfig.getEntityManager().createQuery(criteriaQuery);

		query.setFirstResult((pageIndex-1)*pageSize).setMaxResults(pageSize);

		List<TxInCommandEntity> list = query.getResultList();

		critQueryCount.where(restrictionsCount);
		PageRequest pageRequest =  PageRequest.of(pageIndex,pageSize,null);
		
		return getPage(list, pageRequest,() -> TransactionCompensatingAutoConfig
				.getEntityManager().createQuery(critQueryCount).getSingleResult().intValue());
	}
	
	   public static <T> Page <T> getPage(List <T> content, Pageable pageable, PageableExecutionUtils.TotalSupplier totalSupplier) {
	        return new PageImpl<T>(content, pageable, totalSupplier.get());
	    }

}
