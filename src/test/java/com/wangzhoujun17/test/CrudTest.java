package com.wangzhoujun17.test;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import com.wangzhoujun17.mapper.ProductMapper;
import com.wangzhoujun17.mapper.UserMapper;
import com.wangzhoujun17.po.Products;
import com.wangzhoujun17.po.Users;
import com.wangzhoujun17.utils.MyBatisUtils;

/**
 * 用户表 users 与商品表 products 的完整 CRUD 测试。
 */
public class CrudTest {

    // ======================== users 表 CRUD ========================

    /** 新增用户 */
    @Test
    public void addUserTest() {
        SqlSession session = MyBatisUtils.getSession();
        UserMapper mapper = session.getMapper(UserMapper.class);

        Users user = new Users();
        // username 有唯一约束，追加时间戳保证测试可重复执行
        user.setUsername("alice_" + System.currentTimeMillis());
        user.setPassword("alice123");
        user.setEmail("alice@example.com");

        int rows = mapper.addUser(user);
        session.commit();
        System.out.println("新增用户成功，影响行数：" + rows + "，自动回填的主键ID：" + user.getId());

        // 清理本次新增的数据，保证可重复执行
        mapper.deleteUser(user.getId());
        session.commit();
        session.close();
    }

    /** 根据ID查询用户 */
    @Test
    public void findUserByIdTest() {
        SqlSession session = MyBatisUtils.getSession();
        UserMapper mapper = session.getMapper(UserMapper.class);

        Users user = mapper.findUserById(1);
        System.out.println("查询用户：" + user);
        session.close();
    }

    /** 查询全部用户 */
    @Test
    public void findAllUsersTest() {
        SqlSession session = MyBatisUtils.getSession();
        UserMapper mapper = session.getMapper(UserMapper.class);

        List<Users> users = mapper.findAllUsers();
        System.out.println("共查询到 " + users.size() + " 个用户：");
        for (Users u : users) {
            System.out.println("  " + u);
        }
        session.close();
    }

    /** 修改用户（动态 SQL：只更新非空字段） */
    @Test
    public void updateUserTest() {
        SqlSession session = MyBatisUtils.getSession();
        UserMapper mapper = session.getMapper(UserMapper.class);

        Users user = new Users();
        user.setId(1);
        user.setEmail("joy_new@example.com");
        // username、password 留空，动态 SQL 不会更新这两列

        int rows = mapper.updateUser(user);
        session.commit();
        System.out.println("修改用户成功，影响行数：" + rows);
        System.out.println("修改后：" + mapper.findUserById(1));
        session.close();
    }

    /** 删除用户 */
    @Test
    public void deleteUserTest() {
        SqlSession session = MyBatisUtils.getSession();
        UserMapper mapper = session.getMapper(UserMapper.class);

        // 先新增一个临时用户，再删除，避免影响固有测试数据
        Users temp = new Users();
        temp.setUsername("temp_" + System.currentTimeMillis());
        temp.setPassword("temp123");
        temp.setEmail("temp@example.com");
        mapper.addUser(temp);
        session.commit();
        System.out.println("已新增临时用户，ID = " + temp.getId());

        int rows = mapper.deleteUser(temp.getId());
        session.commit();
        System.out.println("删除用户成功，影响行数：" + rows);
        System.out.println("删除后查询结果：" + mapper.findUserById(temp.getId()));
        session.close();
    }

    /** 多条件动态查询 */
    @Test
    public void findUserByConditionTest() {
        SqlSession session = MyBatisUtils.getSession();
        UserMapper mapper = session.getMapper(UserMapper.class);

        Users condition = new Users();
        condition.setUsername("o");  // 用户名包含字母 o
        List<Users> users = mapper.findUserByCondition(condition);
        System.out.println("按条件查询到 " + users.size() + " 个用户：");
        for (Users u : users) {
            System.out.println("  " + u);
        }
        session.close();
    }

    // ======================== products 表 CRUD ========================

    /** 新增商品 */
    @Test
    public void addProductTest() {
        SqlSession session = MyBatisUtils.getSession();
        ProductMapper mapper = session.getMapper(ProductMapper.class);

        Products product = new Products();
        product.setName("SpringBoot实战");
        product.setPrice(59.9);
        product.setStock(50);

        int rows = mapper.addProduct(product);
        session.commit();
        System.out.println("新增商品成功，影响行数：" + rows + "，主键ID：" + product.getId());
        session.close();
    }

    /** 根据ID查询商品 */
    @Test
    public void findProductByIdTest() {
        SqlSession session = MyBatisUtils.getSession();
        ProductMapper mapper = session.getMapper(ProductMapper.class);

        System.out.println("查询商品：" + mapper.findProductById(1));
        session.close();
    }

    /** 查询全部商品 */
    @Test
    public void findAllProductsTest() {
        SqlSession session = MyBatisUtils.getSession();
        ProductMapper mapper = session.getMapper(ProductMapper.class);

        List<Products> list = mapper.findAllProducts();
        System.out.println("共查询到 " + list.size() + " 个商品：");
        for (Products p : list) {
            System.out.println("  " + p);
        }
        session.close();
    }

    /** 修改商品 */
    @Test
    public void updateProductTest() {
        SqlSession session = MyBatisUtils.getSession();
        ProductMapper mapper = session.getMapper(ProductMapper.class);

        Products product = new Products();
        product.setId(1);
        product.setName("Java基础入门（第2版）");
        product.setPrice(48.0);
        product.setStock(150);

        int rows = mapper.updateProduct(product);
        session.commit();
        System.out.println("修改商品成功，影响行数：" + rows);
        System.out.println("修改后：" + mapper.findProductById(1));
        session.close();
    }

    /** 删除商品 */
    @Test
    public void deleteProductTest() {
        SqlSession session = MyBatisUtils.getSession();
        ProductMapper mapper = session.getMapper(ProductMapper.class);

        Products temp = new Products();
        temp.setName("待删除测试商品");
        temp.setPrice(1.0);
        temp.setStock(1);
        mapper.addProduct(temp);
        session.commit();

        int rows = mapper.deleteProduct(temp.getId());
        session.commit();
        System.out.println("删除商品成功，影响行数：" + rows);
        session.close();
    }

    /** 名称模糊查询（注解方式） */
    @Test
    public void findProductByNameTest() {
        SqlSession session = MyBatisUtils.getSession();
        ProductMapper mapper = session.getMapper(ProductMapper.class);

        List<Products> list = mapper.findProductByName("Java");
        System.out.println("名称含 Java 的商品共 " + list.size() + " 个：");
        for (Products p : list) {
            System.out.println("  " + p);
        }
        session.close();
    }

    /** 名称模糊查询（映射文件方式） */
    @Test
    public void searchProductByNameTest() {
        SqlSession session = MyBatisUtils.getSession();
        ProductMapper mapper = session.getMapper(ProductMapper.class);

        List<Products> list = mapper.searchProductByName("框架");
        System.out.println("名称含“框架”的商品共 " + list.size() + " 个：");
        for (Products p : list) {
            System.out.println("  " + p);
        }
        session.close();
    }
}
