package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.ItemModel;

import java.util.List;

/**
 * @author shkstart
 * @create 2021-02-26-10:50
 */
public interface ItemService {
    //创建商品
    ItemModel createItem(ItemModel itemModel) throws BusinessException;
    //商品列表浏览
    List<ItemModel> listItem();
    //商品详情浏览
    ItemModel getItemById(Integer id);
    //减库存的操作
    boolean decreaseStock(Integer itemId, Integer amount);
    //用户下单成功后商品销量增加
    void increaseSales(Integer itemId,Integer amount)throws BusinessException;



}
