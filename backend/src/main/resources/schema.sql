DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS cart_item;
DROP TABLE IF EXISTS cart;
DROP TABLE IF EXISTS delivery;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS users;

-- 회원(User)
CREATE TABLE users
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50),
    email       VARCHAR(100) UNIQUE NOT NULL,
    password    VARCHAR(255),
    address     VARCHAR(255),
    phone       VARCHAR(30),
    join_date   TIMESTAMP,
    update_date TIMESTAMP,
    role        ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER'
);

-- 카테고리(Category)
CREATE TABLE category
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(100),
    parent_id BIGINT,
    FOREIGN KEY (parent_id) REFERENCES category (id)
);

-- 상품(Product)
CREATE TABLE product
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100),
    image_url   VARCHAR(255),
    price       INT,
    stock       INT,
    description VARCHAR(500),
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    category_id BIGINT,
    FOREIGN KEY (category_id) REFERENCES category (id)
);

-- 배송(Delivery) → 먼저 생성
CREATE TABLE delivery
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    address         VARCHAR(255),
    start_date      TIMESTAMP,
    complete_date   TIMESTAMP,
    tracking_number VARCHAR(100),
    status          ENUM('배송준비중', '배송중', '배송완료') NOT NULL DEFAULT '배송중',
    company         VARCHAR(50)
);

-- 주문(Order)
CREATE TABLE orders
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT,
    total_price      INT,
    order_date       TIMESTAMP,
    shipping_address VARCHAR(255),
    status           ENUM('결제대기','결제완료', '배송준비중', '배송중', '배송완료', '취소', '환불') NOT NULL DEFAULT '결제대기',
    delivery_id      BIGINT,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (delivery_id) REFERENCES delivery (id)
);

-- 장바구니(Cart)
CREATE TABLE cart
(
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- 장바구니_상품(Cart_Item)
CREATE TABLE cart_item
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id    BIGINT,
    product_id BIGINT,
    quantity   INT,
    FOREIGN KEY (cart_id) REFERENCES cart (id),
    FOREIGN KEY (product_id) REFERENCES product (id)
);

-- 주문상품(Order_Item)
CREATE TABLE order_item
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id   BIGINT,
    product_id BIGINT,
    quantity   INT,
    unit_price INT,
    FOREIGN KEY (order_id) REFERENCES orders (id),
    FOREIGN KEY (product_id) REFERENCES product (id)
);
