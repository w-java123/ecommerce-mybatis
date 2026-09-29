package com.wangzhoujun17.po;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 订单实体类，对应数据库表 orders。
 * 与 users 表为多对一关系（一个订单属于一个用户）；
 * 与 products 表通过 orderdetails 中间表构成多对多关系。
 */
public class Orders implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单ID，主键 */
    private Integer id;
    /** 下单用户ID，外键 */
    private Integer userId;
    /** 下单日期 */
    private Date orderDate;

    /** 所属用户（多对一关联属性） */
    private Users user;
    /** 订单包含的商品列表（多对多关联属性） */
    private List<Products> productList;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public List<Products> getProductList() {
        return productList;
    }

    public void setProductList(List<Products> productList) {
        this.productList = productList;
    }

    @Override
    public String toString() {
        return "Orders [id=" + id + ", userId=" + userId
                + ", orderDate=" + orderDate + ", productList=" + productList + "]";
    }
}
