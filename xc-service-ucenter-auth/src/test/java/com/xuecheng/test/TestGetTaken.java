package com.xuecheng.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

public class TestGetTaken {
    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Autowired
    RestTemplate restTemplate;

    public void testTaken(){
        ServiceInstance serviceInstance = loadBalancerClient.choose("学成在线微服务");
        URI uri = serviceInstance.getUri();//Http:ip:host
        String authUrl = uri+"/auth/oauth/token";
        LinkedMultiValueMap<String,String> header = new LinkedMultiValueMap();
        String httpBasic = this.getHttpBasic("XcWebApp", "XcWebApp");
        header.add("Authorization",httpBasic);
        LinkedMultiValueMap<String,String> body = new LinkedMultiValueMap();
        body.add("grant_type","password");
        body.add("username","itcast");
        body.add("password","123");
        HttpEntity<MultiValueMap<String,String>> httpEntity = new HttpEntity(body,header);
        ResponseEntity<Map> exchange = restTemplate.exchange(authUrl, HttpMethod.POST, httpEntity, Map.class);
        Map body1 = exchange.getBody();

    }
    private String getHttpBasic(String clientId,String clientSecret){
        String bascString = clientId+":"+clientSecret;
        byte[] decode = Base64Utils.decode(bascString.getBytes());

        return "Basic "+new String(decode);

    }
}
