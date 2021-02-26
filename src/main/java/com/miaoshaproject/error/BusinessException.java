package com.miaoshaproject.error;

/**
 * @author shkstart
 * @create 2021-02-10 0:25
 */
//设计模式：包装器 业务异常类实现
public class BusinessException extends Exception implements CommonError {
//内部关联一个commonerror,实际上是关联了这个接口的具体实现类---->enum类
    private CommonError commonError;

    //直接接收enum类的传参，用于构造业务异常
    public BusinessException(CommonError commonError) {
        //exception类自身还要初始化，所以必须调用super???但是我感觉去掉也可以？？
        super();
        this.commonError = commonError;
    }
    //接收自定义errmsg的方式构造业务异常--->通用错误类型的定制化改变errormessage的方式
    public BusinessException(CommonError commonError,String errorMsg) {
        this.commonError = commonError;
        this.commonError.setErrorMsg(errorMsg);
    }


    @Override
    public String getErrorMsg() {
        return commonError.getErrorMsg();
    }

    @Override
    public int getErrorCode() {
        return commonError.getErrorCode();
    }

    @Override
    public CommonError setErrorMsg(String errorMsg) {
        commonError.setErrorMsg(errorMsg);
        return this;
    }
}
