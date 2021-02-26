package com.miaoshaproject.error;

/**
 * @author shkstart
 * @create 2021-02-10 0:12
 */
public interface CommonError {
    public String getErrorMsg();
    public int getErrorCode();
    public CommonError setErrorMsg(String errorMsg);
}
