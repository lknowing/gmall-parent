package com.atguigu.gmall.common.cache;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class GmallCacheAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @SneakyThrows
    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object GmallCacheAspectTest(ProceedingJoinPoint joinPoint) {
        Object[] args = new Object[0];
        try {
            //声明一个对象
            Object obj = null;
            /*
            1. 获取缓存的key key=注解的前缀:方法的参数
                a. 先获取注解
                b. 获取参数拼接
            2. 通过key获取缓存的数据
                true:
                    return object
                false:
                    上锁：
                    查询数据库 放入缓存
                    解锁
             */
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            GmallCache gmallCache = methodSignature.getMethod().getAnnotation(GmallCache.class);
            String prefix = gmallCache.prefix();
            // 获取参数
            args = joinPoint.getArgs();
            String key = prefix + Arrays.asList(args).toString();
            // 通过缓存获取数据
            obj = this.getRedisData(key, methodSignature);
            if (obj == null) {
                String locKey = key + ":lock";
                // 缓存没有数据 去数据库查询 放入缓存
                RLock lock = redissonClient.getLock(locKey);
                boolean res = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                if (res) {
                    try {
                        // 上锁成功
                        obj = joinPoint.proceed(args);
                        if (obj == null) {
                            Object o = new Object();
                            redisTemplate.opsForValue().setIfAbsent(key, JSON.toJSONString(o), RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                            return o;
                        }
                        redisTemplate.opsForValue().setIfAbsent(key, JSON.toJSONString(obj), RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                        return obj;
                    } finally {
                        lock.unlock();
                    }
                } else {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return GmallCacheAspectTest(joinPoint);
                }
            } else {
                return obj;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return joinPoint.proceed(args);
    }

    private Object getRedisData(String key, MethodSignature methodSignature) {
        // JSON.toJSONString(obj)
        String strJson = (String) this.redisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(strJson)) {
            // 字符串 转成 具体的方法需要的返回 数据类型
            return JSON.parseObject(strJson, methodSignature.getReturnType());
        }
        return null;
    }

/*
    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate redisTemplate;

    //  定义一个环绕通知！
    @SneakyThrows
    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object gmallCacheAspectMethod(ProceedingJoinPoint point) {
        //  定义一个对象
        Object obj = new Object();
        */
/*
         业务逻辑！
         1. 必须先知道这个注解在哪些方法 || 必须要获取到方法上的注解
         2. 获取到注解上的前缀
         3. 必须要组成一个缓存的key！
         4. 可以通过这个key 获取缓存的数据
            true:
                直接返回！
            false:
                分布式锁业务逻辑！
         *//*

        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        GmallCache gmallCache = methodSignature.getMethod().getAnnotation(GmallCache.class);
        //   获取到注解上的前缀
        String prefix = gmallCache.prefix();
        //  组成缓存的key！ 获取方法传递的参数
        String key = prefix + Arrays.asList(point.getArgs()).toString();
        try {
            //  可以通过这个key 获取缓存的数据
            obj = this.getRedisData(key, methodSignature);
            if (obj == null) {
                //  分布式业务逻辑
                //  设置分布式锁，进入数据库进行查询数据！
                RLock lock = redissonClient.getLock(key + ":lock");
                //  调用trylock方法
                boolean result = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                //  判断
                if (result) {
                    try {
                        //  执行业务逻辑：直接从数据库获取数据
                        //  这个注解 @GmallCache 有可能在 BaseCategoryView getCategoryName , List<SpuSaleAttr> getSpuSaleAttrListById ....
                        obj = point.proceed(point.getArgs());
                        //  防止缓存穿透
                        if (obj == null) {
                            Object object = new Object();
                            //  将缓存的数据变为 Json 的 字符串
                            this.redisTemplate.opsForValue().set(key, JSON.toJSONString(object), RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                            return object;
                        }
                        //  将缓存的数据变为 Json 的 字符串
                        this.redisTemplate.opsForValue().set(key, JSON.toJSONString(obj), RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                        return obj;
                    } finally {
                        //  解锁
                        lock.unlock();
                    }
                } else {
                    //  没有获取到
                    try {
                        Thread.sleep(100);
                        return gmallCacheAspectMethod(point);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                //  直接从缓存获取的数据！
                return obj;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        //  数据库兜底！
        return point.proceed(point.getArgs());
    }

    */
/*

    private Object getRedisData(String key, MethodSignature methodSignature) {
        //  在向缓存存储数据的时候，将数据变为Json 字符串了！
        //  通过这个key 获取到缓存的value
        String strJson = (String) this.redisTemplate.opsForValue().get(key);
        //  判断
        if (!StringUtils.isEmpty(strJson)) {
            //  将字符串转换为对应的数据类型！
            return JSON.parseObject(strJson, methodSignature.getReturnType());
        }
        return null;
    }
*/

}
