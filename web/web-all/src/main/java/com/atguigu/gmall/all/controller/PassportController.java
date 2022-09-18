package com.atguigu.gmall.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/08 13:17
 * @FileName: PassportController
 */
@Controller
public class PassportController {
    @GetMapping("login.html")
    public String login(HttpServletRequest request) {
        // window.location.href = 'http://passport.gmall.com/login.html?originUrl='+window.location.href
        // window.location.href 是当前页面，也就是从哪里跳转过来的页面
        String originUrl = request.getParameter("originUrl");
        // 保证返回的路径完整
        String queryString = request.getQueryString();
        String substring = queryString.substring(queryString.indexOf("=") + 1);
        request.setAttribute("originUrl", substring);
        return "login";
    }
}
