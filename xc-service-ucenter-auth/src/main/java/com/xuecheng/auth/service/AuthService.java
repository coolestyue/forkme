package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Autowired
    RestTemplate restTemplate;
    public AuthToken login(String username,String password,String clientId,String clientSecret){
        AuthToken authToken = this.applyTaken(username, password, clientId, clientSecret);
        if(authToken==null){
            //异常
        }
        String auth_taken = authToken.getAccess_token();
        String contents = JSON.toJSONString(authToken);
        boolean b = this.saveRedis(auth_taken, contents, 54456);
        return authToken;
    }
    public AuthToken getUserToken(String token){
        String userTaken = "user_token"+":"+token;
        String s = stringRedisTemplate.opsForValue().get(userTaken);
        AuthToken authToken = JSON.parseObject(s, AuthToken.class);
        return authToken;
    }
    //将身份令牌存入redis
    public boolean saveRedis(String access_token,String content,long ttl){
                //令牌名称
        String name = "user_token:" + access_token;
        //保存到令牌到redis
        stringRedisTemplate.boundValueOps(name).set(content,ttl, TimeUnit.SECONDS);
        //获取过期时间
        Long expire = stringRedisTemplate.getExpire(name);
        return expire>0;
    }

    public AuthToken applyTaken(String username,String password,String clientId,String clientSecret){
        ServiceInstance serviceInstance = loadBalancerClient.choose("学成在线微服务");
        URI uri = serviceInstance.getUri();//Http:ip:host
        String authUrl = uri+"/auth/oauth/token";
        LinkedMultiValueMap<String,String> header = new LinkedMultiValueMap();
        String httpBasic = this.getHttpBasic(clientId, clientSecret);
        header.add("Authorization",httpBasic);
        LinkedMultiValueMap<String,String> body = new LinkedMultiValueMap();
        body.add("grant_type","password");
        body.add("username",username);
        body.add("password",password);
        HttpEntity<MultiValueMap<String,String>> httpEntity = new HttpEntity(body,header);
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                            if (response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401 ) {
                super.handleError(response);
            }
            }
        });
        ResponseEntity<Map> exchange = restTemplate.exchange(authUrl, HttpMethod.POST, httpEntity, Map.class);
        Map map = exchange.getBody();
                if(map == null ||
                map.get("access_token") == null ||
                map.get("refresh_token") == null ||
                map.get("jti") == null){//jti是jwt令牌的唯一标识作为用户身份令牌
           // ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
                    String  error_description = (String) map.get("error_description");
                    if(StringUtils.isNotEmpty(error_description)){
                        if(error_description.indexOf("坏的凭证")>=0){
                            //
                        }else if(error_description.indexOf("UserDetailsService returned null")>=0){
                            //
                        }
                    }
                    return null;
        }




        AuthToken authToken = new AuthToken();
        //访问令牌(jwt)
        String jwt_token = (String) map.get("access_token");
        //刷新令牌(jwt)
        String refresh_token = (String) map.get("refresh_token");
        //jti，作为用户的身份标识
        String access_token = (String) map.get("jti");
        authToken.setJwt_token(jwt_token);
        authToken.setAccess_token(access_token);
        authToken.setRefresh_token(refresh_token);
        return authToken;
    }
    private String getHttpBasic(String clientId,String clientSecret){
        String bascString = clientId+":"+clientSecret;
        byte[] decode = Base64Utils.decode(bascString.getBytes());

        return "Basic "+new String(decode);

    }
}
