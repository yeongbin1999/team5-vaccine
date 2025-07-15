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
    id          INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(20),
    email       VARCHAR(100) UNIQUE NOT NULL,
    password    VARCHAR(60) NOT NULL,
    address     VARCHAR(200),
    phone       VARCHAR(20),
    join_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    role        ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER'
);

-- 카테고리(Category)
CREATE TABLE category
(
    id        INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(50) NOT NULL,
    parent_id INT UNSIGNED,
    FOREIGN KEY (parent_id) REFERENCES category (id)
);

-- 상품(Product)
CREATE TABLE product
(
    id          INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    image_url   VARCHAR(500),
    price       INT UNSIGNED NOT NULL,
    stock       INT UNSIGNED NOT NULL,
    description TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    category_id INT UNSIGNED,
    FOREIGN KEY (category_id) REFERENCES category (id)
);

-- 배송(Delivery)
CREATE TABLE delivery
(
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    address         VARCHAR(200) NOT NULL,
    start_date      TIMESTAMP,
    complete_date   TIMESTAMP,
    tracking_number VARCHAR(50),
    status          ENUM('배송준비중', '배송중', '배송완료') NOT NULL DEFAULT '배송준비중',
    company         VARCHAR(50)
);

-- 주문(Order)
CREATE TABLE orders
(
    id               INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id          INT UNSIGNED NOT NULL,
    total_price      INT UNSIGNED NOT NULL,
    order_date       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    shipping_address VARCHAR(200),
    status           ENUM('배송준비중','배송중','배송완료','취소') NOT NULL DEFAULT '배송준비중',
    delivery_id      INT UNSIGNED,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (delivery_id) REFERENCES delivery (id)
);

-- 장바구니(Cart)
CREATE TABLE cart
(
    id      INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- 장바구니_상품(Cart_Item)
CREATE TABLE cart_item
(
    id         INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    cart_id    INT UNSIGNED NOT NULL,
    product_id INT UNSIGNED NOT NULL,
    quantity   INT UNSIGNED NOT NULL,
    FOREIGN KEY (cart_id) REFERENCES cart (id),
    FOREIGN KEY (product_id) REFERENCES product (id)
);

-- 주문상품(Order_Item)
CREATE TABLE order_item
(
    id         INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_id   INT UNSIGNED NOT NULL,
    product_id INT UNSIGNED NOT NULL,
    quantity   INT UNSIGNED NOT NULL,
    unit_price INT UNSIGNED NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders (id),
    FOREIGN KEY (product_id) REFERENCES product (id)
);
