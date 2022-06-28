package com.seattle.msready.transaction.compensating.support.impl;



import com.seattle.msready.transaction.compensating.support.config.TransactionCompensatingAutoConfig;
import com.seattle.msready.transaction.compensating.support.api.EventuallyConsistentService;
import com.seattle.msready.transaction.compensating.support.api.TransactionEventuallyConsistentService;
import com.seattle.msready.transaction.compensating.support.dao.TxInCommandDao;
import com.seattle.msready.transaction.compensating.support.dao.TxOutCommandDao;
import com.seattle.msready.transaction.compensating.support.dto.TxOutCommandDto;
import com.seattle.msready.transaction.compensating.support.entity.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.persistence.Query;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(TransactionEventuallyConsistentService.BEAN_NAME)
public class EventuallyConsistentServiceImpl implements TransactionEventuallyConsistentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventuallyConsistentServiceImpl.class);

    public final static String SPRING_BEAN_TX_MANAGER = "transactionManager";

    @Resource(name = TransactionEventuallyConsistentService.BEAN_NAME)
    private EventuallyConsistentService self;

    @Autowired
    private TxOutCommandDao txOutCommandDao;

    @Autowired
    private TxInCommandDao txInCommandDao;

    @Autowired(required = false)
    @Qualifier("restTemplateWithLoadBalance")
    private RestTemplate restTemplate = null;

    public static Logger getLogger() {
        return LOGGER;
    }

    @Value("${SourceSystemId}")
    private Integer SourceSystemId;
    @Value("${SourceSystemBaseURL}")
    private String SourceSystemBaseURL;

    @Value("#{'${transaction.out.command.file.path:}'}")
    private String outCommandFilePath;

    public Integer getSourceSystemId() {
        return SourceSystemId;
    }

    public void setSourceSystemId(Integer sourceSystemId) {
        SourceSystemId = sourceSystemId;
    }

    public String getSourceSystemBaseURL() {
        return SourceSystemBaseURL;
    }

    public void setSourceSystemBaseURL(String sourceSystemBaseURL) {
        SourceSystemBaseURL = sourceSystemBaseURL;
    }

    public void processOutCommand(AbstractTxOutCommand command) {
        // fill miss field
        prepareTxOutCommand(command);
        // Save command in db
        saveTxOutCommandToDb(command);
        // Register the command to spring transaction commit hook.
        registerOutCommonInSpringTransactionHook(command);

    }

    private void registerOutCommonInSpringTransactionHook(AbstractTxOutCommand command) {
        EventuallyConsistentOutTransactionSync sync = new EventuallyConsistentOutTransactionSync();
        sync.setOutCommand(command);
        TransactionSynchronizationManager.registerSynchronization(sync);
    }

    /**
     * fill missing field
     *
     * @param command
     */
    private void prepareTxOutCommand(AbstractTxOutCommand command) {
        LOGGER.debug("prepareTxOutCommand...");
        command.setTransactionTime(new Date());
        command.setCommandUUID(command.getCommandUUID());

        Map<String, Object> commandMessageInfo = new HashMap<String, Object>();
        commandMessageInfo.put(CommandInfo.CommandUUID.toString(), command.getCommandUUID());
        commandMessageInfo.put(CommandInfo.GlobalTransactionId.toString(), command.getGlobalTransactionId());
        commandMessageInfo.put(CommandInfo.TransactionType.toString(), command.getTransactionType());
        commandMessageInfo.put(CommandInfo.NeedResponse.toString(), command.getNeedResponse());
        commandMessageInfo.put(CommandInfo.SourceSystem.toString(), getSourceSystemId());
        commandMessageInfo.put(CommandInfo.SourceSystemBaseURL.toString(), getSourceSystemBaseURL());
        command.setCommandMessageInfo(commandMessageInfo);
    }

    private void saveTxOutCommandToDb(AbstractTxOutCommand command) {
        LOGGER.debug("saveTxOutCommandToDb...");
        TxOutCommandEntity commandEntity = new TxOutCommandEntity();
        commandEntity.setGlobalTransactionId(command.getGlobalTransactionId());
        commandEntity.setNeedResponse(command.getNeedResponse());
        commandEntity.setTransactionTime(command.getTransactionTime());
        commandEntity.setTransactionType(command.getTransactionType());
        commandEntity.setCommandUUID(command.getCommandUUID());
        commandEntity.setAppName(command.getSenderAppName());
        outCommandDataProcess(command, commandEntity);
        commandEntity.setOutCommandClass(command.getClass().getName());
        TransactionCompensatingAutoConfig.getEntityManager().persist(commandEntity);
    }

    private AbstractTxOutCommand convertOutCommandEntityToCommand(TxOutCommandEntity commandEntity) {
        LOGGER.debug("convertOutCommandEntityToCommand...");
        try {
            AbstractTxOutCommand outCommand = (AbstractTxOutCommand) Class.forName(commandEntity.getOutCommandClass())
                    .newInstance();
            LOGGER.debug("outCommand:{}", outCommand);
            outCommand.setGlobalTransactionId(commandEntity.getGlobalTransactionId());
            outCommand.setNeedResponse(commandEntity.getNeedResponse());
            outCommand.setTransactionTime(commandEntity.getTransactionTime());
            outCommand.setTransactionType(commandEntity.getTransactionType());
            outCommand.setCommandUUID(commandEntity.getCommandUUID());
            outCommand.setCommandDataAsJson(commandEntity.getCommandData());
            return outCommand;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void processInCommand(AbstractTxInCommand command) {

        // R1:从MQ中获取消息，先不删除消息
        TransactionTemplate transactionTemplate = new TransactionTemplate(TransactionCompensatingAutoConfig.getApplicationContext().getBean(SPRING_BEAN_TX_MANAGER, PlatformTransactionManager.class));
        transactionTemplate.setPropagationBehavior(Propagation.REQUIRES_NEW.value());

        try {
            // R2: 开始数据库事务
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                // R3: 检查是否已经处理该消息（源系统+交易ID,UNIQUE KEY），如果已经处理，则忽略R4~R5步骤<BR>
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {

                    TxInCommandEntity inCommandEntity = txInCommandDao.findByCommandUUID(command.getCommandUUID());

                    if (inCommandEntity == null) {
                        // R4: 写入该消息到本地数据库
                        TransactionCompensatingAutoConfig.getEntityManager().persist(convertInCommandToCommandEntity(command));
                        // R5: 处理该消息，执行业务操作
                        command.execute();
                    } else {
                        LOGGER.warn("Duplicate command,uuid:{}", command.getCommandUUID());
                    }
                }
                // R6: 提交数据库事务
            });

        } catch (Exception e) {

            e.printStackTrace();

            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    TxInCommandEntity inCommandEntity = txInCommandDao.findByCommandUUID(command.getCommandUUID());
                    if (inCommandEntity == null) {
                        TxInCommandEntity entity = convertInCommandToCommandEntity(command);
                        entity.setIsSuccessful("false");
                        entity.setErrorMessage(e.getMessage());
                        TransactionCompensatingAutoConfig.getEntityManager().persist(entity);
                    }
                }
            });
        }

        // R7: 删除消息 （此处删除消息，可以减少调用方的重发可能性）
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                command.deleteMessageAfterTxComplete();
            }
        });
        // R8: 通知调用发该交易已经处理，更新调用方消息状态。 如果调用方系统且无需确认/通知该消息已完成，则忽略此步骤。
        if ("Y".equals(command.getNeedResponse())) {
            // using rest api in source sytem, mark command received
            // TODO prepare src system id
            sendResponseToSourceSystem(command.getSourceAppName(), command.getCommandUUID());
        }

    }

    private TxInCommandEntity convertInCommandToCommandEntity(AbstractTxInCommand command) {
        LOGGER.debug("convertInCommandToCommandEntity...");
        TxInCommandEntity entity = new TxInCommandEntity();
        inCommandDataProcess(command, entity);
        entity.setCommandUUID(command.getCommandUUID());
        entity.setGlobalTransactionId(command.getGlobalTransactionId());
        entity.setInCommandClass(command.getClass().getName());
        entity.setIsSuccessful("true");
        entity.setNeedResponse(command.getNeedResponse());
        entity.setProcessTime(new Date());
        entity.setAppName(command.getRecAppName());
        entity.setSourceAppName(command.getSourceAppName());
        entity.setTransactionType(command.getTransactionType());
        entity.setSourceAppName(command.getSourceAppName());
        return entity;
    }

    private void outCommandDataProcess(AbstractTxOutCommand command, TxOutCommandEntity entity) {
        if (!StringUtils.isEmpty(command.getCommandDataAsJson()) && command.getCommandDataAsJson().length() <= 20000) {
            if (command.getCommandDataAsJson().length() <= 4000) {
                entity.setCommandData1(command.getCommandDataAsJson().substring(0));
            }else if (command.getCommandDataAsJson().length() > 4000 && command.getCommandDataAsJson().length() <= 8000) {
                entity.setCommandData2(command.getCommandDataAsJson().substring(4000));
            }else if (command.getCommandDataAsJson().length() > 8000 && command.getCommandDataAsJson().length() <= 12000) {
                entity.setCommandData3(command.getCommandDataAsJson().substring(8000));
            }else if (command.getCommandDataAsJson().length() > 12000 && command.getCommandDataAsJson().length() <= 16000) {
                entity.setCommandData4(command.getCommandDataAsJson().substring(12000));
            }else if (command.getCommandDataAsJson().length() > 16000) {
                entity.setCommandData5(command.getCommandDataAsJson().substring(16000));
            }
        } else if(!StringUtils.isEmpty(command.getCommandDataAsJson())){
            //TODO file system
            /*Assert.isTrue(outCommandFilePath!=null, "The directoryPath is null.");
            String filePath = outCommandFilePath + command.getCommandUUID() + ".json";
            try {
                FileUtils.writeStringToFile(new File(filePath), command.getCommandDataAsJson());
                entity.setFilePath(filePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }*/
        }
    }

    private void inCommandDataProcess(AbstractTxInCommand command, TxInCommandEntity entity) {
        if (!StringUtils.isEmpty(command.getCommandDataAsJson()) && command.getCommandDataAsJson().length() <= 20000) {
            if (command.getCommandDataAsJson().length() <= 4000) {
                entity.setCommandData1(command.getCommandDataAsJson().substring(0));
            }else if (command.getCommandDataAsJson().length() > 4000 && command.getCommandDataAsJson().length() <= 8000) {
                entity.setCommandData2(command.getCommandDataAsJson().substring(4000));
            }else if (command.getCommandDataAsJson().length() > 8000 && command.getCommandDataAsJson().length() <= 12000) {
                entity.setCommandData3(command.getCommandDataAsJson().substring(8000));
            }else if (command.getCommandDataAsJson().length() > 12000 && command.getCommandDataAsJson().length() <= 16000) {
                entity.setCommandData4(command.getCommandDataAsJson().substring(12000));
            }else if (command.getCommandDataAsJson().length() > 16000) {
                entity.setCommandData5(command.getCommandDataAsJson().substring(16000));
            }
        } else if(!StringUtils.isEmpty(command.getCommandDataAsJson())){
            //TODO file system
            /*Assert.isNotEmpty(outCommandFilePath, "The directoryPath is null.");
            String filePath = outCommandFilePath + command.getCommandUUID() + ".json";
            try {
                FileUtils.writeStringToFile(new File(filePath), command.getCommandDataAsJson());
                entity.setFilePath(filePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }*/
        }
    }

    /**
     * Send response to source system using command UUID,return false if command
     * already responsed, return true if successfully Used in service provider
     * side.
     *
     * @param baseUrl
     * @param commnadUUID
     * @return
     */

    private boolean sendResponseToSourceSystem(String baseUrl, String commnadUUID) {
        LOGGER.debug("Response base URL is :" + baseUrl);
        String url = baseUrl + "/public/tx/markResponded?messageUUID={messageUUID}";

        // set http headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Basic a2VybWl0Omtlcm1pdA==");

        // set url parameters to http request
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("messageUUID", commnadUUID);
        } catch (JSONException e) {
            throw new RuntimeException(e.getCause());
        }
        HttpEntity<String> requestEntity = new HttpEntity<String>(parameters.toString(), headers);

        // mark responded through resful services
        ResponseEntity<Boolean> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Boolean.class,
                commnadUUID);

        return response.getBody();

    }


    @Override
    public void redoOutCommand(String commandUUID) {
        LOGGER.debug("redoOutCommand...");
        LOGGER.debug("commandUUID:{}", commandUUID);
        TxOutCommandEntity commandEntity = txOutCommandDao.findByCommandUUID(commandUUID);
        if ("Y".equals(commandEntity.getIsResponded())) {
            throw new RuntimeException("Command [uuid=" + commandUUID + "] already responded!");
        }
        AbstractTxOutCommand outCommand = convertOutCommandEntityToCommand(commandEntity);
        outCommand.execute();

        commandEntity.setResendTimes(commandEntity.getResendTimes() + 1);
        commandEntity.setLastResendTime(new Date());
        if ("Y".equals(outCommand.getNeedResponse())) {
            // to re-execute command which doesn't require reply,should change
            // the responded flag to 'N'
            markOutCommandResponded(outCommand.getCommandUUID());
        }
    }

    @Override
    public boolean markOutCommandResponded(String commandUUID) {
        LOGGER.debug("markOutCommandResponded...");
        LOGGER.debug("commandUUID:{}", commandUUID);
        Query query = TransactionCompensatingAutoConfig.getEntityManager().createQuery(
                "update TxOutCommandEntity  set isResponded='Y',responseTime=:currentDate where commandUUID=:commandUuid");
        query.setParameter("commandUuid", commandUUID);
        query.setParameter("currentDate", new Date());
        query.executeUpdate();
        TransactionCompensatingAutoConfig.getEntityManager().flush();
        return true;
    }

    @Override
    public void markOutCommandErrorMessage(String commandUUID, Exception e) {
        TxOutCommandEntity commandEntity = txOutCommandDao.findByCommandUUID(commandUUID);
        commandEntity.setErrorMessage(e.getMessage());
        TransactionCompensatingAutoConfig.getEntityManager().persist(commandEntity);
    }

    @Override
    public List<TxOutCommandEntity> findByisResponded$CommandUUID$globalTransactionId$transactionType$transactionTime(
            TxOutCommandDto txOutCommandDto) {
        List<TxOutCommandEntity> list = txOutCommandDao
                .findByisResponded$CommandUUID$globalTransactionId$transactionType$transactionTime(txOutCommandDto);
        return list;
    }

    @Override
    public List<TxInCommandEntity> findByisSuccessful$CommandUUID$globalTransactionId$transactionType$transactionTime(
            String isSuccessful, long globalTransactionId, long transactionType, String commandUUID,
            Date processStartTime, Date processEndTime) {
        List<TxInCommandEntity> list = txOutCommandDao
                .findByisSuccessful$CommandUUID$globalTransactionId$transactionType$transactionTime(isSuccessful,
                        globalTransactionId, transactionType, commandUUID, processStartTime, processEndTime);
        return list;
    }

    @Override
    public List<TxOutCommandEntity> findByisResponded$CommandUUID$globalTransactionId$transactionType$transactionTime(
            String isResponded, long globalTransactionId, long transactionType, String commandUUID,
            Date processStartTime, Date processEndTime, String mqIntegrationPoint) {
        List<TxOutCommandEntity> list = txOutCommandDao
                .findByisResponded$CommandUUID$globalTransactionId$transactionType$transactionTime(isResponded,
                        globalTransactionId, transactionType, commandUUID, processStartTime, processEndTime,
                        mqIntegrationPoint);
        return list;
    }

    @Override
    public Page<TxOutCommandEntity> findByisResponded$CommandUUID$globalTransactionId$transactionType$transactionTime$pageIndex$pageSize(
            String isResponded, String globalTransactionId, long transactionType, String commandUUID,
            Date processStartTime, Date processEndTime, String mqIntegrationPoint, Integer pageIndex, Integer pageSize) {


        return txOutCommandDao
                .findByisResponded$CommandUUID$globalTransactionId$transactionType$transactionTime$pageIndex$pageSize(isResponded,
                        globalTransactionId, transactionType, commandUUID, processStartTime, processEndTime,
                        mqIntegrationPoint, pageIndex, pageSize);
    }

    @Override
    public Page<TxInCommandEntity> findByisSuccessful$CommandUUID$globalTransactionId$transactionType$transactionTime$pageIndex$pageSize(
            String isSuccessful, String globalTransactionId, long transactionType, String commandUUID,
            Date processStartTime, Date processEndTime, Integer pageIndex, Integer pageSize) {

        return txInCommandDao
                .findByisSuccessful$CommandUUID$globalTransactionId$transactionType$transactionTime$pageIndex$pageSize(isSuccessful,
                        globalTransactionId, transactionType, commandUUID, processStartTime, processEndTime,
                        pageIndex, pageSize);
    }

	@Override
	public void redoInCommand(String commandUUID) {
		 TxInCommandEntity commandEntity = txInCommandDao.findByCommandUUID(commandUUID);
	        if ("Y".equals(commandEntity.getIsSuccessful())) {
	            throw new RuntimeException("Command [uuid=" + commandUUID + "] already responded!");
	        }
	        AbstractTxInCommand inCommand = convertInCommandEntityToCommand(commandEntity);
	        inCommand.execute();

//	        commandEntity.setResendTimes(commandEntity.getResendTimes() + 1);
	        commandEntity.setInsertTime(new Date());
	        if ("N".equals(inCommand.getNeedResponse())) {
	            // to re-execute command which doesn't require reply,should change
	            // the responded flag to 'N'
	        	markInCommandSuccessed(inCommand.getCommandUUID());
	        }
	}

	private AbstractTxInCommand convertInCommandEntityToCommand(TxInCommandEntity commandEntity) {
		 try {
	            AbstractTxInCommand inCommand = (AbstractTxInCommand) Class.forName(commandEntity.getInCommandClass())
	                    .newInstance();
	            LOGGER.debug("InCommand:{}", inCommand);
	            inCommand.setGlobalTransactionId(commandEntity.getGlobalTransactionId());
	            inCommand.setNeedResponse(commandEntity.getNeedResponse());
//	            inCommand.setTransactionTime(commandEntity.getTransactionTime());
	            inCommand.setTransactionType(commandEntity.getTransactionType());
	            inCommand.setCommandUUID(commandEntity.getCommandUUID());
	            inCommand.setCommandDataAsJson(commandEntity.getCommandData());
	            return inCommand;
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
	}

	@Override
	public boolean markInCommandSuccessed(String commandUUID) {
		LOGGER.debug("markOutCommandResponded...");
        LOGGER.debug("commandUUID:{}", commandUUID);
        Query query = TransactionCompensatingAutoConfig.getEntityManager().createQuery(
                "update TxInCommandEntity  set isSuccessful='Y',insertTime=:currentDate where commandUUID=:commandUuid");
        query.setParameter("commandUuid", commandUUID);
        query.setParameter("currentDate", new Date());
        query.executeUpdate();
        TransactionCompensatingAutoConfig.getEntityManager().flush();
        return true;
	}

}
