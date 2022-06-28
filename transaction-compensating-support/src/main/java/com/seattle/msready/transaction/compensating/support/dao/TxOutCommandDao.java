package com.seattle.msready.transaction.compensating.support.dao;


import com.seattle.msready.transaction.compensating.support.utils.PageableExecutionUtils;
import com.seattle.msready.transaction.compensating.support.config.TransactionCompensatingAutoConfig;
import com.seattle.msready.transaction.compensating.support.dto.TxOutCommandDto;
import com.seattle.msready.transaction.compensating.support.entity.TxInCommandEntity;
import com.seattle.msready.transaction.compensating.support.entity.TxOutCommandEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.List;

@Repository
public class TxOutCommandDao {

    public TxOutCommandEntity findByCommandUUID(String commandUUID) {
        String sql = "from TxOutCommandEntity where commandUUID=:commandUUID";
        Query query = TransactionCompensatingAutoConfig.getEntityManager().createQuery(sql, TxOutCommandEntity.class);
        query.setParameter("commandUUID", commandUUID);
        List<TxOutCommandEntity> list = query.getResultList();
        if (CollectionUtils.isEmpty(list) || list.size() == 0) {
            return null;
        } else if (list.size() == 1) {
            return list.get(0);
        } else {
            throw new RuntimeException("Found too many TxOutCommandEntity for commandUUID: " + commandUUID);
        }
    }

    private EntityManager getEntityManager() {
        return TransactionCompensatingAutoConfig.getApplicationContext().getBean(EntityManager.class);
    }

