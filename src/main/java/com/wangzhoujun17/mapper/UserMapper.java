package com.wangzhoujun17.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.wangzhoujun17.po.Users;

/**
 * 用户 DAO 接口。
 * 简单查询使用注解方式实现，复杂动态 SQL 与关联查询在 UserMapper.xml 中实现。
 */
public interface UserMapper {

    /** 新增用户（注解方式） */
    @Insert("insert into users(username, password, email) "
            + "values(#{username}, #{password}, #{email})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int addUser(Users user);

    /** 根据ID删除用户（注解方式） */
    @Delete("delete from users where id = #{id}")
    int deleteUser(Integer id);

    /** 根据ID查询用户（注解方式） */
    @Select("select * from users where id = #{id}")
    Users findUserById(Integer id);

    /** 查询全部用户（注解方式） */
    @Select("select * from users order by id")
    List<Users> findAllUsers();

    /** 修改用户信息 -> UserMapper.xml 动态 SQL */
    int updateUser(Users user);

    /** 按用户名模糊查询 + 邮箱精确查询 -> UserMapper.xml 动态 SQL */
    List<Users> findUserByCondition(Users user);

    /**
     * 关联查询：查询用户及其所有订单（一对多）
     * @param id 用户ID
     * @return 携带 orderList 的 Users 对象
     */
    Users findUserWithOrders(@Param("id") Integer id);
}
