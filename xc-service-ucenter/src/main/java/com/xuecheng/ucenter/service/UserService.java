package com.xuecheng.ucenter.service;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.dao.XcCompanyUserRepository;
import com.xuecheng.ucenter.dao.XcMenuMapper;
import com.xuecheng.ucenter.dao.XcUserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    XcCompanyUserRepository xcCompanyUserRepository;
    @Autowired
    XcUserRepository xcUserRepository;
    @Autowired
    XcMenuMapper xcMenuMapper;

    public XcUserExt getUserExt(String username){

        XcUser xcUser = this.findXcUser(username);
        if(xcUser==null){
            return null;
        }
        XcUserExt xcUserExt = new XcUserExt();
        String userId = xcUser.getId();

        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(userId);

        BeanUtils.copyProperties(xcUser,xcUserExt);
        xcUserExt.setPermissions(xcMenus);
        XcCompanyUser xcCompanyUser = xcCompanyUserRepository.findByUserId(userId);
        if(xcCompanyUser!=null){
            xcUserExt.setCompanyId(xcCompanyUser.getCompanyId());
        }
        return xcUserExt;
    }

    private XcUser findXcUser(String username) {

        return xcUserRepository.findByUsername(username);
    }

}
