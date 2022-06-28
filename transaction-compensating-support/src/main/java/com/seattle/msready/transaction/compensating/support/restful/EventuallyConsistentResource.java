package com.seattle.msready.transaction.compensating.support.restful;


import com.seattle.msready.transaction.compensating.support.api.TransactionEventuallyConsistentService;
import com.seattle.msready.transaction.compensating.support.dto.TxOutCommandDto;
import com.seattle.msready.transaction.compensating.support.entity.TxInCommandEntity;
import com.seattle.msready.transaction.compensating.support.entity.TxOutCommandEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/tx")
public class EventuallyConsistentResource {

	@Autowired
	private TransactionEventuallyConsistentService eventuallyConsistentService;

	@RequestMapping(value = "/v1/out/redo", method = RequestMethod.POST)
	public void redoOutCommand(@RequestParam("messageUUID") String messageUUID) {
		eventuallyConsistentService.redoOutCommand(messageUUID);
	}

	@RequestMapping(value = "/v1/out/markResponded", method = RequestMethod.POST)
	public boolean markOutCommandResponded(@RequestParam("messageUUID") String messageUUID) {
		return eventuallyConsistentService.markOutCommandResponded(messageUUID);
	}
	
	@RequestMapping(value = "/v1/in/redo", method = RequestMethod.POST)
	public void redoInCommand(@RequestParam("messageUUID") String messageUUID) {
		eventuallyConsistentService.redoInCommand(messageUUID);
	}

	@RequestMapping(value = "/v1/in/markSuccessed", method = RequestMethod.POST)
	public boolean markInCommandSuccessed(@RequestParam("messageUUID") String messageUUID) {
		return eventuallyConsistentService.markInCommandSuccessed(messageUUID);
	}

	@RequestMapping(value = "/v1/out/searchTxOutCommandList", method = RequestMethod.GET)
	public Page<TxOutCommandEntity> getAllTxOutCommandList(@RequestParam("pageNo") Integer pageNo,
														   @RequestParam("pageSize") Integer pageSize,
														   @RequestParam(value = "isResponded", required = false) String isResponded,
														   @RequestParam(value = "mqIntegrationPoint", required = false) String mqIntegrationPoint,
														   @RequestParam(value = "globalTransactionId", defaultValue = "0") String globalTransactionId,
														   @RequestParam(value = "transactionType", defaultValue = "0") Long transactionType,
														   @RequestParam(value = "commandUUID", required = false) String commandUUID,
														   @RequestParam(value = "processStartTime", required = false) Date processStartTime,
														   @RequestParam(value = "processEndTime", required = false) Date processEndTime) throws Exception {
//		Assert.isNotNull(isResponded, "isResponded is null.");
		// TODO
		/*
		 * RestResult restResult = new RestResult();
		 * 
		 * PagedResult<TxOutCommandEntity> txOutCommandEntity
		 * =eventuallyConsistentService.
		 * findByisResponded$CommandUUID$globalTransactionId$transactionType$transactionTime$pageIndex$pageSize
		 * (isResponded, globalTransactionId, transactionType, commandUUID,
		 * processStartTime, processEndTime,mqIntegrationPoint, pageNo,
		 * pageSize);
		 * 
		 * Map resultMap = new HashMap(); resultMap.put("element",
		 * txOutCommandEntity.getElementsInCurrentPage());
		 * resultMap.put("total", txOutCommandEntity.getTotalElements());
		 * resultMap.put("totalPages", txOutCommandEntity.getTotalPages());
		 * restResult.setStatus(Boolean.TRUE); restResult.setValue(resultMap);
		 */

		Page<TxOutCommandEntity> outCommandPage = eventuallyConsistentService
				.findByisResponded$CommandUUID$globalTransactionId$transactionType$transactionTime$pageIndex$pageSize(
						isResponded, globalTransactionId, transactionType, commandUUID, processStartTime,
						processEndTime, mqIntegrationPoint, pageNo, pageSize);

		return outCommandPage;
	}

	@RequestMapping(value = "/v1/out/searchTxOutCommandListBean", method = RequestMethod.POST)
	public List<TxOutCommandEntity> getAllTxOutCommandListBean(@RequestBody TxOutCommandDto txOutCommandDto)
			throws Exception {

		List<TxOutCommandEntity> valueList = new ArrayList<>();

		valueList = eventuallyConsistentService
				.findByisResponded$CommandUUID$globalTransactionId$transactionType$transactionTime(txOutCommandDto);

		return valueList;
	}

	@RequestMapping(value = "/v1/in/searchTxInCommandList", method = RequestMethod.GET)
	public Page<TxInCommandEntity> getAllTxInCommandList(@RequestParam("pageNo") Integer pageNo,
														 @RequestParam("pageSize") Integer pageSize,
														 @RequestParam(value = "isSuccessful", required = false) String isSuccessful,
														 @RequestParam(value = "globalTransactionId", defaultValue="0") String globalTransactionId,
														 @RequestParam(value = "transactionType", defaultValue="0") Long transactionType,
														 @RequestParam(value = "commandUUID", required = false) String commandUUID,
														 @RequestParam(value = "processStartTime", required = false) Date processStartTime,
														 @RequestParam(value = "processEndTime", required = false) Date processEndTime) throws Exception {
//		Assert.isNotNull(isSuccessful, "isSuccessful is null.");

		Page<TxInCommandEntity> inCommandPage = eventuallyConsistentService
				.findByisSuccessful$CommandUUID$globalTransactionId$transactionType$transactionTime$pageIndex$pageSize(
						isSuccessful, globalTransactionId, transactionType, commandUUID, processStartTime,
						processEndTime, pageNo, pageSize);

		return inCommandPage;
	}
}
