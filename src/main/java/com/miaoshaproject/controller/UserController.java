package com.miaoshaproject.controller;

import com.alibaba.druid.util.StringUtils;
import com.miaoshaproject.controller.viewobject.UserVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.CommonError;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import com.sun.xml.internal.rngom.parse.host.Base;
import javafx.beans.binding.ObjectExpression;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;
import sun.security.provider.MD5;

import javax.servlet.http.HttpServletRequest;
import java.io.OptionalDataException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Random;

/**
 * @author shkstart
 * @create 2021-02-09 22:32
 */

@Controller("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")//跨域请求ajax.让response headers里面加上一个Access-Control-Allow-Origin: *
public class UserController extends BaseController {
    @Autowired
    private UserService userService;
    @Autowired
    private HttpServletRequest httpServletRequest;

    //通过id获取用户信息
    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam("id") Integer id) throws BusinessException {
        //调用业务层（service）具体实现类中的方法，按id查询用户并返回
        UserModel userModel = userService.getUserById(id);
        if(userModel==null){
            userModel.setEcrptPassword("111");
        }
        UserVO userVO = converFromModel(userModel);
        //如果没有出现异常，正常处理了请求，则调用creat方法，将数据再包装一个状态信息;success，返回给前端通用数据格式：状态+data
        CommonReturnType check = CommonReturnType.create(userVO);
        return check;

    }

    //用户获取otp短信接口
    @RequestMapping(value="/getotp",method = RequestMethod.POST,consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam("telphone")String telphone){
        //产生一个随机的otp;
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt+=10000;
        String otp = String.valueOf(randomInt);
        //产生的otp与用户手机号关联;session机制---->主要是为了方便后续验证两个otpcode
        httpServletRequest.getSession().setAttribute(telphone,otp);

        //产生的otp通过短信方式发送给用户，省略模拟
        System.out.println("telphone="+telphone+" & optCode="+ otp );
        //int i=10/0;

        return CommonReturnType.create(null);
    }


    //用户注册接口
    @RequestMapping(value = "/register",method = RequestMethod.POST,consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType register(@RequestParam(name = "telphone")String telphone,
                                     @RequestParam(name = "otpCode")String otpCode,
                                     @RequestParam(name = "name")String name,
                                     @RequestParam(name = "gender")Byte gender,
                                     @RequestParam(name = "age")Integer age,
                                     @RequestParam(name = "password")String password) throws BusinessException,NoSuchAlgorithmException,UnsupportedEncodingException {
        //验证手机号和服务器端session中otpcode相同

        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telphone);
        //此处用的是alibaba的druid库中的equals,因为它内部会进行一个判空的处理
        if (!StringUtils.equals(inSessionOtpCode,otpCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDITION_ERROR,"短信验证码不一致");

        }
        //验证手机号和服务器端session中otpcode相同后，如果一致进入注册流程
        UserModel userModel=new UserModel();
        userModel.setName(name);
        userModel.setAge(age);
        userModel.setGender(gender);
        //用户传入的密码是明文的，不能直接存入数据库，要加密后存入----采用自己写的MD5的方法加密
        userModel.setEcrptPassword(this.EncodeByMd5(password));
        userModel.setRegisterMode("byphone");
        userModel.setTelphone(telphone);
        userService.register(userModel);
        return CommonReturnType.create(null);
    }


    //用户登录接口
    @RequestMapping(value = "/login",method = RequestMethod.POST,consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telphone")String telphone,
                                  @RequestParam(name = "password")String password) throws BusinessException,NoSuchAlgorithmException,UnsupportedEncodingException {
        //入参校验，手机号和密码都不能为空
        if (StringUtils.isEmpty(password) || StringUtils.isEmpty(telphone)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDITION_ERROR);
        }
        //用户登录开始-->校验用户信息是否合法
        UserModel userModel = userService.validateLogin(telphone, this.EncodeByMd5(password));
        //如果上一步的方法没有抛出任何异常的话，则把登录凭证加入到用户登录成功的session内
        this.httpServletRequest.getSession().setAttribute("LOGIN",true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);
        return CommonReturnType.create(null);
    }



    public String EncodeByMd5(String str) throws NoSuchAlgorithmException,UnsupportedEncodingException {
        //确定计算方法
        MessageDigest md5=MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();
        //加密字符串
        String newstr = base64Encoder.encode(md5.digest(str.getBytes("utf-8")));
        return newstr;

    }





    private UserVO converFromModel(UserModel userModel){
        if(userModel==null)return null;
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel,userVO);
        return userVO;
    }
    //定义一个exception handler来处理未被controller层吸收的异常



}
