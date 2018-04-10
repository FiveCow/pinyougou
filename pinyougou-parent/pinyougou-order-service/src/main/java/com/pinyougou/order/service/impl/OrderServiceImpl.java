package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.common.IdWorker;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojogroup.Cart;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}


	@Autowired
	private IdWorker idWorker;


	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
    private TbOrderItemMapper orderItemMapper;
	@Autowired
	private TbPayLogMapper payLogMapper ;
	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		//订单是需要根据不同的商家进行拆分 一个商家就相当于一个订单
		List<Cart>cartList = (List<Cart>)
				redisTemplate.boundHashOps("cartList").get( order.getUserId() );
		//order 就只是传递基本的信息过来。
        boolean flag=true;
        List<String> orderList = new ArrayList<>();

        double totalPageLogMoney = 0;
		for (Cart cart : cartList) {
			//cart就是一个商家   卖的东西是一个列表 需要循环遍历List<orderItem> list;
            TbOrder order1 = new TbOrder();
            long nextId = idWorker.nextId();
            order1.setOrderId(nextId);//订单的ID
            order1.setUserId(order.getUserId());//从页面传递过来 订单所属的用户ID
            order1.setPaymentType(order.getPaymentType());//支付类型
            order1.setStatus("1");//未付款
            order1.setCreateTime(new Date());
            order1.setUpdateTime(order1.getCreateTime());
            order1.setReceiverAreaName(order.getReceiverAreaName());//收货人地址 tbaddress中的address表
            order1.setReceiverMobile(order.getReceiverMobile());//收货人收货电话
            order1.setReceiver(order.getReceiver());//收货人
            order1.setSourceType(order.getSourceType());//订单来源
            order1.setSellerId(cart.getSellerId());

            //循环订单明细列表
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            //实付金额 这个商家的实付金额 是明细总金额的累积之和。
            double sellerMoney = 0;
            for (TbOrderItem orderItem : orderItemList) {
                orderItem.setId(idWorker.nextId());
                orderItem.setOrderId(nextId);
                orderItem.setSellerId(cart.getSellerId());
                sellerMoney+=orderItem.getTotalFee().doubleValue();
                orderItemMapper.insertSelective(orderItem);
            }
            order1.setPayment(new BigDecimal(sellerMoney));//设置每一个订单的实付金额
            int selective = orderMapper.insertSelective(order1);
            if(selective<=0){
                flag = false;
            }

			orderList.add(nextId+"");
			totalPageLogMoney+=sellerMoney;//设置总金额
        }

        //插入支付日志表
		//判断如果是微信支付
		if("1".equals(order.getPaymentType())){
			TbPayLog tbPayLog = new TbPayLog();
			tbPayLog.setCreateTime(new Date());
			tbPayLog.setOrderList(orderList.toString().replace("[","").replace("]",""));//订单ID的列表以逗号分隔
			tbPayLog.setOutTradeNo(idWorker.nextId()+"");//支付订单号
			tbPayLog.setPayType("1");//微信支付
			tbPayLog.setTotalFee((long)totalPageLogMoney*100);//设置付款总金额
			tbPayLog.setTradeState("0");//未支付
			//tbPayLog.setTransactionId();//没有 支付成功之后才有
			//tbPayLog.setPayTime(); 支付成功之后才有
			tbPayLog.setUserId(order.getUserId());
			//插入支付日志表
			payLogMapper.insertSelective(tbPayLog);
			//将数据存储到redis中
			redisTemplate.boundHashOps("payLog").put(order.getUserId(),tbPayLog);//放入redis中
		}

        if(flag){
            redisTemplate.boundHashOps("cartList").delete(order.getUserId());
        }


	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public TbPayLog searchPayLogFromRedis(String userId) {
		return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
	}

	@Override
	public void updateOrderStatus(String out_trade_no, String transaction_id) {
		//1.修改支付日志状态
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		payLog.setPayTime(new Date());
		payLog.setTradeState("1");//已支付
		payLog.setTransactionId(transaction_id);//交易号
		payLogMapper.updateByPrimaryKey(payLog);
		//2.修改订单状态
		String orderList = payLog.getOrderList();//获取订单号列表
		String[] orderIds = orderList.split(",");//获取订单号数组

		for(String orderId:orderIds){
			TbOrder order = orderMapper.selectByPrimaryKey( Long.parseLong(orderId) );
			if(order!=null){
				order.setStatus("2");//已付款
				orderMapper.updateByPrimaryKey(order);
			}
		}
		//清除redis缓存数据
		redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());

	}

}
