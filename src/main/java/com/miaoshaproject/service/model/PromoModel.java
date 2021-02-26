package com.miaoshaproject.service.model;

import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * @author shkstart
 * @create 2021-02-26-21:45
 */
//秒杀活动模型
public class PromoModel {

    //秒杀活动状态 1 还未开始 2 进行中 3 已经结束
    //此字段和数据库字段没有任何关系！！！
    private Integer status;



    private Integer id;
    //秒杀活动的名称
    private String promoName;
    //秒杀开始时间---->推荐使用joda-time,而不是javautil的date
//    只有到达开始时间，秒杀商品才能被抢购！
    private DateTime startDate;
    //秒杀活动的结束时间
    private DateTime endDate;
    //秒杀活动的适用商品--->简化：一个活动只能适用一个id
    private Integer itemId;
    //秒杀商品的价格
    private BigDecimal promoItemPrice;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPromoName() {
        return promoName;
    }

    public void setPromoName(String promoName) {
        this.promoName = promoName;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getPromoItemPrice() {
        return promoItemPrice;
    }

    public void setPromoItemPrice(BigDecimal promoItemPrice) {
        this.promoItemPrice = promoItemPrice;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(DateTime endDate) {
        this.endDate = endDate;
    }
}
