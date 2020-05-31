package com.xuecheng.api.auth;

import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.ApiOperation;

public interface AuthControllerApi {
    public LoginResult login(LoginRequest loginRequest);

    @ApiOperation("查询userjwt令牌")
    public JwtResult userjwt();
    ResponseResult logout();
}
