package com.wangzhoujun17.test;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import com.wangzhoujun17.mapper.OrdersMapper;
import com.wangzhoujun17.po.Orders;
import com.wangzhoujun17.utils.MyBatisUtils;

/**
 * 订单表 orders 的完整 CRUD 测试。
 */
public class OrdersCrudTest {

    /** 新增订单 */
    @Test
    public void addOrdersTest() {
        SqlSession session = MyBatisUtils.getSession();
        OrdersMapper mapper = session.getMapper(OrdersMapper.class);

        Orders orders = new Orders();
        orders.setUserId(2);
        orders.setOrderDate(new Date());

        int rows = mapper.addOrders(orders);
        session.commit();
        System.out.println("新增订单成功，影响行数：" + rows + "，订单ID：" + orders.getId());
        session.close();
    }

    /** 根据ID查询订单 */
    @Test
    public void findOrdersByIdTest() {
        SqlSession session = MyBatisUtils.getSession();
        OrdersMapper mapper = session.getMapper(OrdersMapper.class);

        System.out.println("查询订单：" + mapper.findOrdersById(1));
        session.close();
    }

    /** 查询全部订单 */
    @Test
    public void findAllOrdersTest() {
        SqlSession session = MyBatisUtils.getSession();
        OrdersMapper mapper = session.getMapper(OrdersMapper.class);

        List<Orders> list = mapper.findAllOrders();
        System.out.println("共查询到 " + list.size() + " 条订单：");
        for (Orders o : list) {
            System.out.println("  " + o);
        }
        session.close();
    }

    /** 按用户ID查询该用户的订单 */
    @Test
    public void findOrdersByUserIdTest() {
        SqlSession session = MyBatisUtils.getSession();
        OrdersMapper mapper = session.getMapper(OrdersMapper.class);

        List<Orders> list = mapper.findOrdersByUserId(1);
        System.out.println("用户1共有 " + list.size() + " 条订单：");
        for (Orders o : list) {
            System.out.println("  " + o);
        }
        session.close();
    }

    /** 修改订单（动态 SQL） */
    @Test
    public void updateOrdersTest() {
        SqlSession session = MyBatisUtils.getSession();
        OrdersMapper mapper = session.getMapper(OrdersMapper.class);

        Orders orders = new Orders();
        orders.setId(1);
        orders.setUserId(3);   // 只修改所属用户
        int rows = mapper.updateOrders(orders);
        session.commit();
        System.out.println("修改订单成功，影响行数：" + rows);
        System.out.println("修改后：" + mapper.findOrdersById(1));
        session.close();
    }

    /** 按日期区间动态查询 */
    @Test
    public void findOrdersByDateRangeTest() {
        SqlSession session = MyBatisUtils.getSession();
        OrdersMapper mapper = session.getMapper(OrdersMapper.class);

        Date begin = java.sql.Date.valueOf("2026-09-21");
        Date end   = java.sql.Date.valueOf("2026-09-26");
        List<Orders> list = mapper.findOrdersByDateRange(begin, end);
        System.out.println("2026-09-21 至 2026-09-26 的订单共 " + list.size() + " 条：");
        for (Orders o : list) {
            System.out.println("  " + o);
        }
        session.close();
    }

    /** 删除订单 */
    @Test
    public void deleteOrdersTest() {
        SqlSession session = MyBatisUtils.getSession();
        OrdersMapper mapper = session.getMapper(OrdersMapper.class);

        // 先新增一条临时订单再删除，避免破坏既有测试数据
        Orders temp = new Orders();
        temp.setUserId(1);
        temp.setOrderDate(new Date());
        mapper.addOrders(temp);
        session.commit();

        int rows = mapper.deleteOrders(temp.getId());
        session.commit();
        System.out.println("删除订单成功，影响行数：" + rows);
        System.out.println("删除后查询：" + mapper.findOrdersById(temp.getId()));
        session.close();
    }
}
