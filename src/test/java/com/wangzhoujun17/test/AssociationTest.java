package com.wangzhoujun17.test;

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
 */
public class AssociationTest {

    /** 一对多：查询用户及其所有订单 */
    @Test
    public void findUserWithOrdersTest() {
        SqlSession session = MyBatisUtils.getSession();
        UserMapper mapper = session.getMapper(UserMapper.class);

        Users user = mapper.findUserWithOrders(1);
        System.out.println("===== 一对多：用户及其订单 =====");
        System.out.println(user);
        System.out.println("该用户订单数量：" + user.getOrderList().size());
        for (Orders o : user.getOrderList()) {
            System.out.println("  订单ID=" + o.getId() + ", 日期=" + o.getOrderDate());
        }
        session.close();
    }

    /** 多对一：查询订单及其所属用户 */
    @Test
    public void findOrdersWithUserTest() {
        SqlSession session = MyBatisUtils.getSession();
        OrdersMapper mapper = session.getMapper(OrdersMapper.class);

        Orders orders = mapper.findOrdersWithUser(1);
        System.out.println("===== 多对一：订单及其所属用户 =====");
        System.out.println("订单ID=" + orders.getId()
                + ", 日期=" + orders.getOrderDate()
                + ", 所属用户=" + orders.getUser().getUsername()
                + "(" + orders.getUser().getEmail() + ")");
        session.close();
    }

    /** 多对多（方式一：嵌套查询，会发送多条 SQL） */
    @Test
    public void findOrdersWithProductTest() {
        SqlSession session = MyBatisUtils.getSession();
        OrdersMapper mapper = session.getMapper(OrdersMapper.class);

        Orders orders = mapper.findOrdersWithProduct(1);
        System.out.println("===== 多对多（嵌套查询）：订单及其商品 =====");
        System.out.println("订单ID=" + orders.getId() + ", 日期=" + orders.getOrderDate());
        for (Products p : orders.getProductList()) {
            System.out.println("  商品：" + p);
        }
        session.close();
    }

    /** 多对多（方式二：嵌套结果，只发送一条 SQL） */
    @Test
    public void findOrdersWithProductByJoinTest() {
        SqlSession session = MyBatisUtils.getSession();
        OrdersMapper mapper = session.getMapper(OrdersMapper.class);

        Orders orders = mapper.findOrdersWithProductByJoin(3);
        System.out.println("===== 多对多（嵌套结果）：订单及其商品 =====");
        System.out.println("订单ID=" + orders.getId() + ", 日期=" + orders.getOrderDate());
        for (Products p : orders.getProductList()) {
            System.out.println("  商品：" + p);
        }
        session.close();
    }
}
