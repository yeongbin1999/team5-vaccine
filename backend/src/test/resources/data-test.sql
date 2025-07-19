-- data-test.sql
-- 테스트 환경을 위한 초기 데이터 삽입 스크립트입니다.
-- 매 테스트 실행 전에 데이터베이스 스키마가 초기화되고 (schema.sql), 이 스크립트가 실행됩니다.

-- 회원(User)
INSERT INTO users (id, name, email, password, address, phone, role)
VALUES (1, '관리자', 'admin@test.com', 'admin', '서울시 송파구', '010-3333-4444', 'ADMIN'),
       (2, '유저1', 'user1@test.com', '1234', '서울시 강남구', '010-1111-2222', 'USER'),
       (3, '유저2', 'user2@test.com', 'abcd', '서울시 마포구', '010-5555-6666', 'USER'),
       (4, '유저3', 'user3@test.com', 'qwer', '서울시 종로구', '010-7777-8888', 'USER'),
       (5, '유저4', 'user4@test.com', 'asdf', '서울시 영등포구', '010-9999-0000', 'USER');
-- 유저4는 장바구니 없음 (테스트 케이스를 위해 유지)

-- 카테고리(Category)
INSERT INTO category (id, name, parent_id)
VALUES (1, '식품', NULL),
       (2, '커피빈', 1),
       (3, '음료', NULL), -- 최신 data.sql에서 추가된 카테고리
       (4, '주스', 3),    -- 최신 data.sql에서 추가된 카테고리
       (5, '차', 1);
-- 최신 data.sql에서 추가된 카테고리

-- 상품(Product)
INSERT INTO product (id, name, image_url, price, stock, description, category_id)
VALUES (1, '에티오피아 예가체프', 'https://dummyimg.com/ethiopia.jpg', 18000, 50, '플로럴하고 밝은 산미의 예가체프 원두', 2),
       (2, '콜롬비아 수프리모', 'https://dummyimg.com/colombia.jpg', 17000, 60, '부드럽고 밸런스 좋은 콜롬비아 수프리모 원두', 2),
       (3, '브라질 산토스', 'https://dummyimg.com/brazil.jpg', 16000, 80, '견과류 향이 풍부한 브라질 산토스 원두', 2),
       (4, '케냐 AA', 'https://dummyimg.com/kenya.jpg', 19000, 40, '진한 바디감과 과일향의 케냐 AA 원두', 2),
       (5, '과테말라 안티구아', 'https://dummyimg.com/guatemala.jpg', 21000, 30, '스모키하고 풍부한 맛의 과테말라 안티구아 원두', 2);
-- 최신 data.sql에서 추가된 상품

-- 장바구니(Cart)
INSERT INTO cart (id, user_id)
VALUES (1, 2), -- 유저1 장바구니
       (2, 3), -- 유저2 장바구니
       (3, 4);
-- 유저3 장바구니 (테스트 케이스를 위해 유지)

-- 장바구니_상품(Cart_Item)
INSERT INTO cart_item (cart_id, product_id, quantity)
VALUES (1, 1, 1), -- 유저1 장바구니에 에티오피아 예가체프 1개
       (1, 2, 1), -- 유저1 장바구니에 콜롬비아 수프리모 1개
       (2, 3, 2), -- 유저2 장바구니에 브라질 산토스 2개
       (3, 1, 1), -- 유저3 - 예가체프 1개 (테스트 케이스를 위해 유지)
       (3, 2, 2), -- 유저3 - 콜롬비아 2개 (테스트 케이스를 위해 유지)
       (3, 4, 3);
-- 유저3 - 케냐 3개 (테스트 케이스를 위해 유지)

-- 주문(Order)
INSERT INTO orders (id, user_id, total_price, shipping_address, status)
VALUES (1, 2, 35000, '서울시 강남구', '배송준비중'), -- 유저1 주문 1
       (2, 3, 32000, '서울시 마포구', '배송준비중'), -- 유저2 주문 2
       (3, 2, 19000, '서울시 강남구', '배송중');
-- 유저1 주문 3

-- 주문상품(Order_Item)
INSERT INTO order_item (id, order_id, product_id, quantity, unit_price)
VALUES (1, 1, 1, 1, 18000), -- 주문 1: 에티오피아 예가체프 1개
       (2, 1, 2, 1, 17000), -- 주문 1: 콜롬비아 수프리모 1개
       (3, 2, 3, 2, 16000), -- 주문 2: 브라질 산토스 2개
       (4, 3, 4, 1, 19000);
-- 주문 3: 케냐 AA 1개

-- 배송(Delivery)
INSERT INTO delivery (id, address, tracking_number, status, company)
VALUES (1, '서울시 강남구', 'TRK987654', '배송중', 'CJ대한통운'),
       (2, '서울시 마포구', 'TRK123456', '배송중', '한진택배'),
       (3, '서울시 강남구', 'TRK654321', '배송완료', '로젠택배');