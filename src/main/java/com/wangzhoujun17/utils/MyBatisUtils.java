package com.wangzhoujun17.utils;

import java.io.IOException;
import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

/**
 * MyBatis 工具类：负责读取核心配置文件并创建全局唯一的 SqlSessionFactory。
 * 通过 getSession() 获取 SqlSession 执行数据库操作。
 */
public class MyBatisUtils {

    private static SqlSessionFactory sqlSessionFactory = null;

    static {
        try {
            // 读取 mybatis-config.xml 核心配置文件
            Reader reader = Resources.getResourceAsReader("mybatis-config.xml");
            // 构建 SqlSessionFactory（全局唯一，静态代码块只执行一次）
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取 SqlSession 对象。
     * 默认不自动提交事务，执行增删改操作后需手动调用 commit()。
     */
    public static SqlSession getSession() {
        return sqlSessionFactory.openSession();
    }
}