    /**
     * IS_RESPONSED
     * TRANSACTION_TYPE
     * COMMAND_UUID
     * GLOBAL_TRANSACTION_ID
     * TRANSACTION_TIME
     *
     * @param txOutCommandDto
     * @return
     */
    public List<TxOutCommandEntity> findByisResponded$CommandUUID$globalTransactionId$transactionType$transactionTime(TxOutCommandDto txOutCommandDto) {

        CriteriaBuilder criteriaBuilder = TransactionCompensatingAutoConfig.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<TxOutCommandEntity> criteriaQuery = criteriaBuilder
                .createQuery(TxOutCommandEntity.class);
        Root<TxOutCommandEntity> root = criteriaQuery.from(TxOutCommandEntity.class);
        criteriaQuery.select(root);

        Predicate restrictions = criteriaBuilder.conjunction();

        if (txOutCommandDto.getIsResponded() != null && !"".equals(txOutCommandDto.getIsResponded())) {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("isResponded").as(String.class), txOutCommandDto.getIsResponded()));
        }

        if (txOutCommandDto.getCommandUUID() != null && !"".equals(txOutCommandDto.getCommandUUID())) {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("commandUUID").as(String.class), txOutCommandDto.getCommandUUID()));
        }
        if (txOutCommandDto.getGlobalTransactionId() != 0) {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("globalTransactionId").as(long.class), txOutCommandDto.getGlobalTransactionId()));
        }
        if (txOutCommandDto.getTransactionType() != 0) {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("transactionType").as(long.class), txOutCommandDto.getTransactionType()));
        }
        if (txOutCommandDto.getProcessStartTime() != null && txOutCommandDto.getProcessEndTime() != null) {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.between(root.get("transactionTime").as(Date.class), txOutCommandDto.getProcessStartTime(), txOutCommandDto.getProcessEndTime()));
        }
        criteriaQuery.where(restrictions);
        criteriaQuery.orderBy(criteriaBuilder.desc(root.get("transactionTime")));
        Query query = TransactionCompensatingAutoConfig.getEntityManager().createQuery(criteriaQuery);
        List<TxOutCommandEntity> list = query.getResultList();

        if (!CollectionUtils.isEmpty(list) || list.size() == 0) {
            return null;
        } else if (list.size() >= 1) {
            return list;
        } else {
            throw new RuntimeException("Found too many TxOutCommandEntity for findByisResponded$CommandUUID$globalTransactionId$transactionType$transactionTime: ");
        }
    }

    /**
     * IS_SUCCESSFUL
     * TRANSACTION_TYPE
     * COMMAND_UUID
     * GLOBAL_TRANSACTION_ID
     * TRANSACTION_TIME
     *
     * @param commandUUID
     * @return
     */
    public List<TxInCommandEntity> findByisSuccessful$CommandUUID$globalTransactionId$transactionType$transactionTime(String isSuccessful, long globalTransactionId,
                                                                                                                      long transactionType, String commandUUID, Date processStartTime, Date processEndTime) {

        CriteriaBuilder criteriaBuilder = TransactionCompensatingAutoConfig.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<TxOutCommandEntity> criteriaQuery = criteriaBuilder
                .createQuery(TxOutCommandEntity.class);
        Root<TxOutCommandEntity> root = criteriaQuery.from(TxOutCommandEntity.class);
        criteriaQuery.select(root);

        Predicate restrictions = criteriaBuilder.conjunction();

        if (isSuccessful != null && !"".equals(isSuccessful)) {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("isSuccessful").as(String.class), isSuccessful));
        }

        if (commandUUID != null && !"0".equals(commandUUID)) {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.like(root.get("commandUUID").as(String.class), commandUUID));
        }
        if (globalTransactionId != 0) {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("globalTransactionId").as(long.class), globalTransactionId));
        }
        if (transactionType != 0) {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("transactionType").as(long.class), transactionType));
        }
        if (processStartTime != null && processEndTime != null) {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.between(root.get("transactionTime").as(Date.class), processStartTime, processEndTime));
        }

        criteriaQuery.where(restrictions);
        criteriaQuery.orderBy(criteriaBuilder.desc(root.get("transactionTime")));
        Query query = TransactionCompensatingAutoConfig.getEntityManager().createQuery(criteriaQuery);

        List<TxInCommandEntity> list = query.getResultList();
        return getMutiRecords(list);
    }

    private TxOutCommandEntity getSingleRecord(List<TxOutCommandEntity> records) {
        if (!CollectionUtils.isEmpty(records) || records.size() == 0) {
            return null;
        } else if (records.size() >= 1) {
            return records.get(0);
        }
        return null;
    }

    private List<TxInCommandEntity> getMutiRecords(List<TxInCommandEntity> records) {
        if (!CollectionUtils.isEmpty(records) || records.size() == 0) {
            return null;
        } else if (records.size() >= 1) {
            return records;
        }
        return null;
    }

    /**
     * IS_SUCCESSFUL
     * TRANSACTION_TYPE
     * COMMAND_UUID
     * GLOBAL_TRANSACTION_ID
     * TRANSACTION_TIME
     *
     * @param commandUUID
     * @return
     */
    public List<TxOutCommandEntity> findByisResponded$CommandUUID$globalTransactionId$transactionType$transactionTime(String isResponded, long globalTransactionId, long transactionType, String commandUUID,
                                                                                                                      Date processStartTime, Date processEndTime, String mqIntegrationPoint) {
        CriteriaBuilder criteriaBuilder = TransactionCompensatingAutoConfig.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<TxOutCommandEntity> criteriaQuery = criteriaBuilder
                .createQuery(TxOutCommandEntity.class);
        Root<TxOutCommandEntity> root = criteriaQuery.from(TxOutCommandEntity.class);
        criteriaQuery.select(root);

        Predicate restrictions = criteriaBuilder.conjunction();

        if (isResponded != null && !"".equals(isResponded)) {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("isResponded").as(String.class), isResponded));
        }

        if (commandUUID != null && !"0".equals(commandUUID)) {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.like(root.get("commandUUID").as(String.class), commandUUID));
        }
        if (globalTransactionId != 0) {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("globalTransactionId").as(long.class), globalTransactionId));
        }
        if (transactionType != 0) {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("transactionType").as(long.class), transactionType));
        }
        if (processStartTime != null && processEndTime != null) {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.between(root.get("transactionTime").as(Date.class), processStartTime, processEndTime));
        }
        if (mqIntegrationPoint != null && !"".equals(mqIntegrationPoint)) {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("mqIntegerationPoint").as(String.class), mqIntegrationPoint));

        }

        criteriaQuery.where(restrictions);
        criteriaQuery.orderBy(criteriaBuilder.desc(root.get("transactionTime")));
        Query query = TransactionCompensatingAutoConfig.getEntityManager().createQuery(criteriaQuery);
        List<TxOutCommandEntity> list = query.getResultList();

        if (!CollectionUtils.isEmpty(list) || list.size() == 0) {
            return null;
        } else if (list.size() >= 1) {
            return list;
        } else {
            throw new RuntimeException("Found too many TxOutCommandEntity for findByisSuccessful$CommandUUID$globalTransactionId$transactionType$transactionTime: ");
        }
    }

    /**
     * IS_SUCCESSFUL
     * TRANSACTION_TYPE
     * COMMAND_UUID
     * GLOBAL_TRANSACTION_ID
     * TRANSACTION_TIME
     *
     * @param commandUUID
     * @return
     */
    public Page<TxOutCommandEntity> findByisResponded$CommandUUID$globalTransactionId$transactionType$transactionTime$pageIndex$pageSize(String isResponded, String globalTransactionId, long transactionType, String commandUUID,
                                                                                                                                         Date processStartTime, Date processEndTime, String mqIntegrationPoint, Integer pageIndex, Integer pageSize) {

        CriteriaBuilder critBuilderCount = TransactionCompensatingAutoConfig.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> critQueryCount = critBuilderCount.createQuery(Long.class);
        Root<TxOutCommandEntity> rootCount = critQueryCount.from(TxOutCommandEntity.class);
        critQueryCount.select(critBuilderCount.countDistinct(rootCount));


        CriteriaBuilder criteriaBuilder = TransactionCompensatingAutoConfig.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<TxOutCommandEntity> criteriaQuery = criteriaBuilder
                .createQuery(TxOutCommandEntity.class);
        Root<TxOutCommandEntity> root = criteriaQuery.from(TxOutCommandEntity.class);
        criteriaQuery.select(root);

        Predicate restrictions = criteriaBuilder.conjunction();
        Predicate restrictionsCount = critBuilderCount.conjunction();

        if (isResponded != null && !"".equals(isResponded)) {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("isResponded").as(String.class), isResponded));
            restrictionsCount = critBuilderCount.and(restrictionsCount, critBuilderCount.equal(root.get("isResponded").as(String.class), isResponded));
        }

        if (commandUUID != null && !"0".equals(commandUUID)) {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.like(root.get("commandUUID").as(String.class), commandUUID));
            restrictionsCount = critBuilderCount.and(restrictionsCount, critBuilderCount.like(root.get("commandUUID").as(String.class), commandUUID));
        }
        if (!"0".equals(globalTransactionId)) {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("globalTransactionId").as(String.class), globalTransactionId));
            restrictionsCount = critBuilderCount.and(restrictionsCount, critBuilderCount.equal(root.get("globalTransactionId").as(String.class), globalTransactionId));
        }
        if (transactionType != 0) {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("transactionType").as(long.class), transactionType));
            restrictionsCount = critBuilderCount.and(restrictionsCount, critBuilderCount.equal(root.get("transactionType").as(long.class), transactionType));
        }
        if (processStartTime != null && processEndTime != null) {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.between(root.get("transactionTime").as(Date.class), processStartTime, processEndTime));
            restrictionsCount = critBuilderCount.and(restrictionsCount, critBuilderCount.between(root.get("transactionTime").as(Date.class), processStartTime, processEndTime));
        }
        if (mqIntegrationPoint != null && !"".equals(mqIntegrationPoint)) {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("mqIntegerationPoint").as(String.class), mqIntegrationPoint));
            restrictionsCount = critBuilderCount.and(restrictionsCount, critBuilderCount.equal(root.get("mqIntegerationPoint").as(String.class), mqIntegrationPoint));

        }

        criteriaQuery.where(restrictions);
        criteriaQuery.orderBy(criteriaBuilder.desc(root.get("transactionTime")));
        Query query = TransactionCompensatingAutoConfig.getEntityManager().createQuery(criteriaQuery);

        query.setFirstResult((pageIndex-1)*pageSize).setMaxResults(pageSize);

        List<TxOutCommandEntity> list = query.getResultList();

        critQueryCount.where(restrictionsCount);
        PageRequest pageRequest =  PageRequest.of(pageIndex,pageSize,null);
        return getPage(list, pageRequest,() -> TransactionCompensatingAutoConfig
                .getEntityManager().createQuery(critQueryCount).getSingleResult().intValue());
    }
    
    public static <T> Page <T> getPage(List <T> content, Pageable pageable, PageableExecutionUtils.TotalSupplier totalSupplier) {
        return new PageImpl<>(content, pageable, totalSupplier.get());
    }
}
