package com.atguigu.gmall.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.IpUtil;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/08 12:48
 * @FileName: PassportApiController
 */
@RestController
@RequestMapping("/api/user/passport")
public class PassportApiController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("login")
    public Result login(@RequestBody UserInfo userInfo, HttpServletRequest request) {
        UserInfo info = userService.login(userInfo);
        if (info != null) {
            String token = UUID.randomUUID().toString();
            String loginKey = RedisConst.USER_LOGIN_KEY_PREFIX + token;
            String ip = IpUtil.getIpAddress(request);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ip", ip);
            jsonObject.put("userId", info.getId().toString());
            redisTemplate.opsForValue().set(loginKey, jsonObject.toJSONString(), RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("token", token);
            hashMap.put("nickName", info.getNickName());
            return Result.ok(hashMap);
        } else {
            return Result.fail().message("用户名或者密码错误！");
        }
    }

    @GetMapping("logout")
    public Result logout(@RequestHeader String token) {
        String loginKey = RedisConst.USER_LOGIN_KEY_PREFIX + token;
        redisTemplate.delete(loginKey);
        return Result.ok();
    }
}
