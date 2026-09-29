package com.wangzhoujun17.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import com.wangzhoujun17.mapper.OrdersMapper;
import com.wangzhoujun17.po.Orders;
import com.wangzhoujun17.utils.MyBatisUtils;

/**
 * 订单表 orders 的完整 CRUD 测试。
 * 测试数据来自 db.sql 种子数据，演示前请先执行 reset-db.bat。
 */
public class OrdersCrudTest {

    /** 新增订单 */
    @Test
    public void addOrdersTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            OrdersMapper mapper = session.getMapper(OrdersMapper.class);

            Orders orders = new Orders();
            orders.setUserId(2);
            orders.setOrderDate(new Date());

            int rows = mapper.addOrders(orders);
            session.commit();
            assertEquals("应插入1行", 1, rows);
            assertNotNull("主键应被回填", orders.getId());

            Orders loaded = mapper.findOrdersById(orders.getId());
            assertNotNull("插入后应能查到", loaded);
            assertEquals("所属用户应为2", Integer.valueOf(2), loaded.getUserId());
            assertNotNull("下单日期不应为null", loaded.getOrderDate());

            // 清理
            mapper.deleteOrders(orders.getId());
            session.commit();
        } finally {
            session.close();
        }
    }

    /** 根据ID查询订单 */
    @Test
    public void findOrdersByIdTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            OrdersMapper mapper = session.getMapper(OrdersMapper.class);

            Orders o = mapper.findOrdersById(1);
            assertNotNull("订单1应存在", o);
            assertEquals("订单ID应为1", Integer.valueOf(1), o.getId());
            assertEquals("订单应归属用户1", Integer.valueOf(1), o.getUserId());
            assertNotNull("下单日期不应为null", o.getOrderDate());

            assertNull("不存在的订单应返回null", mapper.findOrdersById(999999));
        } finally {
            session.close();
        }
    }

    /** 查询全部订单 */
    @Test
    public void findAllOrdersTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            OrdersMapper mapper = session.getMapper(OrdersMapper.class);

            List<Orders> list = mapper.findAllOrders();
            assertNotNull(list);
            assertEquals("种子数据应有4条订单", 4, list.size());
            for (int i = 0; i < list.size(); i++) {
                assertEquals("第" + (i + 1) + "条订单ID应为" + (i + 1),
                        Integer.valueOf(i + 1), list.get(i).getId());
                assertNotNull("下单日期不应为null", list.get(i).getOrderDate());
            }
        } finally {
            session.close();
        }
    }

    /** 按用户ID查询该用户的订单 */
    @Test
    public void findOrdersByUserIdTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            OrdersMapper mapper = session.getMapper(OrdersMapper.class);

            // 种子数据：订单1、2 属于用户1
            List<Orders> list = mapper.findOrdersByUserId(1);
            assertNotNull(list);
            assertEquals("用户1应有2条订单", 2, list.size());
            for (Orders o : list) {
                assertEquals("结果应都归属用户1", Integer.valueOf(1), o.getUserId());
            }
            // 用户2 有1条，用户3 有1条
            assertEquals("用户2应有1条订单", 1, mapper.findOrdersByUserId(2).size());
            assertEquals("用户3应有1条订单", 1, mapper.findOrdersByUserId(3).size());
            assertEquals("不存在的用户应返回空集合",
                    0, mapper.findOrdersByUserId(999999).size());
        } finally {
            session.close();
        }
    }

    /** 修改订单（动态 SQL：只更新非空字段） */
    @Test
    public void updateOrdersTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            OrdersMapper mapper = session.getMapper(OrdersMapper.class);
            Integer userIdBefore = mapper.findOrdersById(1).getUserId();

            Orders orders = new Orders();
            orders.setId(1);
            orders.setUserId(3);   // 只修改所属用户，orderDate 不传
            int rows = mapper.updateOrders(orders);
            session.commit();
            assertEquals("应更新1行", 1, rows);

            Orders after = mapper.findOrdersById(1);
            assertEquals("所属用户应已更新为3", Integer.valueOf(3), after.getUserId());
            assertNotNull("未传的 orderDate 应保持原值", after.getOrderDate());

            // 还原，避免污染种子数据
            Orders restore = new Orders();
            restore.setId(1);
            restore.setUserId(userIdBefore);
            mapper.updateOrders(restore);
            session.commit();
            assertEquals("应已还原", userIdBefore,
                    mapper.findOrdersById(1).getUserId());
        } finally {
            session.close();
        }
    }

    /** 按日期区间动态查询 */
    @Test
    public void findOrdersByDateRangeTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            OrdersMapper mapper = session.getMapper(OrdersMapper.class);

            // 种子数据日期：1=09-20, 2=09-22, 3=09-25, 4=09-28
            Date begin = java.sql.Date.valueOf("2026-09-21");
            Date end   = java.sql.Date.valueOf("2026-09-26");
            List<Orders> list = mapper.findOrdersByDateRange(begin, end);
            assertNotNull(list);
            assertEquals("09-21至09-26应有2条", 2, list.size());

            // 只传起始日期
            List<Orders> fromOnly = mapper.findOrdersByDateRange(begin, null);
            assertEquals("09-21起应有3条", 3, fromOnly.size());

            // 两个都不传 -> 全部
            assertEquals("无条件应返回全部4条",
                    4, mapper.findOrdersByDateRange(null, null).size());
        } finally {
            session.close();
        }
    }

    /**
     * 日期区间边界应【两端都包含】。
     * <p>
     * 种子数据里没有正好落在区间端点的订单，若只测上面的用例，
     * 把 SQL 从 {@code <=} 误写成 {@code <} 也不会被发现。
     * 本用例专门插入一条正好在端点上的订单来锁住这个行为。
     */
    @Test
    public void findOrdersByDateRangeIsInclusiveTest() {
        SqlSession session = MyBatisUtils.getSession();
        Integer onBeginId = null;
        Integer onEndId = null;
        try {
            OrdersMapper mapper = session.getMapper(OrdersMapper.class);

            // 插入一条日期正好等于端点 09-26 的订单
            Orders onEnd = new Orders();
            onEnd.setUserId(1);
            onEnd.setOrderDate(java.sql.Date.valueOf("2026-09-26"));
            mapper.addOrders(onEnd);
            onEndId = onEnd.getId();
            // 再插入一条正好等于起点 09-21 的订单
            Orders onBegin = new Orders();
            onBegin.setUserId(1);
            onBegin.setOrderDate(java.sql.Date.valueOf("2026-09-21"));
            mapper.addOrders(onBegin);
            onBeginId = onBegin.getId();
            session.commit();

            Date begin = java.sql.Date.valueOf("2026-09-21");
            Date end   = java.sql.Date.valueOf("2026-09-26");

            // 单日区间：起止相同，应恰好命中端点那天
            List<Orders> single = mapper.findOrdersByDateRange(begin, begin);
            assertNotNull(single);
            assertEquals("起点当日应被包含", 1, single.size());
            assertEquals("命中的应是09-21那条",
                    onBeginId, single.get(0).getId());

            // 完整区间：端点两条都应包含。种子2条 + 新增2条 = 4条
            List<Orders> full = mapper.findOrdersByDateRange(begin, end);
            assertEquals("区间两端都应被包含（种子2条+端点2条）", 4, full.size());

            final Integer bid = onBeginId;
            final Integer eid = onEndId;
            assertTrue("应含起点当日订单",
                    full.stream().anyMatch(o -> o.getId().equals(bid)));
            assertTrue("应含终点当日订单",
                    full.stream().anyMatch(o -> o.getId().equals(eid)));
        } finally {
            // 无论如何都要清理：断言失败时也要删掉本用例插入的数据，
            // 否则残留会破坏 findAllOrdersTest 等依赖固定条数的测试。
            try {
                OrdersMapper mapper = session.getMapper(OrdersMapper.class);
                if (onBeginId != null) {
                    mapper.deleteOrders(onBeginId);
                }
                if (onEndId != null) {
                    mapper.deleteOrders(onEndId);
                }
                session.commit();
            } catch (RuntimeException ignored) {
                session.rollback();
            } finally {
                session.close();
            }
        }
    }

    /** 删除订单（仅删除没有详情的订单） */
    @Test
    public void deleteOrdersTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            OrdersMapper mapper = session.getMapper(OrdersMapper.class);

            // 先新增一条临时订单再删除，避免破坏既有测试数据
            Orders temp = new Orders();
            temp.setUserId(1);
            temp.setOrderDate(new Date());
            mapper.addOrders(temp);
            session.commit();
            assertNotNull("删除前应能查到", mapper.findOrdersById(temp.getId()));

            int rows = mapper.deleteOrders(temp.getId());
            session.commit();
            assertEquals("应删除1行", 1, rows);
            assertNull("删除后应查不到", mapper.findOrdersById(temp.getId()));

            assertEquals("删除不存在的订单应影响0行",
                    0, mapper.deleteOrders(999999));
        } finally {
            session.close();
        }
    }

    /**
     * 删除【带详情】的订单：直接删会违反外键约束，
     * 必须走 OrdersService.deleteOrdersWithDetails（见 OrdersServiceTest）。
     */
    @Test
    public void deleteOrdersWithDetailsFailsByForeignKeyTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            OrdersMapper mapper = session.getMapper(OrdersMapper.class);

            // 订单1 在 orderdetails 中有2条详情，直接删除应抛异常
            try {
                mapper.deleteOrders(1);
                session.commit();
                fail("删除带详情的订单应因外键约束而失败");
            } catch (RuntimeException e) {
                session.rollback();
                // 断言这是外键约束错误，而不是别的异常
                assertTrue("异常应说明外键约束失败",
                        e.getMessage() != null
                                && e.getMessage().contains("foreign key constraint"));
            }

            // 回滚后订单1 应仍然存在
            assertNotNull("回滚后订单1应仍然存在", mapper.findOrdersById(1));
        } finally {
            session.close();
        }
    }
}
