package com.miaoshaproject.controller;


import com.miaoshaproject.controller.viewobject.ItemVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.model.ItemModel;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author shkstart
 * @create 2021-02-26-11:31
 */
@Controller("item")
@RequestMapping("/item")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")//跨域请求ajax.让response headers里面加上一个Access-Control-Allow-Origin: *
public class ItemController extends BaseController {
    //封装service请求用来创建商品------创建理念：尽量让controller层简单，而service层复杂
    @Autowired
    private ItemService itemService;
    //创建商品的controller
    @RequestMapping(value = "/create",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createItem(@RequestParam(name = "title")String title,
                                       @RequestParam(name="description") String description,
                                       @RequestParam(name="price") BigDecimal price,
                                       @RequestParam(name="stock") Integer stock,
                                       @RequestParam(name="imgUrl") String imgUrl) throws BusinessException {
        //封装service请求用来创建商品
        //尽量让Controller层简单，让Service层负责，把服务逻辑尽可能聚合在Service层内部，实现流转处理
        //创建给service层的
        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setDescription(description);
        itemModel.setPrice(price);
        itemModel.setStock(stock);
        itemModel.setImgUrl(imgUrl);

        //调用service层的create方法创建商品
        ItemModel itemModelForReturn = itemService.createItem(itemModel);
        //返回给前端的是ItemVO对象
        ItemVO itemVO = convertVOFromModel(itemModelForReturn);


        return CommonReturnType.create(itemVO);

    }

    //商品详情页浏览的controller
    @RequestMapping(value = "/get",method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType getItem(@RequestParam(name = "id")Integer id){
        ItemModel itemModel = itemService.getItemById(id);
        ItemVO itemVO = convertVOFromModel(itemModel);

        return CommonReturnType.create(itemVO);
    }

    //商品列表页面浏览的controller
    @RequestMapping(value = "/list",method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType listItem(){
        List<ItemModel> itemModels = itemService.listItem();
        //使用Java8的stream api 将list内的itemModel转为ItemVO
        List<ItemVO> itemVOList = itemModels.stream().map(itemModel -> {
            ItemVO itemVO = convertVOFromModel(itemModel);
            return itemVO;
        }).collect(Collectors.toList());
        return CommonReturnType.create(itemVOList);

    }
    //返回给前端的是ItemVO对象
    private ItemVO convertVOFromModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel, itemVO);
        if(itemModel.getPromoModel()!=null){
            //有正在进行或即将开始的秒杀活动
            //设置秒杀活动状态
            itemVO.setPromoStatus(itemModel.getPromoModel().getStatus());
            //设置秒杀活动id
            itemVO.setPromoId(itemModel.getPromoModel().getId());
            //设置秒杀活动开始时间
            itemVO.setStartDate(itemModel.getPromoModel().getStartDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
            //秒杀价格
            itemVO.setPromoPrice(itemModel.getPromoModel().getPromoItemPrice());
        }else{
            //该商品没有秒杀活动
            itemVO.setPromoStatus(0);
        }
        return itemVO;
    }
}
