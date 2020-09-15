package com.uama.microservices.provider.ruleengine.model.iot;

import java.util.Date;
import javax.persistence.*;

@Table(name = "iot_rule_chain")
public class IotRuleChain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    /**
     * 规则链名称
     */
    @Column(name = "chain_name")
    private String chainName;

    /**
     * 规则链input_node id (rule_node.id)
     */
    @Column(name = "first_node_id")
    private String firstNodeId;

    /**
     * 是否为root chain 0否 1是
     */
    private Byte root;

    /**
     * 归属id, 目前为产品id (iot_product_info.id)
     */
    @Column(name = "belong_to_id")
    private String belongToId;

    /**
     * 归属类型, 目前为PRODUCT_CHAIN
     */
    @Column(name = "belong_type")
    private String belongType;

    /**
     * 租户id
     */
    @Column(name = "tenant_id")
    private String tenantId;

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
     * 获取规则链名称
     *
     * @return chain_name - 规则链名称
     */
    public String getChainName() {
        return chainName;
    }

    /**
     * 设置规则链名称
     *
     * @param chainName 规则链名称
     */
    public void setChainName(String chainName) {
        this.chainName = chainName;
    }

    /**
     * 获取规则链input_node id (rule_node.id)
     *
     * @return first_node_id - 规则链input_node id (rule_node.id)
     */
    public String getFirstNodeId() {
        return firstNodeId;
    }

    /**
     * 设置规则链input_node id (rule_node.id)
     *
     * @param firstNodeId 规则链input_node id (rule_node.id)
     */
    public void setFirstNodeId(String firstNodeId) {
        this.firstNodeId = firstNodeId;
    }

    /**
     * 获取是否为root chain 0否 1是
     *
     * @return root - 是否为root chain 0否 1是
     */
    public Byte getRoot() {
        return root;
    }

    /**
     * 设置是否为root chain 0否 1是
     *
     * @param root 是否为root chain 0否 1是
     */
    public void setRoot(Byte root) {
        this.root = root;
    }

    /**
     * 获取归属id, 目前为产品id (iot_product_info.id)
     *
     * @return belong_to_id - 归属id, 目前为产品id (iot_product_info.id)
     */
    public String getBelongToId() {
        return belongToId;
    }

    /**
     * 设置归属id, 目前为产品id (iot_product_info.id)
     *
     * @param belongToId 归属id, 目前为产品id (iot_product_info.id)
     */
    public void setBelongToId(String belongToId) {
        this.belongToId = belongToId;
    }

    /**
     * 获取归属类型, 目前为PRODUCT_CHAIN
     *
     * @return belong_type - 归属类型, 目前为PRODUCT_CHAIN
     */
    public String getBelongType() {
        return belongType;
    }

    /**
     * 设置归属类型, 目前为PRODUCT_CHAIN
     *
     * @param belongType 归属类型, 目前为PRODUCT_CHAIN
     */
    public void setBelongType(String belongType) {
        this.belongType = belongType;
    }

    /**
     * 获取租户id
     *
     * @return tenant_id - 租户id
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * 设置租户id
     *
     * @param tenantId 租户id
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
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