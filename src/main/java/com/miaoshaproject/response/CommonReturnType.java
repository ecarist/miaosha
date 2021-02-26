package com.miaoshaproject.response;

import com.miaoshaproject.controller.viewobject.UserVO;
import com.miaoshaproject.service.model.UserModel;

/**
 * @author shkstart
 * @create 2021-02-09 23:35
 */
public class CommonReturnType {
    //status表明对应请求的返回处理结果：成功 or 失败
    private String status;
    //成功:返回前端需要的json数据；
    //失败:使用通用的错误码格式，返回错误信息（什么原因导致失败---自己定义的异常类和异常处理）
    private Object data;

    //controller完成请求处理后，调用一下方法，如果不带任何status,则默认是成功的success
    public static CommonReturnType create(Object data) {
        return create("success",data);
    }

    public static CommonReturnType create(String status, Object data) {
        CommonReturnType commonReturnType = new CommonReturnType();
        commonReturnType.setData(data);
        commonReturnType.setStatus(status);
        return commonReturnType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
