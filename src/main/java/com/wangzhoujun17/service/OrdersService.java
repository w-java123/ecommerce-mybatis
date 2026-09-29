package com.wangzhoujun17.service;

import java.util.Date;

import org.apache.ibatis.session.SqlSession;

import com.wangzhoujun17.mapper.OrderDetailMapper;
import com.wangzhoujun17.mapper.OrdersMapper;
import com.wangzhoujun17.mapper.ProductMapper;
import com.wangzhoujun17.po.OrderDetails;
import com.wangzhoujun17.po.Orders;
import com.wangzhoujun17.po.Products;
import com.wangzhoujun17.utils.MyBatisUtils;

/**
 * 订单业务类。
 * <p>
 * 涉及多张表、需要保证原子性的操作放在这里。原因是 MyBatis 的 Mapper 接口
 * 一个方法对应一条 SQL，而「下单」「删除订单」都需要操作多张表，
 * 必须由同一个 SqlSession 承载并通过一次 commit 提交，才能保证事务原子性。
 */
public class OrdersService {

    /**
     * 下单：创建订单、写入订单详情、扣减商品库存，三步在同一事务内完成。
     * <p>
     * 库存扣减使用 {@code where stock >= #{quantity}} 条件更新，
     * 若受影响行数为 0 说明库存不足，此时回滚整个下单操作。
     *
     * @param userId    下单用户ID
     * @param productId 商品ID
     * @param quantity  购买数量
     * @return 新建订单的ID
     * @throws IllegalStateException 库存不足时抛出
     */
    public Integer placeOrder(Integer userId, Integer productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("购买数量必须大于0");
        }

        SqlSession session = MyBatisUtils.getSession();
        try {
            OrdersMapper ordersMapper = session.getMapper(OrdersMapper.class);
            OrderDetailMapper detailMapper = session.getMapper(OrderDetailMapper.class);
            ProductMapper productMapper = session.getMapper(ProductMapper.class);

            // 1. 扣减库存：库存不足时影响行数为 0
            int stockRows = productMapper.reduceStock(productId, quantity);
            if (stockRows == 0) {
                session.rollback();
                Products product = productMapper.findProductById(productId);
                if (product == null) {
                    throw new IllegalStateException("商品不存在，商品ID：" + productId);
                }
                throw new IllegalStateException(
                        "库存不足，商品：" + product.getName()
                                + "，当前库存：" + product.getStock()
                                + "，本次购买：" + quantity);
            }

            // 2. 创建订单，主键回填到 orders.id
            Orders orders = new Orders();
            orders.setUserId(userId);
            orders.setOrderDate(new Date());
            ordersMapper.addOrders(orders);

            // 3. 写入订单详情，建立订单与商品的多对多关系
            OrderDetails detail = new OrderDetails();
            detail.setOrderId(orders.getId());
            detail.setProductId(productId);
            detail.setQuantity(quantity);
            detailMapper.addOrderDetail(detail);

            session.commit();
            return orders.getId();
        } catch (RuntimeException e) {
            session.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    /**
     * 删除订单及其全部订单详情。
     * <p>
     * orderdetails.order_id 外键指向 orders.id 且未设置级联删除，
     * 因此直接删除仍有详情的订单会触发外键约束错误。
     * 本方法在同一事务内先删详情、再删订单，避免该问题。
     *
     * @param orderId 订单ID
     * @return true 表示订单存在且已成功删除；false 表示订单不存在
     */
    public boolean deleteOrdersWithDetails(Integer orderId) {
        SqlSession session = MyBatisUtils.getSession();
        try {
            OrdersMapper ordersMapper = session.getMapper(OrdersMapper.class);
            OrderDetailMapper detailMapper = session.getMapper(OrderDetailMapper.class);

            // 先校验订单是否存在，避免删了子表却删不到主表
            Orders orders = ordersMapper.findOrdersById(orderId);
            if (orders == null) {
                session.rollback();
                return false;
            }

            // 顺序不可颠倒：先删子表 orderdetails，再删主表 orders
            detailMapper.deleteByOrderId(orderId);
            ordersMapper.deleteOrders(orderId);

            // 两条 SQL 都成功才提交，任一步抛异常则整体回滚
            session.commit();
            return true;
        } catch (RuntimeException e) {
            session.rollback();
            throw e;
        } finally {
            session.close();
        }
    }
}
