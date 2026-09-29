package com.wangzhoujun17.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.wangzhoujun17.po.OrderDetails;

/**
 * 订单详情 DAO 接口，对应中间表 orderdetails。
 * 该表存储订单与商品的多对多关系及购买数量。
 */
public interface OrderDetailMapper {

    /** 新增订单详情 */
    @Insert("insert into orderdetails(order_id, product_id, quantity) "
            + "values(#{orderId}, #{productId}, #{quantity})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int addOrderDetail(OrderDetails detail);

    /**
     * 显式指定主键插入订单详情（映射文件方式）。
     * 供测试还原被删除的种子数据使用，业务代码请用 addOrderDetail。
     */
    int insertDetailWithId(@Param("id") Integer id,
                           @Param("orderId") Integer orderId,
                           @Param("productId") Integer productId,
                           @Param("quantity") Integer quantity);

    /** 根据ID删除订单详情 */
    @Delete("delete from orderdetails where id = #{id}")
    int deleteOrderDetail(Integer id);

    /** 根据ID查询订单详情 */
    @Select("select * from orderdetails where id = #{id}")
    OrderDetails findOrderDetailById(Integer id);

    /** 查询全部订单详情 */
    @Select("select * from orderdetails order by id")
    List<OrderDetails> findAllOrderDetails();

    /** 修改订单详情（数量） */
    @Update("update orderdetails set quantity = #{quantity} where id = #{id}")
    int updateOrderDetail(OrderDetails detail);

    /** 根据订单ID删除该订单的全部详情（下单/退单时使用） */
    @Delete("delete from orderdetails where order_id = #{orderId}")
    int deleteByOrderId(Integer orderId);

    /** 根据订单ID查询详情列表 */
    @Select("select * from orderdetails where order_id = #{orderId} order by id")
    List<OrderDetails> findDetailsByOrderId(Integer orderId);

    /**
     * 两个关联（多对一 + 多对一）同时映射：
     * 查询订单详情，并同时携带商品信息与订单信息。
     */
    List<OrderDetails> findDetailWithOrderAndProduct(Integer orderId);
}
