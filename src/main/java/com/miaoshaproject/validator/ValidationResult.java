package com.miaoshaproject.validator;

import com.sun.deploy.util.StringUtils;

import javax.print.DocFlavor;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shkstart
 * @create 2021-02-25-22:40
 */
//validator结果的封装
public class ValidationResult {
    //校验结果--是否有错--布尔值
    private boolean hasErrors=false;
    //弄一个map存放错误信息
    private Map<String,String> errorMsgMap=new HashMap<>();

    public boolean isHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public Map<String, String> getErrorMsgMap() {
        return errorMsgMap;
    }

    public void setErrorMsgMap(Map<String, String> errorMsgMap) {
        this.errorMsgMap = errorMsgMap;
    }
    //实现通用的通过格式化字符串信息获取错误结果的msg方法
    public String getErrMsg(){

        return org.apache.commons.lang.StringUtils.join(errorMsgMap.values().toArray(),",");
    }
}
