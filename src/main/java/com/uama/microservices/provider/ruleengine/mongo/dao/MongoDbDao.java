package com.uama.microservices.provider.ruleengine.mongo.dao;

import com.uama.framework.common.util.BigDecimalUtil;
import com.uama.framework.util.log.LogUtils;
import com.uama.microservices.provider.ruleengine.mongo.enums.MongoAsyncSaveResultEnum;
import com.uama.microservices.provider.ruleengine.mongo.model.MongoAsyncSaveResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * 描述:
 * mongoDB基础方法封装
 *
 */
public abstract class MongoDbDao<T> {
    
    protected Logger logger = LoggerFactory.getLogger(MongoDbDao.class);
    
    @Value("${mongo.service.dao.thread_pool_size:2}")
    private int threadPoolSize;
    
    @Value("${mongo.service.dao.queue_capacity:9960}")
    private int queueCapacity;
    
    @Value("${mongo.service.dao.remaining_queue_capacity_threshold:20}")
    private double remainingQueueCapacityThreshold;
    
    
    private ThreadPoolExecutor executorService;
    
    @PostConstruct
    public void init() {
        executorService = new ThreadPoolExecutor(threadPoolSize, threadPoolSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(queueCapacity));
    }
    
    @PreDestroy
    public void stop() {
        if (null != executorService) {
            try {
                executorService.shutdownNow();
            } catch(Exception e) {
                logger.error("mongo db executor service shut down fail, {}", e.getMessage());
            }
        }
    }

    /**
     * 反射获取泛型类型
     *
     * @return
     */
    protected abstract Class<T> getEntityClass();

    @Autowired
    private MongoTemplate mongoTemplate;

    /***
     * 保存一个对象
     * @param t
     */
    public void save(T t) {
        logger.debug("-------------->MongoDB save start");
        this.mongoTemplate.save(t);
    }
    
