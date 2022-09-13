package com.atguigu.gmall.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/13 12:55
 * @FileName: ServiceOrderApplication
 */
@SpringBootApplication
@ComponentScan({"com.atguigu.gmall"})
@EnableFeignClients("com.atguigu.gmall")
@EnableDiscoveryClient
public class ServiceOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceOrderApplication.class, args);
    }
}
