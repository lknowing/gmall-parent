package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/08 18:18
 * @FileName: AuthGlobalFilter
 */
@Component
public class AuthGlobalFilter implements GlobalFilter {
    /*
    authUrls:
      url: trade.html,myOrder.html,list.html,addCart.html # 用户访问该控制器的时候，会被拦截跳转到登录！
     */
    @Value("${authUrls.url}")
    private String authUrls;

    private AntPathMatcher pathMatcher = new AntPathMatcher();

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * @param exchange spring框架封装的 web服务请求 request 和响应 response对象
     * @param chain    过滤器链对象
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 先获取url路径
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        // 判断是否属于内部数据接口
        if (pathMatcher.match("/**/inner/**", path)) {
            ServerHttpResponse response = exchange.getResponse();
            // 提示用户没有权限访问
            return out(response, ResultCodeEnum.PERMISSION);
        }

        // 获取到登录的userId，在缓存中获取数据，必须要有token
        String userId = this.getUserId(request);
        // 获取临时用户Id
        String userTempId = this.getUserTempId(request);
        // 判断是否是非法登录 ip不一致，上面的获取getUserId方法return "-1";
        if ("-1".equals(userId)) {
            ServerHttpResponse response = exchange.getResponse();
            // 提示用户非法请求
            return out(response, ResultCodeEnum.ILLEGAL_REQUEST);
        }

        if (pathMatcher.match("/api/**/auth/**", path)) {
            // 判断用户是否登录
            if (StringUtils.isEmpty(userId)) {
                ServerHttpResponse response = exchange.getResponse();
                // 提示用户未登录
                return out(response, ResultCodeEnum.LOGIN_AUTH);
            }
        }

        // 用户访问一些 业务控制器 需要登录，关注 订单 购物车
        // trade.html,myOrder.html,list.html,addCart.html # 用户访问该控制器的时候，会被拦截跳转到登录！
        String[] split = authUrls.split(",");
        if (split != null && split.length > 0) {
            for (String url : split) {
                if (path.indexOf(url) != -1 && StringUtils.isEmpty(userId)) {
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION,
                            "http://www.gmall.com/login.html?originUrl=" + request.getURI());
                    // 重定向
                    return response.setComplete();
                }
            }
        }

        // 将获取到的用户Id 添加到请求头 请求头可能会存有userId
        // 以后使用 AuthContextHolder类获取请求头里面的userId
        if (!StringUtils.isEmpty(userId) || !StringUtils.isEmpty(userTempId)) {
            // 放入请求头
            if (!StringUtils.isEmpty(userId)) {
                request.mutate().header("userId", userId).build();
            }
            if (!StringUtils.isEmpty(userTempId)) {
                request.mutate().header("userTempId", userTempId).build();
            }
            return chain.filter(exchange.mutate().request(request).build());
        }
        // 默认返回，表示这个过滤器结束了
        return chain.filter(exchange);
    }

    private String getUserTempId(ServerHttpRequest request) {
        String userTempId = "";
        HttpCookie httpCookie = request.getCookies().getFirst("userTempId");
        if (httpCookie != null) {
            userTempId = httpCookie.getValue();
        } else {
            List<String> stringList = request.getHeaders().get("userTempId");
            if (!CollectionUtils.isEmpty(stringList)) {
                userTempId = stringList.get(0);
            }
        }
        return userTempId;
    }

    /**
     * @param request
     * @return
     */
    private String getUserId(ServerHttpRequest request) {
        String token = "";
        HttpCookie httpCookie = request.getCookies().getFirst("token");
        if (httpCookie != null) {
            token = httpCookie.getValue();
        } else {
            List<String> tokenList = request.getHeaders().get("token");
            if (!CollectionUtils.isEmpty(tokenList)) {
                token = tokenList.get(0);
            }
        }

        if (!StringUtils.isEmpty(token)) {
            String userLoginKey = "user:login:" + token;
            String string = (String) redisTemplate.opsForValue().get(userLoginKey);
            if (!StringUtils.isEmpty(string)) {
                JSONObject jsonObject = JSON.parseObject(string);
                String gatewayIpAddress = IpUtil.getGatwayIpAddress(request);
                String ip = (String) jsonObject.get("ip");
                if (gatewayIpAddress.equals(ip)) {
                    String userId = (String) jsonObject.get("userId");
                    return userId;
                } else {
                    return "-1";
                }
            }
        }
        return "";
    }

    /**
     * @param response
     * @param resultCodeEnum
     * @return
     */
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        // 输出的内容 细节处理 设置每个页面的类型 Content-Type=application/json
        Result result = Result.build(null, resultCodeEnum);
        String str = JSON.toJSONString(result);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        // 产生 DataBuffer
        DataBufferFactory dataBufferFactory = response.bufferFactory();
        DataBuffer wrap = dataBufferFactory.wrap(str.getBytes());
        return response.writeWith(Mono.just(wrap));
    }
}
