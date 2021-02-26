package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.UserModel;


/**
 * @author shkstart
 * @create 2021-02-09 22:32
 */
public interface UserService {
    //处理根据id查询用户的请求
    public UserModel getUserById(Integer id);
    //处理用户注册的请求
    public void register(UserModel userModel) throws BusinessException;

    /**
     *
     * @param telphone:用户注册手机号
     * @param ecrptPassword：用户加密后的密码
     * @throws BusinessException
     */
    public UserModel validateLogin(String telphone, String ecrptPassword) throws BusinessException;
}
