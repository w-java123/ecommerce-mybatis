package com.wangzhoujun17.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import com.wangzhoujun17.mapper.OrderDetailMapper;
import com.wangzhoujun17.po.OrderDetails;
import com.wangzhoujun17.utils.MyBatisUtils;

/**
 * 订单详情表 orderdetails（多对多中间表）的完整 CRUD 测试。
 * 测试数据来自 db.sql 种子数据，演示前请先执行 reset-db.bat。
 */
public class OrderDetailCrudTest {

    /** 新增订单详情 */
    @Test
    public void addOrderDetailTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            OrderDetailMapper mapper = session.getMapper(OrderDetailMapper.class);

            OrderDetails detail = new OrderDetails();
            detail.setOrderId(1);
            detail.setProductId(2);
            detail.setQuantity(3);

            int rows = mapper.addOrderDetail(detail);
            session.commit();
            assertEquals("应插入1行", 1, rows);
            assertNotNull("主键应被回填", detail.getId());

            OrderDetails loaded = mapper.findOrderDetailById(detail.getId());
            assertNotNull("插入后应能查到", loaded);
            assertEquals(Integer.valueOf(1), loaded.getOrderId());
            assertEquals(Integer.valueOf(2), loaded.getProductId());
            assertEquals(Integer.valueOf(3), loaded.getQuantity());

