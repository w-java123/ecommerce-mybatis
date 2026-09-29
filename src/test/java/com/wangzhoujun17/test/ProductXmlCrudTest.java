package com.wangzhoujun17.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import com.wangzhoujun17.mapper.ProductMapper;
import com.wangzhoujun17.po.Products;
import com.wangzhoujun17.utils.MyBatisUtils;

/**
 * ProductMapper 映射文件（XML）方式的 CRUD 测试。
 * 与 CrudTest 中的注解方式互为对照，验证两套写法行为一致。
 *
 * 测试数据来自 db.sql 种子数据：
 *   1=Java基础入门(45.0, 100)  2=JavaWeb程序设计(50.0, 80)
 *   3=SSM框架整合实战(62.5, 60)  4=MySQL数据库原理(39.9, 120)
 */
public class ProductXmlCrudTest {

    /** 查：根据ID（resultMap 映射） */
    @Test
    public void selectProductByIdTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            ProductMapper mapper = session.getMapper(ProductMapper.class);
            Products p = mapper.selectProductById(1);

            assertNotNull("商品1应存在", p);
            assertEquals(Integer.valueOf(1), p.getId());
            assertEquals("Java基础入门", p.getName());
            assertEquals(Double.valueOf(45.0), p.getPrice());
            assertEquals(Integer.valueOf(100), p.getStock());
        } finally {
            session.close();
        }
    }

    /** 查：全部 */
    @Test
    public void selectAllProductsTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            ProductMapper mapper = session.getMapper(ProductMapper.class);
            List<Products> list = mapper.selectAllProducts();

            assertNotNull(list);
            assertEquals("种子数据应有4个商品", 4, list.size());
            // 按 id 排序，逐个校验
            for (int i = 0; i < list.size(); i++) {
                assertEquals("第" + (i + 1) + "个商品ID应为" + (i + 1),
                        Integer.valueOf(i + 1), list.get(i).getId());
                assertNotNull("商品名不应为null", list.get(i).getName());
                assertNotNull("价格不应为null", list.get(i).getPrice());
                assertNotNull("库存不应为null", list.get(i).getStock());
            }
        } finally {
            session.close();
        }
    }

    /** 增：插入并回填主键，随后删除 */
    @Test
    public void insertProductTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            ProductMapper mapper = session.getMapper(ProductMapper.class);

            Products p = new Products();
            p.setName("测试商品XML_" + System.currentTimeMillis());
            p.setPrice(12.5);
            p.setStock(7);

            int rows = mapper.insertProduct(p);
            session.commit();

            assertEquals("应插入1行", 1, rows);
            assertNotNull("主键应被回填", p.getId());

            // 回读校验，确认字段都正确落库
            Products loaded = mapper.selectProductById(p.getId());
            assertNotNull("插入后应能查到", loaded);
            assertEquals(p.getName(), loaded.getName());
            assertEquals(Double.valueOf(12.5), loaded.getPrice());
            assertEquals(Integer.valueOf(7), loaded.getStock());

            // 清理
            mapper.deleteProductById(p.getId());
            session.commit();
            assertNull("删除后应查不到", mapper.selectProductById(p.getId()));
        } finally {
            session.close();
        }
    }

    /** 删：删除不存在的记录应返回0行 */
    @Test
    public void deleteNonExistentProductTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            ProductMapper mapper = session.getMapper(ProductMapper.class);
            int rows = mapper.deleteProductById(999999);
            assertEquals("删除不存在的商品应影响0行", 0, rows);
        } finally {
            session.close();
        }
    }

    /** 改：动态 SQL 只更新非空字段，未传字段保持原值 */
    @Test
    public void updateProductSelectiveTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            ProductMapper mapper = session.getMapper(ProductMapper.class);

            Products before = mapper.selectProductById(2);
            String nameBefore = before.getName();
            Double priceBefore = before.getPrice();

            // 只改库存，name 和 price 不传
            Products upd = new Products();
            upd.setId(2);
            upd.setStock(999);
            int rows = mapper.updateProductSelective(upd);
            session.commit();
            assertEquals("应更新1行", 1, rows);

            Products after = mapper.selectProductById(2);
            assertEquals("库存应更新为999", Integer.valueOf(999), after.getStock());
            assertEquals("未传的 name 应保持原值", nameBefore, after.getName());
            assertEquals("未传的 price 应保持原值", priceBefore, after.getPrice());

            // 还原
            Products restore = new Products();
            restore.setId(2);
            restore.setStock(before.getStock());
            mapper.updateProductSelective(restore);
            session.commit();
            assertEquals("库存应已还原", before.getStock(),
                    mapper.selectProductById(2).getStock());
        } finally {
            session.close();
        }
    }

    /** 查：动态 SQL 组合条件 */
    @Test
    public void searchProductTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            ProductMapper mapper = session.getMapper(ProductMapper.class);

            // 只按名称模糊查
            Map<String, Object> onlyName = new HashMap<>();
            onlyName.put("name", "Java");
            List<Products> byName = mapper.searchProduct(onlyName);
            assertEquals("名称含Java的商品应有2个", 2, byName.size());

            // 只按库存下限查：种子数据中 stock>=80 的有 100/80/120 共3个
            Map<String, Object> onlyStock = new HashMap<>();
            onlyStock.put("minStock", 80);
            List<Products> byStock = mapper.searchProduct(onlyStock);
            assertEquals("库存>=80的商品应有3个", 3, byStock.size());

            // 两个条件组合：名称含Java 且 库存>=90 -> 只剩 1(100) 和 4(120)? 4名不含Java
            // 实际：1=Java基础入门(100) 2=JavaWeb程序设计(80) -> 库存>=90 只剩 1
            Map<String, Object> both = new HashMap<>();
            both.put("name", "Java");
            both.put("minStock", 90);
            List<Products> combined = mapper.searchProduct(both);
            assertEquals("名称含Java且库存>=90应只剩1个", 1, combined.size());
            assertEquals(Integer.valueOf(1), combined.get(0).getId());

            // 都不传 -> 返回全部
            List<Products> all = mapper.searchProduct(new HashMap<>());
            assertEquals("无条件应返回全部4个", 4, all.size());
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
            assertEquals("名称含“框架”的应有1个", 1, list.size());
            assertEquals("SSM框架整合实战", list.get(0).getName());

            // 注解版与 XML 版结果应一致
            List<Products> byAnnotation = mapper.findProductByName("Java");
            List<Products> byXml = mapper.searchProductByName("Java");
            assertEquals("两种写法结果应一致", byAnnotation.size(), byXml.size());
            assertFalse("Java 类商品不应为空", byXml.isEmpty());
        } finally {
            session.close();
        }
    }

    /** 两套 CRUD（注解版 / XML 版）对同一数据应读到相同结果 */
    @Test
    public void annotationAndXmlAgreeTest() {
        SqlSession session = MyBatisUtils.getSession();
        try {
            ProductMapper mapper = session.getMapper(ProductMapper.class);

            for (int id = 1; id <= 4; id++) {
                Products byAnnotation = mapper.findProductById(id);
                Products byXml = mapper.selectProductById(id);

                assertNotNull("注解版应查到商品" + id, byAnnotation);
                assertNotNull("XML版应查到商品" + id, byXml);
                assertEquals("商品" + id + " 的ID应一致",
                        byAnnotation.getId(), byXml.getId());
                assertEquals("商品" + id + " 的名称应一致",
                        byAnnotation.getName(), byXml.getName());
                assertEquals("商品" + id + " 的价格应一致",
                        byAnnotation.getPrice(), byXml.getPrice());
                assertEquals("商品" + id + " 的库存应一致",
                        byAnnotation.getStock(), byXml.getStock());
            }

            assertEquals("两套查全部结果数量应一致",
                    mapper.findAllProducts().size(),
                    mapper.selectAllProducts().size());
        } finally {
            session.close();
        }
    }
}
