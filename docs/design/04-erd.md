# ERD

~~~mermaid
erDiagram

    %% 관계 정의
    USER ||--o{ PRODUCT_LIKE : has
    PRODUCT ||--o{ PRODUCT_LIKE : liked_by
    BRAND ||--o{ PRODUCT : owns
    USER ||--o{ ORDER : places
    ORDER ||--|{ ORDER_ITEM : includes
    PRODUCT ||--|{ ORDER_ITEM : contains
    ORDER ||--|| PAYMENT : paid_with
    USER ||--o{ POINT : earns

    %% USER 테이블
    USER {
        VARCHAR id PK "사용자 ID"
        VARCHAR user_id UK "사용자 로그인 ID"
        VARCHAR name "사용자 이름"
        VARCHAR email "이메일 주소"
        DATE birth_date "생년월일"
        VARCHAR gender "성별"
    }

    %% PRODUCT 테이블
    PRODUCT {
        INT id PK "상품 ID"
        INT brand_id FK "브랜드 ID"
        VARCHAR name "상품명"
        VARCHAR main_image_url "메인 상품 이미지 URL"
        VARCHAR description "상품 상세 내용"
        INT price "가격"
        INT stock_quantity "재고 수량"
        INT like_count "좋아요 수"
    }

    %% BRAND 테이블
    BRAND {
        INT id PK "브랜드 ID"
        VARCHAR name "브랜드명"
        VARCHAR logo_url "로고 이미지 URL"
        VARCHAR introduction "브랜드 소개"
    }

    %% PRODUCT_LIKE 테이블
    PRODUCT_LIKE {
        INT id PK "좋아요 ID"
        VARCHAR user_id FK "사용자 ID"
        VARCHAR product_id FK "상품 ID"
        TIMESTAMP created_at "좋아요 생성 시점"
    }

    %% ORDER 테이블
    ORDER {
        VARCHAR order_id PK "주문 ID"
        VARCHAR user_id FK "사용자 ID"
        TIMESTAMP order_date "주문일"
        VARCHAR recipient_name "수령자 이름"
        VARCHAR delivery_address "배송 주소"
        VARCHAR recipient_contact "수령자 연락처"
        INT total_product_amount "상품 총액"
        INT discount_amount "할인 금액"
        INT final_payment_amount "최종 결제 금액"
        VARCHAR order_status "주문 상태"
        INT used_points "사용 포인트"
    }

    %% ORDER_ITEM 테이블
    ORDER_ITEM {
        INT id PK "주문 상품 ID"
        VARCHAR order_id FK "주문 ID"
        INT product_id FK "상품 ID"
        INT quantity "수량"
        INT unit_price "단가"
    }

    %% PAYMENT 테이블
    PAYMENT {
        VARCHAR payment_id PK "결제 ID"
        VARCHAR order_id FK "주문 ID"
        VARCHAR user_id FK "사용자 ID"
        INT amount "결제 금액"
        VARCHAR payment_method "결제 수단"
        VARCHAR card_info "카드 정보"
        TIMESTAMP payment_date "결제일"
        VARCHAR status "결제 상태"
        VARCHAR pg_transaction_id "PG사 거래 ID"
    }

    %% POINT 테이블
    POINT {
        INT point_id PK "포인트 ID"
        VARCHAR user_id FK "사용자 ID"
        INT amount "포인트 금액"
        TIMESTAMP issue_date "지급일"
        TIMESTAMP expiry_date "만료일"
    }

~~~

