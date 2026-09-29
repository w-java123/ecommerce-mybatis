package com.wangzhoujun17.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.wangzhoujun17.po.Products;

/**
 * 商品 DAO 接口。
 * <p>
 * 本接口同时演示两种 MyBatis 开发方式：
 * <ul>
 *   <li><b>注解方式</b>：addProduct / deleteProduct / findProductById /
 *       findAllProducts / updateProduct / findProductByName / reduceStock</li>
 *   <li><b>映射文件方式</b>：insertProduct / deleteProductById /
 *       selectProductById / selectAllProducts / updateProductSelective /
 *       searchProduct（SQL 写在 ProductMapper.xml 中）</li>
 * </ul>
 * 两套方法功能等价，便于对比两种写法的差异。
 */
public interface ProductMapper {

    // ==================== 方式一：注解 ====================

    /** 新增商品（注解方式） */
    @Insert("insert into products(name, price, stock) "
            + "values(#{name}, #{price}, #{stock})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int addProduct(Products product);

    /** 根据ID删除商品（注解方式） */
    @Delete("delete from products where id = #{id}")
    int deleteProduct(Integer id);

    /** 根据ID查询商品（注解方式） */
    @Select("select * from products where id = #{id}")
    Products findProductById(Integer id);

    /** 查询全部商品（注解方式） */
    @Select("select * from products order by id")
    List<Products> findAllProducts();

    /** 修改商品（注解方式，更新全部字段） */
    @Update("update products set name = #{name}, price = #{price}, "
            + "stock = #{stock} where id = #{id}")
    int updateProduct(Products product);

    /** 按商品名称模糊查询（注解方式） */
    @Select("select * from products where name like concat('%', #{name}, '%')")
    List<Products> findProductByName(String name);

    /** 扣减库存：仅当库存充足时才扣减，返回受影响行数用于判断业务是否成功 */
    @Update("update products set stock = stock - #{quantity} "
            + "where id = #{id} and stock >= #{quantity}")
    int reduceStock(@Param("id") Integer id,
                    @Param("quantity") Integer quantity);

    // ==================== 方式二：映射文件 ProductMapper.xml ====================

    /** 新增商品（映射文件方式，主键回填） */
    int insertProduct(Products product);

    /** 根据ID删除商品（映射文件方式） */
    int deleteProductById(Integer id);

    /** 根据ID查询商品（映射文件方式，使用 resultMap 映射） */
    Products selectProductById(Integer id);

    /** 查询全部商品（映射文件方式，使用 resultMap 映射） */
    List<Products> selectAllProducts();

    /**
     * 修改商品（映射文件方式，动态 SQL 只更新非空字段）。
     * 调用方须至少传入一个非空字段，否则生成的 SQL 非法。
     */
    int updateProductSelective(Products product);

    /**
     * 组合条件查询（映射文件方式，动态 SQL）。
     * @param params 支持的键：name（模糊匹配）、minStock（库存下限）
     */
    List<Products> searchProduct(Map<String, Object> params);

    /** 按商品名称模糊查询（映射文件方式，兼容原有测试） */
    List<Products> searchProductByName(String name);
}
