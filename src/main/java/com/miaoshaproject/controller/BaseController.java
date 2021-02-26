package com.miaoshaproject.controller;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * @author shkstart
 * @create 2021-02-10 15:35
 */
public class BaseController {
    //定义一个exception handler来处理未被controller层吸收的异常---->钩子思想3-4
    //抽象出来是想给所有controller都能用
    public static final String CONTENT_TYPE_FORMED="application/x-www-form-urlencoded";
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(value = {Exception.class})
    @ResponseBody
    public Object handlerException(HttpServletRequest request, Exception exception){
        HashMap<String, Object> map = new HashMap<>();
        if(exception instanceof BusinessException){
            BusinessException businessException= (BusinessException) exception;

            map.put("errCode",businessException.getErrorCode());
            map.put("errMsg",businessException.getErrorMsg());
        }else {

            map.put("errCode", EmBusinessError.UNKNOW_ERROR.getErrorCode());
            map.put("errMsg",EmBusinessError.UNKNOW_ERROR.getErrorMsg());

        }



        return CommonReturnType.create("fail",map);

    }


}
