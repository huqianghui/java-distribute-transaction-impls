package com.seattle.msready.mq.transaction.compensating.mq.domain;

import java.io.Serializable;
import java.util.Collection;
import java.util.Locale;
import java.util.TimeZone;

public class AppUser implements Serializable {

    private static final long serialVersionUID = 5575108098573199425L;

    private Long userId;

    //user's login name
    private String userName;


    private String password;

    //user's real name
    private String realName;

    //user's department id
    private String deptId;

    //user's org id
    private Long orgId;

    //user's language id
    private String langId;

    //client ip address of user's browser
    private String clientIp;

    private Integer position;

    private Integer userType;

    private Collection <Long> userRoleIds;

    private Collection <Long> accessOrgIds;

    private Collection<Long> accessModuleIds;

    private TimeZone orgTimeZone;

    private Integer orgTimeOffsetDays;


    private Long partyId;


    private Locale locale;

    private String email;

    private String dataAccessTokenFLag;

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public String getLangId() {
        return langId;
    }

    public void setLangId(String langId) {
        this.langId = langId;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public Collection <Long> getUserRoleIds() {
        return userRoleIds;
    }

    public void setUserRoleIds(Collection <Long> userRoleIds) {
        this.userRoleIds = userRoleIds;
    }

    public Collection <Long> getAccessOrgIds() {
        return accessOrgIds;
    }

    public void setAccessOrgIds(Collection <Long> accessOrgIds) {
        this.accessOrgIds = accessOrgIds;
    }

    public Collection <Long> getAccessModuleIds() {
        return accessModuleIds;
    }

    public void setAccessModuleIds(Collection <Long> accessModuleIds) {
        this.accessModuleIds = accessModuleIds;
    }

    public TimeZone getOrgTimeZone() {
        return orgTimeZone;
    }

    public void setOrgTimeZone(TimeZone orgTimeZone) {
        this.orgTimeZone = orgTimeZone;
    }

    public Integer getOrgTimeOffsetDays() {
        return orgTimeOffsetDays;
    }

    public void setOrgTimeOffsetDays(Integer orgTimeOffSet) {
        this.orgTimeOffsetDays = orgTimeOffSet;
    }

    public Long getPartyId() {
        return partyId;
    }

    public void setPartyId(Long partyId) {
        this.partyId = partyId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getDataAccessTokenFLag() {
        return dataAccessTokenFLag;
    }

    public void setDataAccessTokenFLag(String dataAccessTokenFLag) {
        this.dataAccessTokenFLag = dataAccessTokenFLag;
    }

}