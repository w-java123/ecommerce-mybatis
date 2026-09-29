package com.wangzhoujun17.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.wangzhoujun17.po.Orders;

/**
 * 订单 DAO 接口。
 * 基本 CRUD 使用注解方式；关联查询（多对一、多对多）在 OrdersMapper.xml 中实现。
 */
public interface OrdersMapper {

    /** 新增订单（注解方式） */
    @Insert("insert into orders(user_id, order_date) "
            + "values(#{userId}, #{orderDate})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int addOrders(Orders orders);

    /** 根据ID删除订单（注解方式，仅删除订单本身） */
    @Delete("delete from orders where id = #{id}")
    int deleteOrders(Integer id);

    /** 根据ID查询订单（注解方式，仅查询订单本身字段） */
    @Select("select * from orders where id = #{id}")
    Orders findOrdersById(Integer id);

    /**
     * 显式指定主键插入订单（映射文件方式）。
     * 供测试还原被删除的种子数据使用，业务代码请用 addOrders。
     */
    int insertWithId(Orders orders);

    /** 查询全部订单（注解方式） */
    @Select("select * from orders order by id")
    List<Orders> findAllOrders();

    /** 按用户ID查询订单列表（注解方式） */
    @Select("select * from orders where user_id = #{userId} order by id")
    List<Orders> findOrdersByUserId(Integer userId);

    /** 按日期区间查询订单 -> OrdersMapper.xml 动态 SQL */
    List<Orders> findOrdersByDateRange(@Param("begin") Date begin,
                                       @Param("end") Date end);

    /** 修改订单 -> OrdersMapper.xml 动态 SQL */
    int updateOrders(Orders orders);

    /**
     * 关联查询（多对一）：查询订单及其所属用户
     * @param id 订单ID
     */
    Orders findOrdersWithUser(@Param("id") Integer id);

    /**
     * 关联查询（多对多）：查询订单及其包含的全部商品
     * 方式一：嵌套查询（collection + select），会发送多条 SQL
     */
    Orders findOrdersWithProduct(@Param("id") Integer id);

    /**
     * 关联查询（多对多）：查询订单及其包含的全部商品
     * 方式二：嵌套结果（collection + 多表连接），只发送一条 SQL
     */
    Orders findOrdersWithProductByJoin(@Param("id") Integer id);
}
