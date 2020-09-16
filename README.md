# 物联网规则计算引擎

一款基于Akka Actor为底层框架的规则计算引擎

## 背景

> 规则引擎起源于基于规则的专家系统，而基于规则的专家系统又是专家系统的其中一个分支。专家系统属于人工智能的范畴，它模仿人类的推理方式，使用 试探性的方法进行推理，并使用人类能理解的术语解释和证明它的推理结论。

在做物联网云平台，特别是PaaS平台时，我们不免会需要一个实时计算引擎去对底层上报的数据加以处理、保存、分发，甚至进行一些较为复杂的判断处理逻辑。

在物联网广泛应用的今天，数据处理规则的变化可谓是日新月异，针对频繁变化的需求，我们唯有找到一个足够灵活的解决方案才能在超多需求的今天跟得上产品的步伐。

该规则引擎借鉴ThingsBoard规则引擎的设计理念，打造一款可以灵活自定义算子，支持规则链路热修改的基础规则引擎。

## 安装

这个项目使用 [java 1.8](https://www.java.com) 和 [mysql](https://www.mysql.com)。请确保你本地安装了它们。

## 使用说明

参考docs文件夹下的说明文档。

## 项目主要结构

### ruleengine

```
|--> ruleengine
 |--> actorcluster
 |--> cache
 |--> config
 |--> dao
 |--> model
 |--> mongo
 |--> service
 |--> stream
 |--> support
 |--> web
 |--- MicroIotRuleEngineApplication.java
```

### actorcluster

```
|--> actorcluster
 |--> actor
 |--> actorsystem
 |--> config
 |--> exception
 |--> message
 |--- ActorClusterContext.java
 |--- ActorClusterService.java
 |--- DefaultActorClusterService.java
```

### actorsystem

```
|--> actorsystem
 |--> actor
 |--> actorservice
 |--> entity
 |--> message
 |--> otherservice
 |--- ActorSystemContext.java
 |--- ConstantFields.java
```
