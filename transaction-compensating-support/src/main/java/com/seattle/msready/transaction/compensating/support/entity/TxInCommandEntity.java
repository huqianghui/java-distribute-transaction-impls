package com.seattle.msready.transaction.compensating.support.entity;


import org.hibernate.annotations.Proxy;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "T_PUB_TX_IN_COMMAND")
@Proxy(lazy = false)
public class TxInCommandEntity implements Serializable {

    @Id
    @Column(name = "COMMAND_UUID")
    private String commandUUID;

    @Column(name = "PROCESS_TIME")
    private Date processTime;

    @Transient
    private String commandData;

    @Column(name = "COMMAND_DATA_1")
    private String commandData1;
    @Column(name = "COMMAND_DATA_2")
    private String commandData2;
    @Column(name = "COMMAND_DATA_3")
    private String commandData3;
    @Column(name = "COMMAND_DATA_4")
    private String commandData4;
    @Column(name = "COMMAND_DATA_5")
    private String commandData5;

    @Column(name = "FILE_PATH")
    private String filePath;

    @Column(name = "GLOBAL_TRANSACTION_ID")
    private String globalTransactionId;

    @Column(name = "NEED_RESPONSE")
    private String needResponse = "Y";

    @Column(name = "TRANSACTION_TYPE")
    private Long transactionType;

    @Column(name = "ERROR_MESSAGE")
    private String errorMessage;

    @Column(name = "IS_SUCCESSFUL")
    private String isSuccessful;
    @Column(name = "INSERT_TIME")
    private Date insertTime;
    @Column(name = "UPDATE_TIME")
    private Date updateTime;

    @Column(name = "IN_COMMAND_CLASS")
    private String inCommandClass;

    @Column(name = "APP_NAME")
    private String appName;

    @Column(name = "SOURCE_APP_NAME")
    private String sourceAppName;


    public String getPrimaryKey() {
        return this.getCommandUUID();
    }

    public void setPrimaryKey(String primaryKey) {
        this.setCommandUUID(primaryKey);
    }

    public String getSequenceName() {
        return SequenceEnum.S_SHORT_UID.value();
    }

    @PrePersist
    protected void prePersist() {
        insertTime = new Date();
    }

    @PreUpdate
    protected void preUpdate() {
        updateTime = new Date();
    }

    public String getSourceAppName() {
        return sourceAppName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setSourceAppName(String sourceAppName) {
        this.sourceAppName = sourceAppName;
    }

    public Date getProcessTime() {
        return processTime;
    }

    public void setProcessTime(Date processTime) {
        this.processTime = processTime;
    }

    public String getCommandUUID() {
        return commandUUID;
    }

    public void setCommandUUID(String commandUUID) {
        this.commandUUID = commandUUID;
    }

    public String getCommandData() {
        if (!StringUtils.isEmpty(this.commandData)) {
            return this.commandData;
        } else if (!StringUtils.isEmpty(this.commandData1)) {
            this.commandData = this.commandData1 + this.commandData2 + this.commandData3 + this.commandData4 + this.commandData5;
            return this.commandData;
        } else if (!StringUtils.isEmpty(this.filePath)) {
            //TODO file process
            /*try {
                this.commandData = FileUtils.readFileToString(new File(this.filePath));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }*/
        }
        return commandData;
    }

    public void setCommandData(String commandData) {
        this.commandData = commandData;
    }

    public String getGlobalTransactionId() {
        return globalTransactionId;
    }

    public void setGlobalTransactionId(String globalTransactionId) {
        this.globalTransactionId = globalTransactionId;
    }

    public String getNeedResponse() {
        return needResponse;
    }

    public void setNeedResponse(String needResponse) {
        this.needResponse = needResponse;
    }

    public Long getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(Long transactionType) {
        this.transactionType = transactionType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getIsSuccessful() {
        return isSuccessful;
    }

    public void setIsSuccessful(String isSuccessful) {
        this.isSuccessful = isSuccessful;
    }

    public String getInCommandClass() {
        return inCommandClass;
    }

    public void setInCommandClass(String inCommandClass) {
        this.inCommandClass = inCommandClass;
    }

    public String getCommandData1() {
        return commandData1;
    }

    public void setCommandData1(String commandData1) {
        this.commandData1 = commandData1;
    }

    public String getCommandData2() {
        return commandData2;
    }

    public void setCommandData2(String commandData2) {
        this.commandData2 = commandData2;
    }

    public String getCommandData3() {
        return commandData3;
    }

    public void setCommandData3(String commandData3) {
        this.commandData3 = commandData3;
    }

    public String getCommandData4() {
        return commandData4;
    }

    public void setCommandData4(String commandData4) {
        this.commandData4 = commandData4;
    }

    public String getCommandData5() {
        return commandData5;
    }

    public void setCommandData5(String commandData5) {
        this.commandData5 = commandData5;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}

