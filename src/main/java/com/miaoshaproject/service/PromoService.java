package com.miaoshaproject.service;

import com.miaoshaproject.service.model.PromoModel;

/**
 * @author shkstart
 * @create 2021-02-26-22:20
 */
public interface PromoService {
    //根据商品id获取该商品即将进行或正在进行的秒杀信息
    PromoModel getPromoByItemId(Integer itemId);
}
