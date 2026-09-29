package com.wangzhoujun17.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import com.wangzhoujun17.mapper.OrdersMapper;
import com.wangzhoujun17.mapper.UserMapper;
import com.wangzhoujun17.po.Orders;
import com.wangzhoujun17.po.Products;
import com.wangzhoujun17.po.Users;
import com.wangzhoujun17.utils.MyBatisUtils;

/**
 * 多表关联查询测试。
 *
 * 一对一 / 多对一：orders -> users（association）
 * 一对多：users -> orders（collection）
 * 多对多：orders <-> products，通过 orderdetails 中间表（collection，两种实现方式）
 *
 * 测试数据来自 db.sql 初始种子数据（演示前请先执行 reset-db.bat）：
 *   users：1=joy, 2=jack, 3=tom
 *   products：1=Java基础入门, 2=JavaWeb程序设计, 3=SSM框架整合实战, 4=MySQL数据库原理
 *   orders：1(用户1), 2(用户1), 3(用户2), 4(用户3)
 *   orderdetails：(1,1,2) (1,3,1) (2,2,5) (3,1,1) (3,4,3) (4,2,2)
 */
public class AssociationTest {

    /** 一对多：查询用户及其所有订单 */
    @Test
    public void findUserWithOrdersTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            UserMapper mapper = session.getMapper(UserMapper.class);
            Users user = mapper.findUserWithOrders(1);

            assertNotNull("用户1应查询到", user);
            assertEquals("用户ID应为1", Integer.valueOf(1), user.getId());
            assertEquals("用户名应为joy", "joy", user.getUsername());

