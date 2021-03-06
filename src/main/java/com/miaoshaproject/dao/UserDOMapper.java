package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.UserDO;


public interface UserDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_info
     *
     * @mbg.generated Tue Feb 09 21:58:30 CST 2021
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_info
     *
     * @mbg.generated Tue Feb 09 21:58:30 CST 2021
     */
    int insert(UserDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_info
     *
     * @mbg.generated Tue Feb 09 21:58:30 CST 2021
     */
    int insertSelective(UserDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_info
     *
     * @mbg.generated Tue Feb 09 21:58:30 CST 2021
     */
    UserDO selectByPrimaryKey(Integer id);
//    以下是自定义的通过手机号获取用户信息的select方法
    UserDO selectByTelphone(String telphone);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_info
     *
     * @mbg.generated Tue Feb 09 21:58:30 CST 2021
     */
    int updateByPrimaryKeySelective(UserDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_info
     *
     * @mbg.generated Tue Feb 09 21:58:30 CST 2021
     */
    int updateByPrimaryKey(UserDO record);
}