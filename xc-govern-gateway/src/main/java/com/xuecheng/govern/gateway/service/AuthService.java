package com.xuecheng.govern.gateway.service;

import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Service
public class AuthService {
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    //从cookie中查询身份令牌(短的)
    public String getTokenFromCookie(HttpServletRequest request){
        Map<String, String> cookieMap = CookieUtil.readCookie(request, "uid");
        String access_token = cookieMap.get("uid");
        if(StringUtils.isEmpty(access_token)){
            return null;
        }
        return access_token;
    }
    //从Header中查询JWT令牌
    public String getJwtFromHeader(HttpServletRequest request){
        String jwt = request.getHeader("Authorization");
        if(StringUtils.isEmpty(jwt)){
            return null;
        }
        if(!jwt.startsWith("Bearer ")){
            return null;
        }
        return jwt;
    }
    //从redis中查询是否存在
    public long getExpire(String auth_token){
        String key = "user_token:"+auth_token;
        Long expire = stringRedisTemplate.getExpire(key);
        return expire;
    }


}
