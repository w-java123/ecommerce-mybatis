package com.wangzhoujun17.test;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import com.wangzhoujun17.mapper.OrderDetailMapper;
import com.wangzhoujun17.po.OrderDetails;
import com.wangzhoujun17.utils.MyBatisUtils;

/**
 * 订单详情表 orderdetails（多对多中间表）的完整 CRUD 测试。
 */
public class OrderDetailCrudTest {

    /** 新增订单详情 */
    @Test
    public void addOrderDetailTest() {
        SqlSession session = MyBatisUtils.getSession();
        OrderDetailMapper mapper = session.getMapper(OrderDetailMapper.class);

        OrderDetails detail = new OrderDetails();
        detail.setOrderId(1);
        detail.setProductId(2);
        detail.setQuantity(3);

        int rows = mapper.addOrderDetail(detail);
        session.commit();
        System.out.println("新增订单详情成功，影响行数：" + rows + "，详情ID：" + detail.getId());
        session.close();
    }

    /** 根据ID查询订单详情 */
    @Test
    public void findOrderDetailByIdTest() {
        SqlSession session = MyBatisUtils.getSession();
        OrderDetailMapper mapper = session.getMapper(OrderDetailMapper.class);

        System.out.println("查询订单详情：" + mapper.findOrderDetailById(1));
        session.close();
    }

    /** 查询全部订单详情 */
    @Test
    public void findAllOrderDetailsTest() {
        SqlSession session = MyBatisUtils.getSession();
        OrderDetailMapper mapper = session.getMapper(OrderDetailMapper.class);

        List<OrderDetails> list = mapper.findAllOrderDetails();
        System.out.println("共查询到 " + list.size() + " 条订单详情：");
        for (OrderDetails d : list) {
            System.out.println("  " + d);
        }
        session.close();
    }

    /** 按订单ID查询详情列表 */
    @Test
    public void findDetailsByOrderIdTest() {
        SqlSession session = MyBatisUtils.getSession();
        OrderDetailMapper mapper = session.getMapper(OrderDetailMapper.class);

        List<OrderDetails> list = mapper.findDetailsByOrderId(1);
        System.out.println("订单1共有 " + list.size() + " 条详情：");
        for (OrderDetails d : list) {
            System.out.println("  " + d);
        }
        session.close();
    }

    /** 修改订单详情（购买数量） */
    @Test
    public void updateOrderDetailTest() {
        SqlSession session = MyBatisUtils.getSession();
        OrderDetailMapper mapper = session.getMapper(OrderDetailMapper.class);

        OrderDetails detail = new OrderDetails();
        detail.setId(1);
        detail.setQuantity(10);

        int rows = mapper.updateOrderDetail(detail);
        session.commit();
        System.out.println("修改订单详情成功，影响行数：" + rows);
        System.out.println("修改后：" + mapper.findOrderDetailById(1));
        session.close();
    }

    /** 删除订单详情 */
    @Test
    public void deleteOrderDetailTest() {
        SqlSession session = MyBatisUtils.getSession();
        OrderDetailMapper mapper = session.getMapper(OrderDetailMapper.class);

        OrderDetails temp = new OrderDetails();
        temp.setOrderId(4);
        temp.setProductId(1);
        temp.setQuantity(1);
        mapper.addOrderDetail(temp);
        session.commit();

        int rows = mapper.deleteOrderDetail(temp.getId());
        session.commit();
        System.out.println("删除订单详情成功，影响行数：" + rows);
        session.close();
    }

    /** 同时映射订单与商品两个关联对象 */
    @Test
    public void findDetailWithOrderAndProductTest() {
        SqlSession session = MyBatisUtils.getSession();
        OrderDetailMapper mapper = session.getMapper(OrderDetailMapper.class);

        List<OrderDetails> list = mapper.findDetailWithOrderAndProduct(1);
        System.out.println("订单1的详情（含订单与商品信息）共 " + list.size() + " 条：");
        for (OrderDetails d : list) {
            System.out.println("  详情ID=" + d.getId()
                    + ", 数量=" + d.getQuantity()
                    + ", 商品=" + d.getProduct().getName()
                    + ", 单价=" + d.getProduct().getPrice()
                    + ", 下单用户ID=" + d.getOrders().getUserId());
        }
        session.close();
    }
}
