package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.OrderDOMapper;
import com.miaoshaproject.dao.SequenceDOMapper;
import com.miaoshaproject.dataobject.OrderDO;
import com.miaoshaproject.dataobject.SequenceDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.OrderModel;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author shkstart
 * @create 2021-02-26-15:45
 */
@Service
public class OrderServiceImpl implements OrderService {
    //service分离的好处
    //service之间可以互相关联互相调用，但所有逻辑都封装在service内部完成了
    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;
    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Autowired
    private OrderDOMapper orderDoMapper;

    @Override
    @Transactional//保证创建订单在同一个事务当中
    public OrderModel createOrder(Integer userId, Integer itemId,Integer promoId ,Integer amount) throws BusinessException {
//  创建订单的步骤
//  1.校验下单状态：商品是否存在---->这些校验无法通过hibernate的注解validiton简单校验。所以单独校验
        //判断商品是否存在
        ItemModel itemModel = itemService.getItemById(itemId);
        if(itemModel==null){
            throw  new BusinessException(EmBusinessError.PARAMETER_VALIDITION_ERROR,"商品信息不存在");
        }
        //判断用户是否合法
        UserModel userModel = userService.getUserById(userId);
        if(userModel==null){
            throw  new BusinessException(EmBusinessError.PARAMETER_VALIDITION_ERROR,"用户信息不存在");
        }

        //判断购买数量是否合法
        if(amount<=0||amount>=99){
            throw  new BusinessException(EmBusinessError.PARAMETER_VALIDITION_ERROR,"商品信息不合法");
        }
        //判断活动信息
        if(promoId!=null){
            // 1校验对应活动是否存在这个适用商品
            //看传过来的秒杀模型id是否和商品模型中聚合的秒杀模型的id一致（该商品有秒杀活动，会将秒杀模型聚合进商品Model）
            if(promoId.intValue()!=itemModel.getPromoModel().getId()){
                throw  new BusinessException(EmBusinessError.PARAMETER_VALIDITION_ERROR,"活动信息不正确");

                //即使id是秒杀模型的id，也不保险，还要校验是不是正在进行的秒杀
            }else if(itemModel.getPromoModel().getStatus()!=2){
                throw  new BusinessException(EmBusinessError.PARAMETER_VALIDITION_ERROR,"秒杀不在进行中");
            }

        }



//  2.落单就减库存，而不是支付减库存

        //采用落单减库存，itemService中提供一个减库存的方法
        boolean result = itemService.decreaseStock(itemId, amount);
        //返回false，库存不够扣的，下单数量大于库存量
        if(!result){
            throw  new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }
        //否则，扣减库存正常开始

//  3.订单入库---存入 order_info表中
        //创建OrderModel对象，封装数据。
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        //如果有秒杀，下单价格是秒杀价格
        if(promoId!=null){
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }else {
            orderModel.setItemPrice(itemModel.getPrice());
        }
        //给订单模型设置秒杀活动id
        orderModel.setPromoId(promoId);
        //订单总价 商品单价X数量
        //先设置单价，设置完就已经明确了这个价格是秒杀价格还是平销价格了，再get出来
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));

        //生成交易流水号（订单号）
        orderModel.setId(generateOrderNo());

        //以下才是正式把用户创建的订单加入数据库中
        OrderDO orderDO = convertFromOrderModel(orderModel);
        orderDoMapper.insertSelective(orderDO);
        //增加商品销量
        itemService.increaseSales(itemId,amount);

//  4.返回前端

        return orderModel;
    }

    //ordermodel转为orderdo的方法
    private OrderDO convertFromOrderModel(OrderModel orderModel){
        if(orderModel==null)return null;
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel,orderDO);
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return  orderDO;
    }
    //创建订单号的方法
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private String generateOrderNo(){
        int sequence=0;
        //16位订单号，
        StringBuilder stringBuilder = new StringBuilder();
        // 前8位时间信息，年月日
        LocalDateTime now = LocalDateTime.now();
        String nowDate=now.format(DateTimeFormatter.ISO_DATE).replace("-","");
        stringBuilder.append(nowDate);
        //中间6位是当天自增序列---->数据库中建一个sequence表
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequence+sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        String sequenceStr = String.valueOf(sequence);
        //不足6位的补0
        for (int i=0;i<6-sequenceStr.length();i++){
            stringBuilder.append(0);
        }
        stringBuilder.append(sequenceStr);

        //最后2位为分库分表位---->分离数据库，只要保证通过userID可以唯一定位到一个数据库的一个表上，现在现不实现，写死
        stringBuilder.append("00");
        return stringBuilder.toString();


    }
}
