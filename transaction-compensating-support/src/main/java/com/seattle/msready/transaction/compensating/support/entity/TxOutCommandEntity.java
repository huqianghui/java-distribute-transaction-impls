package com.seattle.msready.transaction.compensating.support.entity;


import org.hibernate.annotations.Proxy;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "T_PUB_TX_OUT_COMMAND")
@Proxy(lazy = false)
public class TxOutCommandEntity implements Serializable {

    @Column(name = "GLOBAL_TRANSACTION_ID")
    private String globalTransactionId;

    @Column(name = "IS_RESPONDED")
    private String isResponded = "N";

    @Id
    @Column(name = "COMMAND_UUID")
    private String commandUUID;

    @Column(name = "LAST_RESEND_TIME")
    private Date lastResendTime;

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

    @Column(name = "NEED_RESPONSE")
    private String needResponse = "Y";

    @Column(name = "RESEND_TIMES")
    private Integer resendTimes = 0;

    @Column(name = "RESPONSE_TIME")
    private Date responseTime;

    @Column(name = "TRANSACTION_TIME")
    private Date transactionTime;

    @Column(name = "TRANSACTION_TYPE")
    private Long transactionType;

    @Column(name = "OUT_COMMAND_CLASS")
    private String outCommandClass;

    @Column(name = "INSERT_TIME")
    private Date insertTime;

    @Column(name = "UPDATE_TIME")
    private Date updateTime;

    @Column(name = "APP_NAME")
    private String appName;

    @Column(name = "ERROR_MESSAGE")
    private String errorMessage;

    public String getPrimaryKey() {
        return this.getCommandUUID();
    }

    public void setPrimaryKey(String primaryKey) {
        this.setCommandUUID(primaryKey);
    }

    @PrePersist
    protected void prePersist() {
        insertTime = new Date();
    }

    @PreUpdate
    protected void preUpdate() {
        updateTime = new Date();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getOutCommandClass() {
        return outCommandClass;
    }

    public void setOutCommandClass(String outCommandClass) {
        this.outCommandClass = outCommandClass;
    }

    public String getGlobalTransactionId() {
        return globalTransactionId;
    }

    public String getIsResponded() {
        return isResponded;
    }

    public Date getLastResendTime() {
        return lastResendTime;
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

    public String getNeedResponse() {
        return needResponse;
    }

    public Integer getResendTimes() {
        return resendTimes;
    }

    public Date getResponseTime() {
        return responseTime;
    }

    public Date getTransactionTime() {
        return transactionTime;
    }

    public Long getTransactionType() {
        return transactionType;
    }

    public void setGlobalTransactionId(String globalTransactionId) {
        this.globalTransactionId = globalTransactionId;
    }

    public void setIsResponded(String isResponded) {
        this.isResponded = isResponded;
    }

    public void setLastResendTime(Date lastResendTime) {
        this.lastResendTime = lastResendTime;
    }

    public void setCommandData(String commandData) {
        this.commandData = commandData;
    }

    public void setNeedResponse(String needResponse) {
        this.needResponse = needResponse;
    }

    public void setResendTimes(Integer resendTimes) {
        this.resendTimes = resendTimes;
    }

    public void setResponseTime(Date responseTime) {
        this.responseTime = responseTime;
    }

    public void setTransactionTime(Date transactionTime) {
        this.transactionTime = transactionTime;
    }

    public void setTransactionType(Long transactionType) {
        this.transactionType = transactionType;
    }

    public String getCommandUUID() {
        return commandUUID;
    }

    public void setCommandUUID(String commandUUID) {
        this.commandUUID = commandUUID;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
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

