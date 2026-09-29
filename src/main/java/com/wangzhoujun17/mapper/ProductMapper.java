package com.wangzhoujun17.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.wangzhoujun17.po.Products;

/**
 * 商品 DAO 接口。
 * 增删改查基本操作全部采用注解方式实现。
 */
public interface ProductMapper {

    /** 新增商品 */
    @Insert("insert into products(name, price, stock) "
            + "values(#{name}, #{price}, #{stock})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int addProduct(Products product);

    /** 根据ID删除商品 */
    @Delete("delete from products where id = #{id}")
    int deleteProduct(Integer id);

    /** 根据ID查询商品 */
    @Select("select * from products where id = #{id}")
    Products findProductById(Integer id);

    /** 查询全部商品 */
    @Select("select * from products order by id")
    List<Products> findAllProducts();

    /** 修改商品（含库存） */
    @Update("update products set name = #{name}, price = #{price}, "
            + "stock = #{stock} where id = #{id}")
    int updateProduct(Products product);

    /** 按商品名称模糊查询（注解方式，演示 @Select + 字符串拼接） */
    @Select("select * from products where name like concat('%', #{name}, '%')")
    List<Products> findProductByName(String name);

    /** 按商品名称模糊查询（映射文件方式，SQL 写在 ProductMapper.xml 中） */
    List<Products> searchProductByName(String name);

    /** 扣减库存：仅当库存充足时才扣减，返回受影响行数用于判断业务是否成功 */
    @Update("update products set stock = stock - #{quantity} "
            + "where id = #{id} and stock >= #{quantity}")
    int reduceStock(@org.apache.ibatis.annotations.Param("id") Integer id,
                    @org.apache.ibatis.annotations.Param("quantity") Integer quantity);
}
