package com.wangzhoujun17.test;

import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import com.wangzhoujun17.mapper.OrderDetailMapper;
import com.wangzhoujun17.mapper.OrdersMapper;
import com.wangzhoujun17.mapper.ProductMapper;
import com.wangzhoujun17.po.OrderDetails;
import com.wangzhoujun17.po.Orders;
import com.wangzhoujun17.po.Products;
import com.wangzhoujun17.service.OrdersService;
import com.wangzhoujun17.utils.MyBatisUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 订单业务类测试：验证跨表操作的事务原子性。
 * 覆盖两个此前缺失的场景：
 *   1. 删除带详情的订单（此前会因外键约束失败）
 *   2. 下单时扣减库存、库存不足时回滚
 */
public class OrdersServiceTest {

    private final OrdersService ordersService = new OrdersService();

    /**
     * 删除带详情的订单应成功：详情与订单一并删除。
     * <p>
     * 关键是先验证「直接删会失败」——否则本测试即使在 service 逻辑损坏时也会通过。
     * 这里直接操作种子数据中的订单1（自带2条详情），而不是自己造的订单。
     */
    @Test
    public void deleteOrdersWithDetailsTest() {
        SqlSession session = MyBatisUtils.getSession();
        OrderDetailMapper detailMapper = session.getMapper(OrderDetailMapper.class);
        OrdersMapper ordersMapper = session.getMapper(OrdersMapper.class);

        // 前置：订单1 存在且有详情
        assertNotNull("订单1应存在", ordersMapper.findOrdersById(1));
        int detailCount = detailMapper.findDetailsByOrderId(1).size();
        assertTrue("订单1应有详情记录", detailCount > 0);

        // 对照：直接用 mapper 删（绕过 service）应因外键约束失败
        try {
            ordersMapper.deleteOrders(1);
            session.commit();
            fail("直接删除带详情的订单应因外键约束失败——"
                    + "若此处未失败，说明 service 的存在价值未被验证");
        } catch (RuntimeException e) {
            session.rollback();
            assertTrue("异常应是外键约束错误",
                    e.getMessage() != null
                            && e.getMessage().contains("foreign key constraint"));
        }
        session.close();

        // 正面路径：走 service 应成功
        boolean deleted = ordersService.deleteOrdersWithDetails(1);
        assertTrue("带详情的订单应删除成功", deleted);

        // 后置断言：订单与详情都已不存在
        session = MyBatisUtils.getSession();
        detailMapper = session.getMapper(OrderDetailMapper.class);
        ordersMapper = session.getMapper(OrdersMapper.class);

        assertNull("订单应已删除", ordersMapper.findOrdersById(1));
        assertTrue("订单详情应已一并删除",
                detailMapper.findDetailsByOrderId(1).isEmpty());
        session.close();

        // 还原种子数据：订单1 及其2条详情。
        // 必须显式指定 id，否则自增会生成新 id，导致其他测试
        // （如 OrderDetailCrudTest 依赖 orderdetails.id）失败。
        session = MyBatisUtils.getSession();
        ordersMapper = session.getMapper(OrdersMapper.class);
        detailMapper = session.getMapper(OrderDetailMapper.class);

        Orders o = new Orders();
        o.setId(1);
        o.setUserId(1);
        o.setOrderDate(java.sql.Date.valueOf("2026-09-20"));
        ordersMapper.insertWithId(o);

        detailMapper.insertDetailWithId(1, 1, 1, 2);
        detailMapper.insertDetailWithId(2, 1, 3, 1);
        session.commit();
        session.close();
    }

    /** 删除不存在的订单应返回 false，且不报错 */
    @Test
    public void deleteNonExistentOrderTest() {
        boolean deleted = ordersService.deleteOrdersWithDetails(999999);
        assertFalse("删除不存在的订单应返回 false", deleted);
    }

    /** 下单应扣减库存，并写入订单与详情 */
    @Test
    public void placeOrderTest() {
        SqlSession session = MyBatisUtils.getSession();
        ProductMapper productMapper = session.getMapper(ProductMapper.class);
        int stockBefore = productMapper.findProductById(2).getStock();
        session.close();

        Integer orderId = ordersService.placeOrder(1, 2, 3);
        assertNotNull("下单应返回订单ID", orderId);

        session = MyBatisUtils.getSession();
        productMapper = session.getMapper(ProductMapper.class);
        OrderDetailMapper detailMapper = session.getMapper(OrderDetailMapper.class);
        OrdersMapper ordersMapper = session.getMapper(OrdersMapper.class);

        int stockAfter = productMapper.findProductById(2).getStock();
        assertEquals("库存应扣减3", stockBefore - 3, stockAfter);

        Orders orders = ordersMapper.findOrdersById(orderId);
        assertNotNull("订单应已创建", orders);
        assertEquals("订单应归属用户1", Integer.valueOf(1), orders.getUserId());

        List<OrderDetails> details = detailMapper.findDetailsByOrderId(orderId);
        assertEquals("应写入1条订单详情", 1, details.size());
        assertEquals("详情对应的商品应为2",
                Integer.valueOf(2), details.get(0).getProductId());

        session.close();

        // 清理：删除本次测试创建的订单，恢复库存
        ordersService.deleteOrdersWithDetails(orderId);
        restoreStock(2, stockBefore);
    }

    /** 库存不足时下单应失败并回滚，库存与订单详情均不变 */
    @Test
    public void placeOrderWithInsufficientStockTest() {
        SqlSession session = MyBatisUtils.getSession();
        ProductMapper productMapper = session.getMapper(ProductMapper.class);
        int stockBefore = productMapper.findProductById(1).getStock();
        int ordersBefore = session.getMapper(OrdersMapper.class).findAllOrders().size();
        session.close();

        // 购买远超库存的数量
        try {
            ordersService.placeOrder(1, 1, stockBefore + 1000);
            fail("库存不足时应抛出异常");
        } catch (IllegalStateException e) {
            assertTrue("异常信息应说明库存不足", e.getMessage().contains("库存不足"));
        }

        session = MyBatisUtils.getSession();
        productMapper = session.getMapper(ProductMapper.class);
        int stockAfter = productMapper.findProductById(1).getStock();
        int ordersAfter = session.getMapper(OrdersMapper.class).findAllOrders().size();
        session.close();

        assertEquals("库存不应变化", stockBefore, stockAfter);
        assertEquals("不应创建订单", ordersBefore, ordersAfter);
    }

    /** 购买数量非法时应抛 IllegalArgumentException */
    @Test
    public void placeOrderWithInvalidQuantityTest() {
        try {
            ordersService.placeOrder(1, 1, 0);
            fail("数量为0应抛出异常");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("必须大于0"));
        }

        try {
            ordersService.placeOrder(1, 1, -5);
            fail("数量为负应抛出异常");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("必须大于0"));
        }
    }

    /** 把指定商品的库存恢复为给定值，用于测试收尾 */
    private void restoreStock(Integer productId, Integer stock) {
        SqlSession session = MyBatisUtils.getSession();
        Products p = session.getMapper(ProductMapper.class).findProductById(productId);
        p.setStock(stock);
        session.getMapper(ProductMapper.class).updateProduct(p);
        session.commit();
        session.close();
    }
}
