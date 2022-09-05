package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.FileWriter;
import java.io.IOException;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/04 13:54
 * @FileName: IndexController
 */
@Controller
public class IndexController {
    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private TemplateEngine templateEngine;

    @GetMapping({"index.html", "/"})
    public String index(Model model) {
        Result result = productFeignClient.getBaseCategoryList();
        model.addAttribute("list", result.getData());
        return "index/index";
    }

    @GetMapping("createIndex")
    @ResponseBody
    public Result createIndex() {
        //  获取后台存储的数据
        Result result = productFeignClient.getBaseCategoryList();
        Context context = new Context();
        context.setVariable("list", result.getData());
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter("F:\\index.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
        templateEngine.process("index/index.html", context, fileWriter);
        return Result.ok();
    }

}
