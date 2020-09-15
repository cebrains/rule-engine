package com.uama.microservices.provider.ruleengine.model.iot;

import java.util.Date;
import javax.persistence.*;

@Table(name = "iot_rule_node")
public class IotRuleNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    /**
     * 规则链id rule_chain.id
     */
    @Column(name = "rule_chain_id")
    private String ruleChainId;

    /**
     * 附加信息 json格式{}
     */
    @Column(name = "node_additional_info")
    private String nodeAdditionalInfo;

    /**
     * 节点的所有自定义配置信息 json格式{}
     */
    @Column(name = "node_configuration")
    private String nodeConfiguration;

    /**
     * 节点类型
     */
    @Column(name = "node_type")
    private String nodeType;

    /**
     * 节点名称
     */
    @Column(name = "node_name")
    private String nodeName;

    /**
     * 添加时间
     */
    private Date intime;

    /**
     * 添加人
     */
    private String inuser;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 更新人
     */
    @Column(name = "update_user")
    private String updateUser;

    /**
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取规则链id rule_chain.id
     *
     * @return rule_chain_id - 规则链id rule_chain.id
     */
    public String getRuleChainId() {
        return ruleChainId;
    }

    /**
     * 设置规则链id rule_chain.id
     *
     * @param ruleChainId 规则链id rule_chain.id
     */
    public void setRuleChainId(String ruleChainId) {
        this.ruleChainId = ruleChainId;
    }

    /**
     * 获取附加信息 json格式{}
     *
     * @return node_additional_info - 附加信息 json格式{}
     */
    public String getNodeAdditionalInfo() {
        return nodeAdditionalInfo;
    }

    /**
     * 设置附加信息 json格式{}
     *
     * @param nodeAdditionalInfo 附加信息 json格式{}
     */
    public void setNodeAdditionalInfo(String nodeAdditionalInfo) {
        this.nodeAdditionalInfo = nodeAdditionalInfo;
    }

    /**
     * 获取节点的所有自定义配置信息 json格式{}
     *
     * @return node_configuration - 节点的所有自定义配置信息 json格式{}
     */
    public String getNodeConfiguration() {
        return nodeConfiguration;
    }

    /**
     * 设置节点的所有自定义配置信息 json格式{}
     *
     * @param nodeConfiguration 节点的所有自定义配置信息 json格式{}
     */
    public void setNodeConfiguration(String nodeConfiguration) {
        this.nodeConfiguration = nodeConfiguration;
    }

    /**
     * 获取节点类型
     *
     * @return node_type - 节点类型
     */
    public String getNodeType() {
        return nodeType;
    }

    /**
     * 设置节点类型
     *
     * @param nodeType 节点类型
     */
    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * 获取节点名称
     *
     * @return node_name - 节点名称
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * 设置节点名称
     *
     * @param nodeName 节点名称
     */
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * 获取添加时间
     *
     * @return intime - 添加时间
     */
    public Date getIntime() {
        return intime;
    }

    /**
     * 设置添加时间
     *
     * @param intime 添加时间
     */
    public void setIntime(Date intime) {
        this.intime = intime;
    }

    /**
     * 获取添加人
     *
     * @return inuser - 添加人
     */
    public String getInuser() {
        return inuser;
    }

    /**
     * 设置添加人
     *
     * @param inuser 添加人
     */
    public void setInuser(String inuser) {
        this.inuser = inuser;
    }

    /**
     * 获取更新时间
     *
     * @return update_time - 更新时间
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 设置更新时间
     *
     * @param updateTime 更新时间
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 获取更新人
     *
     * @return update_user - 更新人
     */
    public String getUpdateUser() {
        return updateUser;
    }

    /**
     * 设置更新人
     *
     * @param updateUser 更新人
     */
    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }
}