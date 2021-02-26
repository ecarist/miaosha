package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.UserDOMapper;
import com.miaoshaproject.dao.UserPasswordDOMapper;
import com.miaoshaproject.dataobject.UserDO;
import com.miaoshaproject.dataobject.UserPasswordDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import com.miaoshaproject.validator.ValidationImpl;
import com.miaoshaproject.validator.ValidationResult;
import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BISchemaBinding;
import org.apache.bcel.generic.IF_ACMPEQ;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.internal.engine.ValidatorImpl;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolation;
import java.beans.Beans;
import java.util.Set;

/**
 * @author shkstart
 * @create 2021-02-09 22:45
 */
@Service
public class UserServiceImpl implements UserService {
    //通过id获取用户的方法
    @Autowired
    private UserDOMapper userDOMapper;
    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;
    @Autowired
    private ValidationImpl validator;

    @Override
    public UserModel getUserById(Integer id) {
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if(userDO==null)return null;
        //通过用户id去密码表里获取用户的密码信息，返回密码实例对象，以便后续组装成model对象
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        //拿着上面两个信息组装成一个model对象返回给controller层
        return convertFromDataObject(userDO,userPasswordDO);
    }

    @Override
    public UserModel validateLogin(String telphone, String ecrptPassword) throws BusinessException {
        //通过手机号找到用户信息---mapper里面自定义一个通过手机号获取用户信息的方法
        UserDO userDO = userDOMapper.selectByTelphone(telphone);
        if(userDO==null){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        //否则如果存在，则组成成usermodel
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        UserModel userModel = convertFromDataObject(userDO, userPasswordDO);

        //比对用户输入的密码是否和数据库内加密的密码匹配
        if (!com.alibaba.druid.util.StringUtils.equals(ecrptPassword,userModel.getEcrptPassword())){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        //如果密码一致，则登录成功！
        return userModel;
    }

    @Override
    @Transactional//加此注解是为了保证userDOMapper.insertSelective(userDO)和userPasswordDOMapper.insertSelective(userPasswordDO)
    //操作在同一个事物里，即用户信息写入数据库user_info表和用户密码写入数据库的password表具有原子性，同时发生，要么同时回滚
    public void register(UserModel userModel) throws BusinessException {
        //后端校验---使得代码具有很好的健壮性，要严谨
        if (userModel==null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDITION_ERROR);
        }
//        if (StringUtils.isEmpty(userModel.getName())
//                ||userModel.getAge()==null
//                ||userModel.getGender()==null
//                ||StringUtils.isEmpty(userModel.getTelphone())
//                ){
//            throw new BusinessException(EmBusinessError.PARAMETER_VALIDITION_ERROR);
//        }

        //使用validator校验userModel的各个属性是否符合校验规则
        ValidationResult result = validator.validate(userModel);
        //验证userModel，如果有错误，会将这个boolean置为true
        if(result.isHasErrors()){
            //抛异常，封装错误信息
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDITION_ERROR,result.getErrMsg());
        }
        //否则就要往数据库里面添加用户数据了
        UserDO userDO = converFromModel(userModel);
        //此处正式插入数据库，用的是 insertselective而不是insert？原因：
        //insert不会对插入的字段判空，如果插入的字段是null则会用null覆盖掉数据库中的默认值
        //而insertSelective会先判空，不为空才会覆盖数据库里的值，否则不覆盖，数据库里是啥就是啥
        //此处还有一个数据库设计的小tips:
        //设计数据库时，尽量不要让字段可以为null,即选中not null的选项，意义：1.java程序处理空指针时非常脆弱；2.null字段只在程序级别有意义，
        // 对于用户来说，null字段没有任何意义，不如直接用默认值（-1，“”--空字符串）来表示---3-10

        //在此处做手机号重复注册校验
        try {
            userDOMapper.insertSelective(userDO);
        }catch (DuplicateKeyException ex){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDITION_ERROR,"手机号重复注册！");
        }
        //赋值给model对象id!!!!!!!很重要
        userModel.setId(userDO.getId());

        UserPasswordDO userPasswordDO=convertPasswordFromModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);


    }
    //实现model转为userdo的方法
    private UserDO converFromModel(UserModel userModel){
        if(userModel==null)return null;
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel,userDO);
        return userDO;

    }
    //实现model转为userpassworddo的方法
    private UserPasswordDO convertPasswordFromModel(UserModel userModel){
        if(userModel==null)return null;
        UserPasswordDO userPasswordDO=new UserPasswordDO();
        userPasswordDO.setEcrptPassword(userModel.getEcrptPassword());
        userPasswordDO.setUserId(userModel.getId());
        return userPasswordDO;
    }
    //实现userdo+userpassworddo转为userModel的方法
    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO){
        if(userDO==null)return null;
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO,userModel);
        if(userPasswordDO!=null){
            userModel.setEcrptPassword(userPasswordDO.getEcrptPassword());
        }
        return userModel;

    }
}
