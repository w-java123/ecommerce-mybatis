package com.wangzhoujun17.po;

import java.io.Serializable;
import java.util.List;

/**
 * 用户实体类，对应数据库表 users。
 * 与 orders 表为一对多关系（一个用户可有多个订单）。
 */
public class Users implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID，主键 */
    private Integer id;
    /** 用户名 */
    private String username;
    /** 密码 */
    private String password;
    /** 邮箱 */
    private String email;
    /** 该用户的所有订单（一对多关联属性） */
    private List<Orders> orderList;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Orders> getOrderList() {
        return orderList;
    }

    public void setOrderList(List<Orders> orderList) {
        this.orderList = orderList;
    }

    @Override
    public String toString() {
        return "Users [id=" + id + ", username=" + username
                + ", password=" + password + ", email=" + email + "]";
    }
}
