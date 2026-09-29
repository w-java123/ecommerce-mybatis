package com.wangzhoujun17.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import com.wangzhoujun17.mapper.ProductMapper;
import com.wangzhoujun17.mapper.UserMapper;
import com.wangzhoujun17.po.Products;
import com.wangzhoujun17.po.Users;
import com.wangzhoujun17.utils.MyBatisUtils;

/**
 * 用户表 users 与商品表 products 的完整 CRUD 测试（注解方式）。
 * 测试数据来自 db.sql 种子数据，演示前请先执行 reset-db.bat。
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
        try {
            UserMapper mapper = session.getMapper(UserMapper.class);
            Users user = mapper.findUserById(1);

            assertNotNull("用户1应存在", user);
            assertEquals("用户ID应为1", Integer.valueOf(1), user.getId());
            assertEquals("用户名应为joy", "joy", user.getUsername());
            assertNotNull("密码不应为null", user.getPassword());
            assertNotNull("邮箱不应为null", user.getEmail());

            // 查询不存在的ID应返回 null
            assertNull("不存在的用户应返回null", mapper.findUserById(999999));
        } finally {
            session.close();
        }
    }

    /** 查询全部用户 */
    @Test
    public void findAllUsersTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            UserMapper mapper = session.getMapper(UserMapper.class);
            List<Users> users = mapper.findAllUsers();

            assertNotNull("结果不应为null", users);
            assertEquals("种子数据应有3个用户", 3, users.size());
            // 按 id 排序，逐个校验字段非空
            for (int i = 0; i < users.size(); i++) {
                Users u = users.get(i);
                assertEquals("第" + (i + 1) + "个用户ID应为" + (i + 1),
                        Integer.valueOf(i + 1), u.getId());
                assertNotNull("用户名不应为null", u.getUsername());
                assertNotNull("密码不应为null", u.getPassword());
            }
        } finally {
            session.close();
        }
    }

    /** 修改用户（动态 SQL：只更新非空字段） */
    @Test
    public void updateUserTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            UserMapper mapper = session.getMapper(UserMapper.class);

            Users before = mapper.findUserById(1);
            String emailBefore = before.getEmail();
            String usernameBefore = before.getUsername();

            Users user = new Users();
            user.setId(1);
            user.setEmail("joy_new@example.com");
            // username、password 留空，动态 SQL 不会更新这两列

            int rows = mapper.updateUser(user);
            session.commit();
            assertEquals("应更新1行", 1, rows);

            Users after = mapper.findUserById(1);
            assertEquals("邮箱应已更新", "joy_new@example.com", after.getEmail());
            assertEquals("未传的 username 应保持原值",
                    usernameBefore, after.getUsername());

            // 还原，避免污染种子数据
            Users restore = new Users();
            restore.setId(1);
            restore.setEmail(emailBefore);
            mapper.updateUser(restore);
            session.commit();
            assertEquals("邮箱应已还原", emailBefore,
                    mapper.findUserById(1).getEmail());
        } finally {
            session.close();
        }
    }

    /** 删除用户 */
    @Test
    public void deleteUserTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            UserMapper mapper = session.getMapper(UserMapper.class);

            // 先新增一个临时用户，再删除，避免影响固有测试数据
            Users temp = new Users();
            temp.setUsername("temp_" + System.currentTimeMillis());
            temp.setPassword("temp123");
            temp.setEmail("temp@example.com");
            mapper.addUser(temp);
            session.commit();
            assertNotNull("新增后主键应被回填", temp.getId());
            assertNotNull("删除前应能查到", mapper.findUserById(temp.getId()));

            int rows = mapper.deleteUser(temp.getId());
            session.commit();
            assertEquals("应删除1行", 1, rows);
            assertNull("删除后应查不到", mapper.findUserById(temp.getId()));

            // 删除不存在的记录应返回0行
            assertEquals("删除不存在的用户应影响0行", 0, mapper.deleteUser(999999));
        } finally {
            session.close();
        }
    }

    /** 多条件动态查询 */
    @Test
    public void findUserByConditionTest() {
        SqlSession session = MyBatisUtils.getSession();
        UserMapper mapper = session.getMapper(UserMapper.class);

        Users condition = new Users();
        condition.setUsername("o");  // 用户名包含字母 o
        List<Users> users = mapper.findUserByCondition(condition);
        assertNotNull(users);
        // joy 含 o，tom 含 o，jack 不含 -> 应为2个
        assertEquals("用户名含字母o的应有2个", 2, users.size());
        for (Users u : users) {
            assertTrue("结果应都含字母o", u.getUsername().contains("o"));
        }
        session.close();
    }

    // ======================== products 表 CRUD ========================

    /** 新增商品 */
    @Test
    public void addProductTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            ProductMapper mapper = session.getMapper(ProductMapper.class);

            Products product = new Products();
            product.setName("SpringBoot实战_" + System.currentTimeMillis());
            product.setPrice(59.9);
            product.setStock(50);

            int rows = mapper.addProduct(product);
            session.commit();

            assertEquals("应插入1行", 1, rows);
            assertNotNull("主键应被回填", product.getId());

            Products loaded = mapper.findProductById(product.getId());
            assertNotNull("插入后应能查到", loaded);
            assertEquals(product.getName(), loaded.getName());
            assertEquals(Double.valueOf(59.9), loaded.getPrice());
            assertEquals(Integer.valueOf(50), loaded.getStock());

            // 清理，保证可重复执行
            mapper.deleteProduct(product.getId());
            session.commit();
        } finally {
            session.close();
        }
    }

    /** 根据ID查询商品 */
    @Test
    public void findProductByIdTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            ProductMapper mapper = session.getMapper(ProductMapper.class);

            Products p = mapper.findProductById(1);
            assertNotNull("商品1应存在", p);
            assertEquals(Integer.valueOf(1), p.getId());
            assertEquals("Java基础入门", p.getName());
            assertEquals(Double.valueOf(45.0), p.getPrice());
            assertEquals(Integer.valueOf(100), p.getStock());

            assertNull("不存在的商品应返回null", mapper.findProductById(999999));
        } finally {
            session.close();
        }
    }

    /** 查询全部商品 */
    @Test
    public void findAllProductsTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            ProductMapper mapper = session.getMapper(ProductMapper.class);

            List<Products> list = mapper.findAllProducts();
            assertNotNull(list);
            assertEquals("种子数据应有4个商品", 4, list.size());
            for (Products p : list) {
                assertNotNull("商品名不应为null", p.getName());
                assertNotNull("价格不应为null", p.getPrice());
                assertNotNull("库存不应为null", p.getStock());
            }
        } finally {
            session.close();
        }
    }

    /** 修改商品（注解方式，更新全部字段） */
    @Test
    public void updateProductTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            ProductMapper mapper = session.getMapper(ProductMapper.class);
            Products before = mapper.findProductById(1);

            Products product = new Products();
            product.setId(1);
            product.setName("Java基础入门（第2版）");
            product.setPrice(48.0);
            product.setStock(150);

            int rows = mapper.updateProduct(product);
            session.commit();
            assertEquals("应更新1行", 1, rows);

            Products after = mapper.findProductById(1);
            assertEquals("名称应已更新", "Java基础入门（第2版）", after.getName());
            assertEquals("价格应已更新", Double.valueOf(48.0), after.getPrice());
            assertEquals("库存应已更新", Integer.valueOf(150), after.getStock());

            // 还原种子数据
            mapper.updateProduct(before);
            session.commit();
            assertEquals("应已还原", before.getName(),
                    mapper.findProductById(1).getName());
        } finally {
            session.close();
        }
    }

    /** 删除商品 */
    @Test
    public void deleteProductTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            ProductMapper mapper = session.getMapper(ProductMapper.class);

            Products temp = new Products();
            temp.setName("待删除测试商品_" + System.currentTimeMillis());
            temp.setPrice(1.0);
            temp.setStock(1);
            mapper.addProduct(temp);
            session.commit();
            assertNotNull("删除前应能查到", mapper.findProductById(temp.getId()));

            int rows = mapper.deleteProduct(temp.getId());
            session.commit();
            assertEquals("应删除1行", 1, rows);
            assertNull("删除后应查不到", mapper.findProductById(temp.getId()));

            assertEquals("删除不存在的商品应影响0行",
                    0, mapper.deleteProduct(999999));
        } finally {
            session.close();
        }
    }

    /** 名称模糊查询（注解方式） */
    @Test
    public void findProductByNameTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            ProductMapper mapper = session.getMapper(ProductMapper.class);

            List<Products> list = mapper.findProductByName("Java");
            assertNotNull(list);
            // Java基础入门、JavaWeb程序设计
            assertEquals("名称含Java的应有2个", 2, list.size());
            for (Products p : list) {
                assertTrue("结果名称应含Java", p.getName().contains("Java"));
            }

            assertEquals("查无匹配应返回空集合",
                    0, mapper.findProductByName("不存在的商品名xyz").size());
        } finally {
            session.close();
        }
    }

    /** 名称模糊查询（映射文件方式） */
    @Test
    public void searchProductByNameTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            ProductMapper mapper = session.getMapper(ProductMapper.class);

            List<Products> list = mapper.searchProductByName("框架");
            assertNotNull(list);
            assertEquals("名称含“框架”的应有1个", 1, list.size());
            assertEquals("SSM框架整合实战", list.get(0).getName());
        } finally {
            session.close();
        }
    }
}