    public MongoAsyncSaveResult asyncSave(T t) {
        MongoAsyncSaveResult result = new MongoAsyncSaveResult();
        double remainingCapacity = Double.parseDouble(BigDecimalUtil.calculatePercent(executorService.getQueue().remainingCapacity(), queueCapacity, 4));
        if (remainingCapacity <= remainingQueueCapacityThreshold) {
            result.setResult(MongoAsyncSaveResultEnum.ASYNC_SAVE_QUEUE_HIGH_LEVEL);
        } else {
            result.setResult(MongoAsyncSaveResultEnum.ASYNC_SAVE_NORMAL);
        }
        try {
            executorService.submit(() -> this.save(t));
        } catch (RejectedExecutionException e) {
            result.setResult(MongoAsyncSaveResultEnum.ASYNC_SAVE_QUEUE_FULL);
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            result.setResult(MongoAsyncSaveResultEnum.ASYNC_SAVE_FAIL);
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    /***
     * 根据id从几何中查询对象
     * @param id
     * @return
     */
    public T queryById(Integer id) {
        Query query = new Query(Criteria.where("_id").is(id));
        return this.mongoTemplate.findOne(query, this.getEntityClass());
    }

    /**
     * 根据条件查询集合
     *
     * @param object
     * @return
     */
    public List<T> queryList(T object) {
        Query query = getQueryByObject(object);
        return mongoTemplate.find(query, this.getEntityClass());
    }

    /**
     * 根据条件查询只返回一个文档
     *
     * @param object
     * @return
     */
    public T queryOne(T object) {
        Query query = getQueryByObject(object);
        return mongoTemplate.findOne(query, this.getEntityClass());
    }

    /***
     * 根据条件分页查询
     * @param object
     * @param start 查询起始值
     * @param size  查询大小
     * @return
     */
    public List<T> getPage(T object, int start, int size) {
        Query query = getQueryByObject(object);
        query.skip(start);
        query.limit(size);
        logger.debug("-------------->MongoDB queryPage start");
        return this.mongoTemplate.find(query, this.getEntityClass());
    }

    /***
     * 根据条件查询库中符合条件的记录数量
     * @param object
     * @return
     */
    public Long getCount(T object) {
        Query query = getQueryByObject(object);
        logger.debug("-------------->MongoDB Count start");
        return this.mongoTemplate.count(query, this.getEntityClass());
    }

    /***
     * 删除对象
     * @param t
     * @return
     */
    public int delete(T t) {
        logger.debug("-------------->MongoDB delete start");
        return (int) this.mongoTemplate.remove(t).getDeletedCount();
    }

    /**
     * 根据id删除
     *
     * @param id
     */
    public void deleteById(Integer id) {
        Criteria criteria = Criteria.where("_id").is(id);
        Query query = new Query(criteria);
        T obj = this.mongoTemplate.findOne(query, this.getEntityClass());
        logger.debug("-------------->MongoDB deleteById start");
        if (obj != null) {
            this.delete(obj);
        }
    }

    /*MongoDB中更新操作分为三种
     * 1：updateFirst     修改第一条
     * 2：updateMulti     修改所有匹配的记录
     * 3：upsert  修改时如果不存在则进行添加操作
     * */

    /**
     * 修改匹配到的第一条记录
     * @param srcObj
     * @param targetObj
     */
    public void updateFirst(T srcObj, T targetObj){
        Query query = getQueryByObject(srcObj);
        Update update = getUpdateByObject(targetObj);
        logger.debug("-------------->MongoDB updateFirst start");
        this.mongoTemplate.updateFirst(query,update,this.getEntityClass());
    }

    /***
     * 修改匹配到的所有记录
     * @param srcObj
     * @param targetObj
     */
    public void updateMulti(T srcObj, T targetObj){
        Query query = getQueryByObject(srcObj);
        Update update = getUpdateByObject(targetObj);
        logger.debug("-------------->MongoDB updateFirst start");
        this.mongoTemplate.updateMulti(query,update,this.getEntityClass());
    }

    /***
     * 修改匹配到的记录，若不存在该记录则进行添加
     * @param srcObj
     * @param targetObj
     */
    public void updateInsert(T srcObj, T targetObj){
        Query query = getQueryByObject(srcObj);
        Update update = getUpdateByObject(targetObj);
        logger.debug("-------------->MongoDB updateInsert start");
        this.mongoTemplate.upsert(query,update,this.getEntityClass());
    }

    /**
     * 将查询条件对象转换为query
     *
     * @param object
     * @return
     * @author Jason
     */
    private Query getQueryByObject(T object) {
        Query query = new Query();
        String[] fileds = getFiledName(object);
        Criteria criteria = new Criteria();
        for (int i = 0; i < fileds.length; i++) {
            String filedName = fileds[i];
            Object filedValue = getFieldValueByName(filedName, object);
            if (filedValue != null) {
                criteria.and(filedName).is(filedValue);
            }
        }
        query.addCriteria(criteria);
        return query;
    }

    /**
     * 将查询条件对象转换为update
     *
     * @param object
     * @return
     * @author Jason
     */
    private Update getUpdateByObject(T object) {
        Update update = new Update();
        String[] fileds = getFiledName(object);
        for (int i = 0; i < fileds.length; i++) {
            String filedName = fileds[i];
            Object filedValue =getFieldValueByName(filedName, object);
            if (filedValue != null) {
                update.set(filedName, filedValue);
            }
        }
        return update;
    }

    /***
     * 获取对象属性返回字符串数组
     * @param o
     * @return
     */
    private static String[] getFiledName(Object o) {
        Field[] fields = o.getClass().getDeclaredFields();
        String[] fieldNames = new String[fields.length];

        for (int i = 0; i < fields.length; ++i) {
            fieldNames[i] = fields[i].getName();
        }

        return fieldNames;
    }

    /***
     * 根据属性获取对象属性值
     * @param fieldName
     * @param o
     * @return
     */
    private static Object getFieldValueByName(String fieldName, Object o) {
        try {
            String e = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + e + fieldName.substring(1);
            Method method = o.getClass().getMethod(getter);
            return method.invoke(o);
        } catch (Exception var6) {
            LogUtils.log.error(var6.getMessage());
            return null;
        }
    }
}