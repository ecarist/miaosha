package com.miaoshaproject.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;
/**
 * @author shkstart
 * @create 2021-02-25-22:46
 */
@Component
public class ValidationImpl implements InitializingBean {
    private Validator validator;

    //拿着创建好的校验器实例去实现校验方法并返回校验结果
    public ValidationResult validate(Object bean){
        //创建自定义的验证结果对象，用来封装是否错误和错误信息
        ValidationResult result = new ValidationResult();

        //调验证器的验证方法，返回一个set----如果bean中有不符合validator定义的@注解中要求的参数，就会有错误，这个set中就会有值
        Set<ConstraintViolation<Object>> constraintViolationSet = validator.validate(bean);
        // set中有值----->
        if(constraintViolationSet.size()>0){
            //set中有值----->表示bean中有错误
            result.setHasErrors(true);
            //遍历set拿到错误信息-----for each是java8特有的表达式----拿到哪个字段发生了什么错误
            constraintViolationSet.forEach(constraintViolation->{
                //获取bean的属性上注解定义的错误信息
                String errMsg = constraintViolation.getMessage();
                //获取是哪个属性有错误
                String propertyName = constraintViolation.getPropertyPath().toString();
                //将错误信息和对应的属性放入错误map里
                result.getErrorMsgMap().put(propertyName,errMsg);
            });
        }
        //将这个map返回
        return result;
    }

    //spring在bean初始化属性之后，回调，调用这个方法，这个方法实例化了一个实现了javax的validator接口的校验器实例！！！
    @Override
    public void afterPropertiesSet() throws Exception {
        //将hibernate validator通过工厂的初始化方法使其实例化
        //貌似hibernate是在这里注入的？？？
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();


    }
}
