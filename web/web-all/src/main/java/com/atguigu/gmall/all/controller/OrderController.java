package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/13 13:00
 * @FileName: OrderController
 */
@Controller
public class OrderController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    @GetMapping("trade.html")
    public String trade(Model model) {
        Result<Map> result = this.orderFeignClient.trade();
        model.addAllAttributes(result.getData());
        // 返回订单结算页面
        return "order/trade";
    }

}
