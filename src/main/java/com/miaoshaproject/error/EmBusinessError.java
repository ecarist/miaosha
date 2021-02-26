package com.miaoshaproject.error;

/**
 * @author shkstart
 * @create 2021-02-10 0:15
 */
public enum EmBusinessError implements CommonError {
    //通用错误类型00001，后期可setErrorMsg修改具体是什么参数不合法
    PARAMETER_VALIDITION_ERROR("参数不合法",10001),
    UNKNOW_ERROR("未知错误",10002),
    //20000开头为用户信息相关错误全局标识
    USER_NOT_EXIST("用户不存在",20001),
    USER_LOGIN_FAIL("用户手机号或密码不正确",20002),
    USER_NOT_LOGIN("用户还未登录",20003),
    //30000开头，交易相关的错误
    STOCK_NOT_ENOUGH("库存不足",30001);
    private String ErrorMsg;
    private int ErrorCode;

    EmBusinessError(String errorMsg, int errorCode) {
        this.ErrorMsg = errorMsg;
        this.ErrorCode = errorCode;
    }

    @Override
    public String getErrorMsg() {
        return ErrorMsg;
    }

    @Override
    public int getErrorCode() {
        return ErrorCode;
    }

    @Override
    public CommonError setErrorMsg(String errorMsg) {
        this.ErrorMsg=errorMsg;
        return this;
    }
}
