package com.uama.microservices.provider.ruleengine.model.iot;

import java.util.Date;
import javax.persistence.*;

@Table(name = "iot_rule_relation")
public class IotRuleRelation {
    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    /**
     * 来源id rule_chain.id或rule_node.id
     */
    @Column(name = "from_id")
    private String fromId;

    /**
     * 来源类型
     */
    @Column(name = "from_type")
    private String fromType;

    /**
     * 目标id rule_chain.id或rule_node.id
     */
    @Column(name = "to_id")
    private String toId;

    /**
     * 目标类型
     */
    @Column(name = "to_type")
    private String toType;

    /**
     * 连接类型
     */
    @Column(name = "relation_type")
    private String relationType;

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
    public Integer getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取来源id rule_chain.id或rule_node.id
     *
     * @return from_id - 来源id rule_chain.id或rule_node.id
     */
    public String getFromId() {
        return fromId;
    }

    /**
     * 设置来源id rule_chain.id或rule_node.id
     *
     * @param fromId 来源id rule_chain.id或rule_node.id
     */
    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    /**
     * 获取来源类型
     *
     * @return from_type - 来源类型
     */
    public String getFromType() {
        return fromType;
    }

    /**
     * 设置来源类型
     *
     * @param fromType 来源类型
     */
    public void setFromType(String fromType) {
        this.fromType = fromType;
    }

    /**
     * 获取目标id rule_chain.id或rule_node.id
     *
     * @return to_id - 目标id rule_chain.id或rule_node.id
     */
    public String getToId() {
        return toId;
    }

    /**
     * 设置目标id rule_chain.id或rule_node.id
     *
     * @param toId 目标id rule_chain.id或rule_node.id
     */
    public void setToId(String toId) {
        this.toId = toId;
    }

    /**
     * 获取目标类型
     *
     * @return to_type - 目标类型
     */
    public String getToType() {
        return toType;
    }

    /**
     * 设置目标类型
     *
     * @param toType 目标类型
     */
    public void setToType(String toType) {
        this.toType = toType;
    }

    /**
     * 获取连接类型
     *
     * @return relation_type - 连接类型
     */
    public String getRelationType() {
        return relationType;
    }

    /**
     * 设置连接类型
     *
     * @param relationType 连接类型
     */
    public void setRelationType(String relationType) {
        this.relationType = relationType;
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