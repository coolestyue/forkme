package com.xuecheng.auth.controller;

import com.xuecheng.api.auth.AuthControllerApi;
import com.xuecheng.auth.service.AuthService;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/")
public class AuthController implements AuthControllerApi {
    @Autowired
    AuthService authService;
    @Override
    @RequestMapping("/uesrlogin")
    public LoginResult login(LoginRequest loginRequest) {

        AuthToken authToken = authService.login(loginRequest.getUsername(), loginRequest.getPassword(), "1", "2");
        String access_token = authToken.getAccess_token();
        saveCookie(access_token);
         return new LoginResult(CommonCode.SUCCESS,access_token);

    }

    @Override
    @GetMapping("/userjwt")
    public JwtResult userjwt() {

        String tokenFormCookie = this.getTokenFormCookie();
        AuthToken userToken = authService.getUserToken(tokenFormCookie);
        String jwt_token = userToken.getJwt_token();

        return new JwtResult(CommonCode.SUCCESS,jwt_token);
    }

    @Override
    public ResponseResult logout() {

        return null;
    }

    public String getTokenFormCookie(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Map<String, String> cookieMap = CookieUtil.readCookie(request, "uid");
        String uid = cookieMap.get("uid");
        return uid;
    }

    private void saveCookie(String access_token) {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        CookieUtil.addCookie(response,"cookiedoamain","/","uid",access_token,10,false);
    }
}
