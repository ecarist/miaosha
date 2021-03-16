package com.miaoshaproject.service;

import com.miaoshaproject.service.model.PromoModel;

/**
 * @author 
 * @create 2021-02-26-22:20
 */
public interface PromoService {
    //根据商品id获取该商品即将进行或正在进行的秒杀信息
    PromoModel getPromoByItemId(Integer itemId);
    // 活动发布
    void publishPromo(Integer promoId);

    // 生成秒杀用的令牌
    String generateSecondKillToken(Integer promoId, Integer itemId, Integer userId);
}
