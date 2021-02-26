package com.miaoshaproject.service.model;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
//import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author shkstart
 * @create 2021-02-09 22:48
 */
public class UserModel {
    private Integer id;
    //notblank和notnull不一样，前者是不能为空且不能为null，后者是不能为null
    @NotBlank(message = "用户名不可为空")
    private String name;
    @NotNull(message = "性别不能不填写")
    private Byte gender;
    @NotNull(message = "年龄不能不填写")
    @Min(value = 0,message = "年龄必须大于0")
    @Max(value = 150,message = "年龄不能超过150岁")
    private Integer age;
    @NotBlank(message = "手机号不能为空")
    private String telphone;
    private String registerMode;
    private String thirdPartyId;
    //用户密码，这个属性是不在最初的UserDO中的，要在查出该用户对应的密码，封装进去，才是一个完整的User实体该有的属性。
    @NotBlank(message = "密码不能为空")
    private String ecrptPassword;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Byte getGender() {
        return gender;
    }

    public void setGender(Byte gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getTelphone() {
        return telphone;
    }

    public void setTelphone(String telphone) {
        this.telphone = telphone;
    }

    public String getRegisterMode() {
        return registerMode;
    }

    public void setRegisterMode(String registerMode) {
        this.registerMode = registerMode;
    }

    public String getThirdPartyId() {
        return thirdPartyId;
    }

    public void setThirdPartyId(String thirdPartyId) {
        this.thirdPartyId = thirdPartyId;
    }

    public String getEcrptPassword() {
        return ecrptPassword;
    }

    public void setEcrptPassword(String ecrptPassword) {
        this.ecrptPassword = ecrptPassword;
    }
}