            List<Orders> orderList = user.getOrderList();
            assertNotNull("orderList 不应为 null", orderList);
            assertEquals("用户1应有2条订单", 2, orderList.size());
            // 订单按 o.id 排序，应为 1 和 2
            assertEquals("第1条订单ID应为1", Integer.valueOf(1), orderList.get(0).getId());
            assertEquals("第2条订单ID应为2", Integer.valueOf(2), orderList.get(1).getId());
            // 关联映射正确性：订单的 userId 应指回用户1
            assertEquals("订单应归属用户1", Integer.valueOf(1), orderList.get(0).getUserId());
        } finally {
            session.close();
        }
    }

    /** 一对多：无订单的用户应返回空集合而非 null */
    @Test
    public void findUserWithOrdersEmptyTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            UserMapper mapper = session.getMapper(UserMapper.class);

            // 先造一个没有任何订单的用户（用户名加时间戳避免唯一约束冲突）
            Users u = new Users();
            u.setUsername("noorder_" + System.currentTimeMillis());
            u.setPassword("pwd");
            u.setEmail("noorder@example.com");
            mapper.addUser(u);
            session.commit();

            Users found = mapper.findUserWithOrders(u.getId());
            assertNotNull("应查询到该用户", found);
            // LEFT JOIN 无匹配行时，collection 应为【空集合】而非 null。
            // 这里必须断言非 null —— 若只写 isEmpty() 前加 null 判断，
            // 映射退化成返回 null 时测试仍会通过，失去防护意义。
            assertNotNull("无订单用户的 orderList 应为空集合而非 null",
                    found.getOrderList());
            assertTrue("无订单用户的 orderList 应为空",
                    found.getOrderList().isEmpty());

            mapper.deleteUser(u.getId());
            session.commit();
        } finally {
            session.close();
        }
    }

    /** 多对一：查询订单及其所属用户 */
    @Test
    public void findOrdersWithUserTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            OrdersMapper mapper = session.getMapper(OrdersMapper.class);
            Orders orders = mapper.findOrdersWithUser(1);

            assertNotNull("订单1应查询到", orders);
            assertEquals("订单ID应为1", Integer.valueOf(1), orders.getId());
            assertEquals("订单应归属用户1", Integer.valueOf(1), orders.getUserId());

            // 关键断言：association 映射正确
            Users user = orders.getUser();
            assertNotNull("orders.user 不应为 null", user);
            assertEquals("所属用户ID应为1", Integer.valueOf(1), user.getId());
            assertEquals("所属用户名应为joy", "joy", user.getUsername());
            assertEquals("所属用户邮箱应为joy@example.com", "joy@example.com", user.getEmail());
        } finally {
            session.close();
        }
    }

    /** 多对一：换个订单验证映射没有把 id 串到用户上 */
    @Test
    public void findOrdersWithUserDifferentRowTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            OrdersMapper mapper = session.getMapper(OrdersMapper.class);
            Orders orders = mapper.findOrdersWithUser(3);

            assertNotNull(orders);
            // 订单3 的 id 是 3，所属用户是 2(jack)。
            // 若 association 的 column 映射错乱，这里会暴露出来。
            assertEquals("订单ID应为3", Integer.valueOf(3), orders.getId());
            assertEquals("订单3应归属用户2", Integer.valueOf(2), orders.getUserId());
            assertEquals("所属用户名应为jack", "jack", orders.getUser().getUsername());
        } finally {
            session.close();
        }
    }

    /** 多对多（方式一：嵌套查询，会发送多条 SQL） */
    @Test
    public void findOrdersWithProductTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            OrdersMapper mapper = session.getMapper(OrdersMapper.class);
            Orders orders = mapper.findOrdersWithProduct(1);

            assertNotNull("订单1应查询到", orders);
            assertEquals(Integer.valueOf(1), orders.getId());

            List<Products> productList = orders.getProductList();
            assertNotNull("productList 不应为 null", productList);
            // orderdetails 中订单1 有 2 条：(1,1) 和 (1,3)
            assertEquals("订单1应含2个商品", 2, productList.size());

            // 校验商品内容，而非仅数量
            List<Integer> ids = productList.stream()
                    .map(Products::getId).sorted().collect(java.util.stream.Collectors.toList());
            assertEquals("商品ID应为 1 和 3",
                    java.util.Arrays.asList(1, 3), ids);
            assertEquals("商品名应为Java基础入门",
                    "Java基础入门", productList.get(0).getName());
        } finally {
            session.close();
        }
    }

    /** 多对多（方式二：嵌套结果，只发送一条 SQL）——结果应与方式一一致 */
    @Test
    public void findOrdersWithProductByJoinTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            OrdersMapper mapper = session.getMapper(OrdersMapper.class);
            Orders orders = mapper.findOrdersWithProductByJoin(3);

            assertNotNull(orders);
            assertEquals(Integer.valueOf(3), orders.getId());

            List<Products> productList = orders.getProductList();
            assertNotNull(productList);
            // orderdetails 中订单3 有 2 条：(3,1) 和 (3,4)
            assertEquals("订单3应含2个商品", 2, productList.size());

            List<Integer> ids = productList.stream()
                    .map(Products::getId).sorted().collect(java.util.stream.Collectors.toList());
            assertEquals("商品ID应为 1 和 4",
                    java.util.Arrays.asList(1, 4), ids);
        } finally {
            session.close();
        }
    }

    /** 两种多对多实现应返回完全相同的结果（防止两种写法行为不一致） */
    @Test
    public void nestedQueryAndJoinAgreeTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            OrdersMapper mapper = session.getMapper(OrdersMapper.class);

            for (int orderId : new int[]{1, 2, 3, 4}) {
                Orders bySelect = mapper.findOrdersWithProduct(orderId);
                Orders byJoin = mapper.findOrdersWithProductByJoin(orderId);

                List<Integer> a = bySelect.getProductList().stream()
                        .map(Products::getId).sorted()
                        .collect(java.util.stream.Collectors.toList());
                List<Integer> b = byJoin.getProductList().stream()
                        .map(Products::getId).sorted()
                        .collect(java.util.stream.Collectors.toList());

                assertEquals("订单" + orderId + " 两种多对多实现结果应一致", a, b);
            }
        } finally {
            session.close();
        }
    }
}