            // 清理，避免污染种子数据
            mapper.deleteOrderDetail(detail.getId());
            session.commit();
        } finally {
            session.close();
        }
    }

    /** 根据ID查询订单详情 */
    @Test
    public void findOrderDetailByIdTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            OrderDetailMapper mapper = session.getMapper(OrderDetailMapper.class);

            OrderDetails d = mapper.findOrderDetailById(1);
            assertNotNull("详情1应存在", d);
            assertEquals(Integer.valueOf(1), d.getId());
            assertEquals("详情1属订单1", Integer.valueOf(1), d.getOrderId());
            assertEquals("详情1对应商品1", Integer.valueOf(1), d.getProductId());
            assertEquals("详情1数量应为2", Integer.valueOf(2), d.getQuantity());

            assertNull("不存在的详情应返回null", mapper.findOrderDetailById(999999));
        } finally {
            session.close();
        }
    }

    /** 查询全部订单详情 */
    @Test
    public void findAllOrderDetailsTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            OrderDetailMapper mapper = session.getMapper(OrderDetailMapper.class);

            List<OrderDetails> list = mapper.findAllOrderDetails();
            assertNotNull(list);
            assertEquals("种子数据应有6条详情", 6, list.size());
            for (OrderDetails d : list) {
                assertNotNull("订单ID不应为null", d.getOrderId());
                assertNotNull("商品ID不应为null", d.getProductId());
                assertNotNull("数量不应为null", d.getQuantity());
                assertTrue("数量应大于0", d.getQuantity() > 0);
            }
        } finally {
            session.close();
        }
    }

    /** 按订单ID查询详情列表 */
    @Test
    public void findDetailsByOrderIdTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            OrderDetailMapper mapper = session.getMapper(OrderDetailMapper.class);

            // 种子数据：订单1有2条、订单2有1条、订单3有2条、订单4有1条
            List<OrderDetails> list = mapper.findDetailsByOrderId(1);
            assertNotNull(list);
            assertEquals("订单1应有2条详情", 2, list.size());
            for (OrderDetails d : list) {
                assertEquals("结果应都属订单1", Integer.valueOf(1), d.getOrderId());
            }

            assertEquals("订单2应有1条", 1, mapper.findDetailsByOrderId(2).size());
            assertEquals("订单3应有2条", 2, mapper.findDetailsByOrderId(3).size());
            assertEquals("订单4应有1条", 1, mapper.findDetailsByOrderId(4).size());
            assertEquals("不存在的订单应返回空集合",
                    0, mapper.findDetailsByOrderId(999999).size());
        } finally {
            session.close();
        }
    }

    /** 修改订单详情（购买数量） */
    @Test
    public void updateOrderDetailTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            OrderDetailMapper mapper = session.getMapper(OrderDetailMapper.class);
            Integer qtyBefore = mapper.findOrderDetailById(1).getQuantity();

            OrderDetails detail = new OrderDetails();
            detail.setId(1);
            detail.setQuantity(10);

            int rows = mapper.updateOrderDetail(detail);
            session.commit();
            assertEquals("应更新1行", 1, rows);
            assertEquals("数量应已更新为10",
                    Integer.valueOf(10), mapper.findOrderDetailById(1).getQuantity());

            // 还原，避免污染种子数据
            OrderDetails restore = new OrderDetails();
            restore.setId(1);
            restore.setQuantity(qtyBefore);
            mapper.updateOrderDetail(restore);
            session.commit();
            assertEquals("应已还原", qtyBefore,
                    mapper.findOrderDetailById(1).getQuantity());
        } finally {
            session.close();
        }
    }

    /** 删除订单详情 */
    @Test
    public void deleteOrderDetailTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            OrderDetailMapper mapper = session.getMapper(OrderDetailMapper.class);

            OrderDetails temp = new OrderDetails();
            temp.setOrderId(4);
            temp.setProductId(1);
            temp.setQuantity(1);
            mapper.addOrderDetail(temp);
            session.commit();
            assertNotNull("删除前应能查到", mapper.findOrderDetailById(temp.getId()));

            int rows = mapper.deleteOrderDetail(temp.getId());
            session.commit();
            assertEquals("应删除1行", 1, rows);
            assertNull("删除后应查不到", mapper.findOrderDetailById(temp.getId()));

            assertEquals("删除不存在的详情应影响0行",
                    0, mapper.deleteOrderDetail(999999));
        } finally {
            session.close();
        }
    }

    /** 按订单ID批量删除详情（供 OrdersService 删除订单时先清理子表） */
    @Test
    public void deleteByOrderIdTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            OrderDetailMapper mapper = session.getMapper(OrderDetailMapper.class);

            // 先删除订单4的详情，再插入几条测试数据，避免影响种子数据的其他订单
            mapper.deleteByOrderId(4);
            session.commit();
            assertEquals("删除后订单4应无详情",
                    0, mapper.findDetailsByOrderId(4).size());

            // 插入3条再批量删除
            for (int i = 1; i <= 3; i++) {
                OrderDetails d = new OrderDetails();
                d.setOrderId(4);
                d.setProductId(i);
                d.setQuantity(i);
                mapper.addOrderDetail(d);
            }
            session.commit();
            assertEquals("应插入3条", 3, mapper.findDetailsByOrderId(4).size());

            int rows = mapper.deleteByOrderId(4);
            session.commit();
            assertEquals("应批量删除3条", 3, rows);
            assertEquals("删除后应为空",
                    0, mapper.findDetailsByOrderId(4).size());

            assertEquals("对无详情的订单应影响0行", 0, mapper.deleteByOrderId(999999));

            // 还原种子数据：订单4 原本有 id=6, product 2, qty 2 一条
            mapper.insertDetailWithId(6, 4, 2, 2);
            session.commit();
            assertEquals("订单4应还原为1条", 1, mapper.findDetailsByOrderId(4).size());
        } finally {
            session.close();
        }
    }

    /** 同时映射订单与商品两个关联对象 */
    @Test
    public void findDetailWithOrderAndProductTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            OrderDetailMapper mapper = session.getMapper(OrderDetailMapper.class);

            List<OrderDetails> list = mapper.findDetailWithOrderAndProduct(1);
            assertNotNull(list);
            assertEquals("订单1应有2条详情", 2, list.size());

            for (OrderDetails d : list) {
                // 两个 association 都应被正确填充
                assertNotNull("orders 关联不应为null", d.getOrders());
                assertNotNull("product 关联不应为null", d.getProduct());

                assertEquals("详情应属订单1", Integer.valueOf(1), d.getOrders().getId());
                assertEquals("订单的userId应与详情一致",
                        Integer.valueOf(1), d.getOrders().getUserId());
                assertNotNull("商品名不应为null", d.getProduct().getName());
                assertNotNull("商品价格不应为null", d.getProduct().getPrice());
            }

            // 订单1 的两条详情应对应商品 1 和 3
            List<Integer> productIds = list.stream()
                    .map(OrderDetails::getProductId)
                    .sorted()
                    .collect(java.util.stream.Collectors.toList());
            assertEquals("应对应商品1和3",
                    java.util.Arrays.asList(1, 3), productIds);
        } finally {
            session.close();
        }
    }
}
