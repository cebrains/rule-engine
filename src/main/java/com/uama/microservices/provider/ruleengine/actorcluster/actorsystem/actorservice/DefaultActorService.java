package com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actorservice;

import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.cluster.Cluster;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.uama.framework.common.util.CollectionUtil;
import com.uama.framework.util.log.LogUtils;
import com.uama.microservices.api.ruleengine.enums.IotRuleChainBelongTypeEnum;
import com.uama.microservices.api.ruleengine.enums.IotRuleChainRootEnum;
import com.uama.microservices.api.ruleengine.model.vo.rulechain.MIotRuleEngineRuleChainInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulenode.MIotRuleEngineRuleNodeInitV;
import com.uama.microservices.api.ruleengine.model.vo.rulerelation.MIotRuleEngineRuleRelationInitV;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.ActorSystemContext;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.ActorTypeMapperEnum;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.actor.NodeHolderActor;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity.NodeBuildPreparedData;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.entity.NodeId;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.NodeMessageCarrier;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.message.nodemessage.*;
import com.uama.microservices.provider.ruleengine.actorcluster.actorsystem.otherservice.systemsupplementaryservice.SystemSupplementaryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import scala.compat.java8.FutureConverters;
import scala.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @program: uama-microservices-iot-ulink-rule-engine
 * @description: @PostConstruct init()
 * @author: liwen
 * @create: 2019-05-09 19:44
 **/
@DependsOn("springContextUtil")
@Service
@Slf4j
public class DefaultActorService implements ActorService {
    @Value("${actor.actorsystem.name:akka}")
    private String actorSystemName;
    @Value("#{'${actor.actorsystem.actortypes:}'.split(',')}")
    private Set<ActorTypeMapperEnum> systemActorTypeList;
    @Value("${actor.actorsystem.config.filename:}")
    private String configFileName;
    @Value("#{'${actor.actor-cluster.seed-nodes:}'.split(',')}")
    private Set<String> seedNodes;
    @Value("${actor.actor-cluster.enable:false}")
    private boolean clusterEnable;
    
    @Value("${actor.actor-remote.canonical.hostname:default}")
    public String remoteHostname;
    @Value("${actor.actor-remote.canonical.port:25520}")
    public Integer remotePort;
    @Value("${actor.actor-remote.docker-container.bind:false}")
    public Boolean remoteDockerBind;
    @Value("${actor.actor-remote.bind.hostname:default}")
    public String remoteBindHostname;
    @Value("${actor.actor-remote.bind.port:25520}")
    public Integer remoteBindport;
    
    private ActorSystemContext actorSystemContext;
    
    private static DefaultActorDependenceService actorDependenceService;
    
    @Autowired
    private SystemSupplementaryService systemSupplementaryService;
    
    @Autowired
    public DefaultActorService(DefaultActorDependenceService actorDependenceService) {
        DefaultActorService.setDefaultActorDependenceService(actorDependenceService);
    }
    
    @PostConstruct
    public void init() {
        // 初始化Actor System
        this.initActorSystem();
        // 初始化系统增强功能
        this.initSystemSupplementary();
        // 初始化所有node holder actor
        this.initNodeHolderActor();
        // 初始化所有链路
        this.initRuleChains();
    }
    
    @PreDestroy
    public void destory() {
        if (clusterEnable) {
            final Cluster cluster = Cluster.get(this.actorSystemContext.getActorSystem());
            cluster.leave(cluster.selfAddress());
        } else {
            Future<Terminated> tScala = this.actorSystemContext.getActorSystem().terminate();
            try {
                Terminated tJava = FutureConverters.toJava(tScala).toCompletableFuture().get();
                if (null != tJava) {
                    LogUtils.log.info("Actor System Shut Down");
                }
            } catch (Exception e) {
                LogUtils.log.error("Actor System Occurs An Error When Shut Down", e);
            }
        }
    }
    
