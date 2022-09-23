package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.activity.client.ActivityFeignClient;
import com.atguigu.gmall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/20 14:25
 * @FileName: SeckillController
 */
@Controller
public class SeckillController {
    @Autowired
    private ActivityFeignClient activityFeignClient;

    @GetMapping("seckill.html")
    public String seckill(Model model) {
        Result result = activityFeignClient.findAll();
        model.addAttribute("list", result.getData());
        // 秒杀的首页
        return "seckill/index";
    }

    @GetMapping("seckill/{skuId}.html")
    public String seckillItem(@PathVariable Long skuId, Model model) {
        Result result = activityFeignClient.getSeckillGoods(skuId);
        model.addAttribute("item", result.getData());
        // 秒杀商品的详情页面
        return "seckill/item";
    }

    @GetMapping("seckill/queue.html")
    public String seckillQueue(HttpServletRequest request) {
        String skuId = request.getParameter("skuId");
        String skuIdStr = request.getParameter("skuIdStr");
        request.setAttribute("skuId", skuId);
        request.setAttribute("skuIdStr", skuIdStr);
        return "seckill/queue";
    }

    @GetMapping("seckill/trade.html")
    public String trade(Model model) {
        // userAddressList detailArrayList totalNum totalAmount
        Result<Map> result = this.activityFeignClient.seckillTrade();
        if (result.isOk()) {
            model.addAllAttributes(result.getData());
            return "seckill/trade";
        } else {
            model.addAttribute("message", result.getMessage());
            return "seckill/fail";
        }
    }

}
