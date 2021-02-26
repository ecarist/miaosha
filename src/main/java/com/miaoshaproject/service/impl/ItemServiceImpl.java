package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.ItemDOMapper;
import com.miaoshaproject.dao.ItemStockDOMapper;
import com.miaoshaproject.dataobject.ItemDO;
import com.miaoshaproject.dataobject.ItemStockDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.ItemModel;

import com.miaoshaproject.service.model.PromoModel;
import com.miaoshaproject.validator.ValidationImpl;
import com.miaoshaproject.validator.ValidationResult;
import org.hibernate.validator.internal.engine.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolation;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author shkstart
 * @create 2021-02-26-10:52
 */
@Service
public class ItemServiceImpl implements ItemService {


    @Autowired
    private ValidationImpl validator;
    @Autowired
    private ItemDOMapper itemDOMapper;
    @Autowired
    private ItemStockDOMapper itemStockDOMapper;
    //注入秒杀活动组件
    @Autowired
    private PromoService promoService;
    //创建商品，需要事务，在方法上添加
    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        //三步
        //1.入参校验
        ValidationResult result = validator.validate(itemModel);
        if(result.isHasErrors()){
           throw new BusinessException(EmBusinessError.PARAMETER_VALIDITION_ERROR,result.getErrMsg());
        }
        //2.写入数据库
        //itemmodel---转化为数据库实例映射---itemdo和itemstockdo
        //将ItemDo写入数据库
        ItemDO itemDO=this.convertItemFromItemModel(itemModel);

        //写入后返回了itemDo的id
        itemDOMapper.insertSelective(itemDO);
        //将id给itemModel
        itemModel.setId(itemDO.getId());
        //此时的itemModel已经具有id了
        ItemStockDO itemStockDO=this.convertItemStockFromItemModel(itemModel);
        //加入库存表
        itemStockDOMapper.insertSelective(itemStockDO);
        //3.返回创建实例
        return this.getItemById(itemModel.getId());
    }

    //将ItemModel转为ItemDo的转换方法
    private ItemDO convertItemFromItemModel(ItemModel itemModel){
        //判空处理
        if(itemModel==null){
            return null;
        }
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel,itemDO);
        //UserModel中的price是BigDecimal类型而不用Double，Double在java内部传到前端，会有精度问题，不精确
        //有可能1.9，显示时是1.999999，为此在Service层，将price定为比较精确的BigDecimal类型
        //但是在拷贝到Dao层时，存入的是Double类型，拷贝方法对应类型不匹配的属性，不会进行拷贝。

        //在拷贝完，将BigDecimal转为Double，再set进去
        //转为double
        itemDO.setPrice(itemModel.getPrice().doubleValue());
        return itemDO;
    }

    //从itemModel转为ItemStockDo方法
    private ItemStockDO convertItemStockFromItemModel(ItemModel itemModel){
        if(itemModel == null){
            return null;
        }
        ItemStockDO itemStockDo = new ItemStockDO();
        itemStockDo.setItemId(itemModel.getId());
        itemStockDo.setStock(itemModel.getStock());
        return itemStockDo;
    }

    @Override
    public List<ItemModel> listItem() {
        List<ItemDO> itemDOList = itemDOMapper.listItem();
        List<ItemModel> itemModelList = itemDOList.stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = this.convertItemModlFromDataObject(itemDO, itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }
    /**
     * 根据商品id查询商品
     * @param id
     * @return
     * 先查出itemDo
     * 再查出对应的stock，封装成itemModel
     */
    @Override
    public ItemModel getItemById(Integer id) {
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if(itemDO==null)return null;
        //根据item的id获得库存表中的库存数量
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
        //把itemdo和itemstockdo转化成itemmodel
        ItemModel itemModel=this.convertItemModlFromDataObject(itemDO,itemStockDO);
        //获取活动商品信息
        PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());

        //如果存在该商品秒杀对象并且秒杀状态不等于3,说明秒杀有效
        if(promoModel!=null && promoModel.getStatus().intValue()!=3){
            //将秒杀对象聚合进ItemModel，将该商品和秒杀对象关联起来
            itemModel.setPromoModel(promoModel);
        }
        return itemModel;
    }
    //将dataobject转换成Model领域模型
    private ItemModel convertItemModlFromDataObject(ItemDO itemDo,ItemStockDO itemStockDo){
            if(itemDo==null)return null;
            ItemModel itemModel = new ItemModel();
            BeanUtils.copyProperties(itemDo,itemModel);
            itemModel.setPrice(new BigDecimal(itemDo.getPrice()));
            if(itemStockDo!=null) {
                itemModel.setStock(itemStockDo.getStock());
            }
            return itemModel;
    }
    //扣减库存
    @Override
    @Transactional//涉及库存表操作，要保证事务一致性
    public boolean decreaseStock(Integer itemId, Integer amount) {
        /*
            item商品表大部分用于查询，查询对应的商品信息
            库存表，在某些高压力的情况下做降级
            比如在微服务下，库存服务可以拆为item的展示服务（item表）和item的库存服务（item_stock表）
            这个item的库存服务独立出来，专门进行库存减操作。
            目前只操作item_stock表，为保证冻结操作的原子性，对item_stock表加行锁，针对某一条记录进行加行锁，减掉对应的库存
            看减完后是否还大于表中库存。
            修改itemStockDoMapper映射文件，修改sql语句
         */
        //返回影响的条目数
        //sql成功执行返回的影响条目数不一定为1，如果购买数量大于库存，超卖，sql语句也会执行，但返回的就是0
//        innodb默认加行锁
//     *  返回值是影响的行数
//     *  正常扣减，返回值是1；否则返回值是0
        int affectRow = itemStockDOMapper.decreaseStock(itemId, amount);
        if(affectRow>0){
            //更新库存成功
            return true;
        }else{
            return false;
        }


    }

    //用户下单成功，商品销量增加
    //暂时不考虑支付情况，只要商品落单成功，销量增加
    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) throws BusinessException {
        itemDOMapper.increaseSales(itemId,amount);


    }
}
