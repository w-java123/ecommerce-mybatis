-- =============================================================
-- 选题 2：电子商务网站
-- 数据库：ecommerce
-- 表：users / products / orders / orderdetails
-- 关联关系：
--   一对多 users(1) -> orders(N)
--   多对多 orders(N) <-> orderdetails <-> products(N)
-- =============================================================

DROP DATABASE IF EXISTS ecommerce;
CREATE DATABASE ecommerce DEFAULT CHARSET=utf8mb4;
USE ecommerce;

-- -------------------------------------------------------------
-- 1. 用户表 users （一对多关系中的“一”方）
-- -------------------------------------------------------------
CREATE TABLE users (
    id       INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID，主键',
    username VARCHAR(50)  NOT NULL UNIQUE    COMMENT '用户名',
    password VARCHAR(50)  NOT NULL           COMMENT '密码',
    email    VARCHAR(100)                    COMMENT '邮箱'
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

INSERT INTO users (username, password, email) VALUES
('joy',   '123456', 'joy@example.com'),
('jack',  '123456', 'jack@example.com'),
('tom',   '123456', 'tom@example.com');

-- -------------------------------------------------------------
-- 2. 商品表 products
-- -------------------------------------------------------------
CREATE TABLE products (
    id    INT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID，主键',
    name  VARCHAR(100) NOT NULL            COMMENT '商品名称',
    price DOUBLE       NOT NULL            COMMENT '商品单价',
    stock INT          NOT NULL DEFAULT 0  COMMENT '库存数量'
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

INSERT INTO products (name, price, stock) VALUES
('Java基础入门',        45.0, 100),
('JavaWeb程序设计',     50.0, 80),
('SSM框架整合实战',     62.5, 60),
('MySQL数据库原理',     39.9, 120);

-- -------------------------------------------------------------
-- 3. 订单表 orders （一对多关系中的“多”方，user_id 为外键）
-- -------------------------------------------------------------
CREATE TABLE orders (
    id         INT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID，主键',
    user_id    INT  NOT NULL                  COMMENT '下单用户ID，外键',
    order_date DATE NOT NULL                  COMMENT '下单日期',
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

INSERT INTO orders (user_id, order_date) VALUES
(1, '2026-09-20'),
(1, '2026-09-22'),
(2, '2026-09-25'),
(3, '2026-09-28');

-- -------------------------------------------------------------
-- 4. 订单详情表 orderdetails （订单与商品的多对多中间表）
-- -------------------------------------------------------------
CREATE TABLE orderdetails (
    id         INT PRIMARY KEY AUTO_INCREMENT COMMENT '详情ID，主键',
    order_id   INT NOT NULL                   COMMENT '订单ID，外键',
    product_id INT NOT NULL                   COMMENT '商品ID，外键',
    quantity   INT NOT NULL DEFAULT 1         COMMENT '购买数量',
    CONSTRAINT fk_detail_order   FOREIGN KEY (order_id)   REFERENCES orders(id),
    CONSTRAINT fk_detail_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

INSERT INTO orderdetails (order_id, product_id, quantity) VALUES
(1, 1, 2),
(1, 3, 1),
(2, 2, 5),
(3, 1, 1),
(3, 4, 3),
(4, 2, 2);
