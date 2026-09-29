package com.wangzhoujun17.po;

import java.io.Serializable;

/**
 * 订单详情实体类，对应数据库表 orderdetails。
 * 该表是 orders 与 products 之间多对多关系的中间表，
 * 额外记录购买数量 quantity。
 */
public class OrderDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 详情ID，主键 */
    private Integer id;
    /** 订单ID，外键 */
    private Integer orderId;
    /** 商品ID，外键 */
    private Integer productId;
    /** 购买数量 */
    private Integer quantity;

    /** 关联的订单对象 */
    private Orders orders;
    /** 关联的商品对象 */
    private Products product;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Orders getOrders() {
        return orders;
    }

    public void setOrders(Orders orders) {
        this.orders = orders;
    }

    public Products getProduct() {
        return product;
    }

    public void setProduct(Products product) {
        this.product = product;
    }

    @Override
    public String toString() {
        return "OrderDetails [id=" + id + ", orderId=" + orderId
                + ", productId=" + productId + ", quantity=" + quantity
                + ", product=" + product + "]";
    }
}