    @Override
    public void initActorSystem() {
        // 基本校验
        this.check(systemActorTypeList);
        // 获得actor system配置
        Config config = this.getActorSystemConfig(configFileName);
        if (null == config) {
            throw new IllegalArgumentException("actor system config not exists");
        }
        // 通过读取配置文件中的配置 覆盖 actor 的配置
        config = convertConfig(config);
        // 生成actor system
        actorSystemContext = ActorSystemContext.create(actorSystemName, config);
    }
    
    private Config convertConfig(Config config) {
        Map<String, Object> newConfigMap = Maps.newHashMap();
        String localIP;
        try {
            localIP = InetAddress.getLocalHost().getHostName();
            LogUtils.log.info("Actor cluster: local hostname is {}, canonical hostname is {}", localIP, remoteHostname);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        if (clusterEnable) {
            convertSeedNodes(newConfigMap, localIP);
        }
        convertRemoteHost(newConfigMap, localIP);
        
        return DefaultActorServiceHelper.replaceConfig(newConfigMap, config);
    }
    
    private void convertRemoteHost(Map<String, Object> newConfigMap, String localIP) {
        if (StringUtils.isNotBlank(remoteHostname)) {
            if ("default".equalsIgnoreCase(remoteHostname)) {
                newConfigMap.put("akka.remote.artery.canonical.hostname", localIP);
            } else {
                newConfigMap.put("akka.remote.artery.canonical.hostname", remoteHostname);
            }
        }
        if (null != remotePort) {
            newConfigMap.put("akka.remote.artery.canonical.port", remotePort);
        }
        if (Boolean.TRUE.equals(remoteDockerBind)) {
            if (StringUtils.isNotBlank(remoteBindHostname)) {
                if ("default".equalsIgnoreCase(remoteBindHostname)) {
                    newConfigMap.put("akka.remote.artery.bind.hostname", localIP);
                } else {
                    newConfigMap.put("akka.remote.artery.bind.hostname", remoteBindHostname);
                }
            }
            if (null != remoteBindport) {
                newConfigMap.put("akka.remote.artery.bind.port", remoteBindport);
            }
        }
    }
    
    private void convertSeedNodes(Map<String, Object> newConfigMap, String localIP) {
        if (CollectionUtil.isNotEmpty(seedNodes)) {
            newConfigMap.put("akka.cluster.seed-nodes", seedNodes);
        } else {
            String seedNode = String.format("akka.tcp://%s@%s:%s", actorSystemName, localIP, remotePort);
            newConfigMap.put("akka.cluster.seed-nodes", Lists.newArrayList(seedNode, seedNode));
        }
    }
    
    private Config getActorSystemConfig(String configFileName) {
        return StringUtils.isNotBlank(configFileName) ? ConfigFactory.parseResources(configFileName).withFallback(ConfigFactory.load()) : null;
    }
    
    private void check(Set<ActorTypeMapperEnum> systemActorTypeList) {
        // check actor holder list for initial
        DefaultActorServiceHelper.checkActorTypeList(systemActorTypeList);
    }
    
    private void initSystemSupplementary() {
        // 初始化deadLetter增强, 嵌入在actor system中 回调为如何处理系统中的dead letter
        this.systemSupplementaryService.initDeadLetterActor(this.actorSystemContext.getActorSystem(), deadLetter -> {
            Object message = deadLetter.message();
            if (message instanceof NodeMessage) {
                actorDependenceService.getDeadLetterMongoDbService().asyncSave((NodeMessage)message);
            }
            return null;
        });
    }
    
    private void initNodeHolderActor() {
        if (null == actorSystemContext) {
            throw new IllegalArgumentException("actor system context might not have been initialized");
        }
        // 初始化配置文件中的每一个node holder actor
        systemActorTypeList.forEach(actorTypeMapperEnum -> {
            ActorRef nodeHolderActorRef = new NodeHolderActor.ActorCreator(actorSystemContext, actorTypeMapperEnum).create();
            actorSystemContext.getActorHolderMap().put(actorTypeMapperEnum, nodeHolderActorRef);
        });
    }
    
    private void initRuleChains() {
        addRuleChainsToActorSystem(null);
    }

    @Override
    public void deleteRuleChainsFromActorSystem(List<MIotRuleEngineRuleChainInitV> ruleChainVList) {
        if (CollectionUtil.isNotEmpty(ruleChainVList)) {
            long deleteTimeStamp = System.currentTimeMillis();
            ruleChainVList.parallelStream().forEach(ruleChain -> actorSystemContext.getActorHolderMap().get(ActorTypeMapperEnum.SYSTEM)
                    .tell(new NodeMessageCarrier<>(null,
                                    null,
                                    new NodeDeleteMessage(ruleChain, false, IotRuleChainRootEnum.ROOT.getCode().equals(ruleChain.getRoot()), deleteTimeStamp)),
                            ActorRef.noSender()));
        }
    }

    @Override
    public void updateRuleChainsInActorSystem(List<String> ruleChainIdList) {
        if (CollectionUtil.isNotEmpty(ruleChainIdList)) {
            // prepare data
            NodeBuildPreparedData preparedData = this.preparedNodeBuildData(ruleChainIdList);
            List<MIotRuleEngineRuleChainInitV> ruleChainVList = preparedData.getAddedRuleChainVList();
            Map<String, MIotRuleEngineRuleChainInitV> ruleChainIdMap = preparedData.getRuleChainIdMap();
            Map<String, List<MIotRuleEngineRuleChainInitV>> chainsGroupByBelongToIdMap = preparedData.getChainsGroupByBelongToIdMap();
            Map<String, List<MIotRuleEngineRuleNodeInitV>> nodesGroupByRuleChainIdMap = preparedData.getNodesGroupByRuleChainIdMap();
            Map<String, List<MIotRuleEngineRuleRelationInitV>> relationsGroupByRuleChainIdMap = preparedData.getRelationsGroupByRuleChainIdMap();
            long timeStamp = System.currentTimeMillis();
            
            // send node update message to every rule chain to update all node actors
            ruleChainVList.parallelStream().forEach(ruleChainV -> {
                String ruleChainId = ruleChainV.getId();
                String firstNodeId = ruleChainV.getFirstNodeId();
                Map<String, List<MIotRuleEngineRuleRelationInitV>> ruleChainRelationsGroupByFromIdMap = relationsGroupByRuleChainIdMap.get(ruleChainId).stream().collect(Collectors.groupingBy(MIotRuleEngineRuleRelationInitV::getFromId));
                Map<String, List<MIotRuleEngineRuleRelationInitV>> ruleChainRelationsGroupByToIdMap = relationsGroupByRuleChainIdMap.get(ruleChainId).stream().collect(Collectors.groupingBy(MIotRuleEngineRuleRelationInitV::getToId));
                Map<String, MIotRuleEngineRuleNodeInitV> ruleNodeIdMap = nodesGroupByRuleChainIdMap.get(ruleChainId).stream().collect(Collectors.toMap(MIotRuleEngineRuleNodeInitV::getId, v -> v));
                actorSystemContext.getActorHolderMap().get(ActorTypeMapperEnum.SYSTEM)
                        .tell(new NodeMessageCarrier<>(new NodeId(firstNodeId),
                                        null,
                                        new NodeUpdateMessage(new NodeInitMessage(
                                                        ruleChainRelationsGroupByFromIdMap,
                                                        ruleChainRelationsGroupByToIdMap,
                                                        ruleNodeIdMap,
                                                        chainsGroupByBelongToIdMap,
                                                        ruleChainIdMap,
                                                        timeStamp),
                                                new NodeDeleteMessage(ruleChainV, true, IotRuleChainRootEnum.ROOT.getCode().equals(ruleChainV.getRoot()), timeStamp)
                                        )),
                                ActorRef.noSender());
            });
        }
    }
    
    /**
     * 将rule chain 加入到actor system中 形成规则链路
     * @param ruleChainIdList 需要加入的rule chain id集合 为null 或者 empty则为加入数据库中的全部rule chain
     */
    @Override
    public void addRuleChainsToActorSystem(List<String> ruleChainIdList) {
        // prepare data
        NodeBuildPreparedData preparedData = this.preparedNodeBuildData(ruleChainIdList);
        List<MIotRuleEngineRuleChainInitV> addedRuleChainVList = preparedData.getAddedRuleChainVList();
        Map<String, MIotRuleEngineRuleChainInitV> ruleChainIdMap = preparedData.getRuleChainIdMap();
        Map<String, List<MIotRuleEngineRuleChainInitV>> chainsGroupByBelongToIdMap = preparedData.getChainsGroupByBelongToIdMap();
        Map<String, List<MIotRuleEngineRuleNodeInitV>> nodesGroupByRuleChainIdMap = preparedData.getNodesGroupByRuleChainIdMap();
        Map<String, List<MIotRuleEngineRuleRelationInitV>> relationsGroupByRuleChainIdMap = preparedData.getRelationsGroupByRuleChainIdMap();
        // 初始化时间戳 用于标记同一时间初始化的各个链路 用于 一个链路中 一个节点如果为多个上层节点的目标 则会收到所有上层节点
        // 发来的init node message, 则会进行多次初始化, 此标记为了标记该节点已被该时间戳的链路初始化任务初始化过 不需要再次初始化
        long initTimeStamp = System.currentTimeMillis();
        
        // send node init message to every rule chain to init all node actors
        addedRuleChainVList.parallelStream().forEach(ruleChainV -> {
            String ruleChainId = ruleChainV.getId();
            Map<String, List<MIotRuleEngineRuleRelationInitV>> ruleChainRelationsGroupByFromIdMap = relationsGroupByRuleChainIdMap.get(ruleChainId).stream().collect(Collectors.groupingBy(MIotRuleEngineRuleRelationInitV::getFromId));
            Map<String, List<MIotRuleEngineRuleRelationInitV>> ruleChainRelationsGroupByToIdMap = relationsGroupByRuleChainIdMap.get(ruleChainId).stream().collect(Collectors.groupingBy(MIotRuleEngineRuleRelationInitV::getToId));
            Map<String, MIotRuleEngineRuleNodeInitV> ruleNodeIdMap = nodesGroupByRuleChainIdMap.get(ruleChainId).stream().collect(Collectors.toMap(MIotRuleEngineRuleNodeInitV::getId, v -> v));
            ruleChainRelationsGroupByFromIdMap.get(ruleChainId).forEach(ruleRelationInitV -> {
                String startNodeId = ruleRelationInitV.getToId();
                ActorTypeMapperEnum startNodeActorTypeEnum = ActorTypeMapperEnum.valueOf(ruleNodeIdMap.get(startNodeId).getNodeType());
                actorSystemContext.getActorHolderMap().get(startNodeActorTypeEnum)
                        .tell(new NodeMessageCarrier<>(new NodeId(startNodeId),
                                        null,
                                        new NodeInitMessage(ruleChainRelationsGroupByFromIdMap,
                                                ruleChainRelationsGroupByToIdMap,
                                                ruleNodeIdMap,
                                                chainsGroupByBelongToIdMap,
                                                ruleChainIdMap,
                                                initTimeStamp)),
                                ActorRef.noSender());
            });
        });
        
        // associate system holder actor with root chains first node and init rule chain context
        addedRuleChainVList.parallelStream().forEach(ruleChainV -> {
            String ruleChainId = ruleChainV.getId();
            String firstNodeId = ruleChainV.getFirstNodeId();
            Map<String, MIotRuleEngineRuleNodeInitV> ruleNodeIdMap = nodesGroupByRuleChainIdMap.get(ruleChainId).stream().collect(Collectors.toMap(MIotRuleEngineRuleNodeInitV::getId, v -> v));
            Map<String, List<MIotRuleEngineRuleRelationInitV>> ruleChainRelationsGroupByFromIdMap = relationsGroupByRuleChainIdMap.get(ruleChainId).stream().collect(Collectors.groupingBy(MIotRuleEngineRuleRelationInitV::getFromId));
            actorSystemContext.getActorHolderMap().get(ActorTypeMapperEnum.SYSTEM)
                    .tell(new NodeMessageCarrier<>(new NodeId(firstNodeId),
                                    null,
                                    new NodeInitMessage(ruleChainRelationsGroupByFromIdMap,
                                            null,
                                            ruleNodeIdMap,
                                            null,
                                            ruleChainIdMap,
                                            initTimeStamp)),
                            ActorRef.noSender());
        });
    
        // update root chains router couse new chains added
        actorSystemContext.getActorHolderMap().get(ActorTypeMapperEnum.SYSTEM)
                .tell(new NodeMessageCarrier<>(null,
                                null,
                                new RootRouterUpdateMessage(addedRuleChainVList)),
                        ActorRef.noSender());
    }
    
    private NodeBuildPreparedData preparedNodeBuildData(List<String> ruleChainIdList) {
        List<MIotRuleEngineRuleChainInitV> allRuleChainVList = actorDependenceService.getIotRuleChainService().getRuleChainsForActorSystem(null, IotRuleChainBelongTypeEnum.PRODUCT_CHAIN.name());
        List<MIotRuleEngineRuleChainInitV> addedRuleChainVList;
        if (CollectionUtil.isNotEmpty(ruleChainIdList)) {
            addedRuleChainVList = allRuleChainVList.parallelStream().filter(ruleChainInitV -> ruleChainIdList.contains(ruleChainInitV.getId())).collect(Collectors.toList());
        } else {
            addedRuleChainVList = allRuleChainVList;
        }
        Map<String, MIotRuleEngineRuleChainInitV> ruleChainIdMap = allRuleChainVList.parallelStream().collect(Collectors.toMap(MIotRuleEngineRuleChainInitV::getId, v -> v));
        Map<String, List<MIotRuleEngineRuleChainInitV>> chainsGroupByBelongToIdMap = allRuleChainVList.parallelStream().filter(ruleChainV -> ruleChainV.getRoot().equals(IotRuleChainRootEnum.NOT_ROOT.getCode())).collect(Collectors.groupingBy(MIotRuleEngineRuleChainInitV::getBelongToId));
        List<MIotRuleEngineRuleNodeInitV> ruleNodeVList = actorDependenceService.getIotRuleNodeService().getRuleNodesForActorSystem(ruleChainIdList);
        Map<String, List<MIotRuleEngineRuleNodeInitV>> nodesGroupByRuleChainIdMap = ruleNodeVList.stream().collect(Collectors.groupingBy(MIotRuleEngineRuleNodeInitV::getRuleChainId));
        List<MIotRuleEngineRuleRelationInitV> ruleRelationVList = actorDependenceService.getIotRuleRelationService().getRuleRelationsForActorSystem(ruleChainIdList);
        Map<String, List<MIotRuleEngineRuleRelationInitV>> relationsGroupByRuleChainIdMap = ruleRelationVList.stream().collect(Collectors.groupingBy(MIotRuleEngineRuleRelationInitV::getRuleChainId));
        LogUtils.log.info("add rule chains to ActorSystem prepare data done");
        return new NodeBuildPreparedData(addedRuleChainVList, ruleChainIdMap, chainsGroupByBelongToIdMap, nodesGroupByRuleChainIdMap, relationsGroupByRuleChainIdMap);
    }
    
    private static void setDefaultActorDependenceService(DefaultActorDependenceService actorDependenceService) {
        DefaultActorService.actorDependenceService = actorDependenceService;
    }
    
    public static DefaultActorDependenceService defultActorDependenceService() {
        return DefaultActorService.actorDependenceService;
    }
    
    @Override
    public ActorSystemContext getActorSystemContext() {
        return actorSystemContext;
    }
    
    @Override
    public void input(NodeMessage message) {
        log.info("Actor system receive a message: {}", message.toString());
        this.getActorSystemContext().getActorHolderMap().get(ActorTypeMapperEnum.SYSTEM)
                .tell(new NodeMessageCarrier<>(null, null, message),ActorRef.noSender());
    }
}
