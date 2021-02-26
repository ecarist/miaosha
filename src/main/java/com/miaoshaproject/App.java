package com.miaoshaproject;

import com.miaoshaproject.dao.UserDOMapper;
import com.miaoshaproject.dataobject.UserDO;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world!
 *
 */
//设置本类为主启动类，被spring托管，自动扫描本类所在的包及其子包,所以不加后面的scanbasepackages也可以
@SpringBootApplication(scanBasePackages = {"com.miaoshaproject"})
//mapperscan可以使得每次不用在每个dao上面加mapper注解了
@MapperScan("com.miaoshaproject.dao")
@RestController
public class App
{
    @Autowired
    private UserDOMapper userDOMapper;

    @RequestMapping("/")
    public String home(){
    UserDO userDO = userDOMapper.selectByPrimaryKey(1);
    if(userDO==null){
        return "用户信息不存在";
    }
    else return userDO.getName();
}






    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        SpringApplication.run(App.class,args);

    }
}
