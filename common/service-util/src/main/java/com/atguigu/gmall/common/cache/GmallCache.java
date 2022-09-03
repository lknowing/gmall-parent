package com.atguigu.gmall.common.cache;

import java.lang.annotation.*;

@Target({ElementType.METHOD})// 注解的使用范围
@Retention(RetentionPolicy.RUNTIME)// 注解的生命周期
public @interface GmallCache {

    //  定义一个数据 sku:skuId
    //  目的用这个前缀要想组成 缓存的key！
    String prefix() default "cache:";

}
