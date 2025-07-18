-- 회원(User)
INSERT INTO users (id, name, email, password, address, phone, role) VALUES
(1, '관리자', 'admin@test.com', 'admin', '서울시 송파구', '010-3333-4444', 'ADMIN'),
(2, '유저1', 'user1@test.com', '1234', '서울시 강남구', '010-1111-2222', 'USER'),
(3, '유저2', 'user2@test.com', 'abcd', '서울시 마포구', '010-5555-6666', 'USER');

-- 카테고리(Category)
INSERT INTO category (id, name, parent_id) VALUES
(1, '식품', NULL),
(2, '커피빈', 1),
(3, '음료', NULL),
(4, '주스', 3),
(5, '차', 1);

-- 상품(Product)
INSERT INTO product (id, name, image_url, price, stock, description, category_id) VALUES
(1, '에티오피아 예가체프', 'https://dummyimg.com/ethiopia.jpg', 18000, 50, '플로럴하고 밝은 산미의 예가체프 원두', 2),
(2, '콜롬비아 수프리모', 'https://dummyimg.com/colombia.jpg', 17000, 60, '부드럽고 밸런스 좋은 콜롬비아 수프리모 원두', 2),
(3, '브라질 산토스', 'https://dummyimg.com/brazil.jpg', 16000, 80, '견과류 향이 풍부한 브라질 산토스 원두', 2),
(4, '케냐 AA', 'https://dummyimg.com/kenya.jpg', 19000, 40, '진한 바디감과 과일향의 케냐 AA 원두', 2);

-- 장바구니(Cart)
INSERT INTO cart (id, user_id) VALUES
(1, 2),  -- 유저1 장바구니
(2, 3);  -- 유저2 장바구니

-- 장바구니_상품(Cart_Item)
INSERT INTO cart_item (cart_id, product_id, quantity) VALUES
(1, 1, 1),  -- 유저1 장바구니에 에티오피아 예가체프 1개
(1, 2, 1),  -- 유저1 장바구니에 콜롬비아 수프리모 1개
(2, 3, 2);  -- 유저2 장바구니에 브라질 산토스 2개

-- 주문(Order)
INSERT INTO orders (id, user_id, total_price, shipping_address, status) VALUES
(1, 2, 35000, '서울시 강남구', '배송준비중'),  -- 유저1 주문 1
(2, 3, 32000, '서울시 마포구', '배송준비중'),  -- 유저2 주문 2
(3, 2, 19000, '서울시 강남구', '배송중');    -- 유저1 주문 3

-- 주문상품(Order_Item)
INSERT INTO order_item (id, order_id, product_id, quantity, unit_price) VALUES
(1, 1, 1, 1, 18000),  -- 주문 1: 에티오피아 예가체프 1개
(2, 1, 2, 1, 17000),  -- 주문 1: 콜롬비아 수프리모 1개
(3, 2, 3, 2, 16000),  -- 주문 2: 브라질 산토스 2개
(4, 3, 4, 1, 19000);  -- 주문 3: 케냐 AA 1개

-- 배송(Delivery)
INSERT INTO delivery (id, address, tracking_number, status, company) VALUES
(1, '서울시 강남구', 'TRK987654', '배송중', 'CJ대한통운'),
(2, '서울시 마포구', 'TRK123456', '배송중', '한진택배'),
(3, '서울시 강남구', 'TRK654321', '배송완료', '로젠택배');
