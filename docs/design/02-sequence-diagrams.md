# 상품 / 브랜드 조회 시퀀스 다이어그램

**상품 목록 조회 시퀀스 다이어그램**

~~~mermaid
sequenceDiagram
actor U as User
participant PC as ProductController
participant PS as ProductService
participant PR as ProductRepository

U ->> PC: 상품 목록 조회 요청
	Note over U,PC: 쿼리 파라미터로 "검색어"와 "정렬 기준" 전달
PC ->> PR: 상품 목록 조회 요청
PR ->> PC: 조건에 맞는 상품 목록 응답
	Note over PR,PC: 브랜드명, 상품명, 가격

~~~

**상품 목록 조회 시퀀스 다이어그램**

~~~mermaid
sequenceDiagram
actor U as User
participant PC as ProductController
participant PS as ProductService
participant PR as ProductRepository

U ->> PC: 상품 상세 조회 요청
	Note over U,PC: 상품 Id 전달
PC ->> PR: 상품 상세 조회 요청
PR ->> PC: 상품 상세 정보 응답
	Note over PR,PC: 브랜드명, 상품명, 가격, 상품 이미지, 상세 내용, 품절 여부

~~~

**브랜드 조회 시퀀스 다이어그램**

~~~mermaid
sequenceDiagram
actor U as User
participant BC as BrandController
participant BS as BrandService
participant BR as BrandRepository

U ->> BC: 브랜드 정보 조회 요청
	Note over U,BC: 브랜드 Id 전달
BC ->> BR: 브랜드 정보 조회 요청
BR ->> BC: 브랜드 정보 응답
	Note over BR,BC: 브랜드명, 로고, 브랜드 소개

~~~




# 상품 좋아요 시퀀스 다이어그램



**좋아요 시퀀스 다이어그램**

~~~mermaid
sequenceDiagram
actor U as User
participant LC as LikeController
participant US as UserService
participant LS as LikeService
participant PR as ProductRepository
participant LR as LikeRepository

U ->> LC: 상품 좋아요 요청
	Note over U,LC: 상품 ID, X-USER-ID 전달
LC ->> US: 사용자 인증 확인
	Note over LC,US: X-USER-ID 전달
alt 인증 실패 (사용자 미존재, 헤더 미존재)
  US ->> U: 401 Unauthorized
else 인증 성공
  US -->> LC: 사용자 정보 반환
end
LC ->> LS: 상품 좋아요 요청
LS ->> PR: 상품 정보 조회
  Note over LS,PR: 상품 ID 전달
alt 상품 미존재
  PR ->> U: 404 Not Found
else 상품 존재
  PR -->> LS: 상품 정보 응답 (좋아요 수)
end
LS ->> LR: 상품 좋아요 정보 조회
  Note over LS,LR: 상품 ID 전달
alt 좋아요 미존재
  LS ->> LR: 사용자 정보와 좋아요 저장
  LS ->> PR: 좋아요 수 증가
else 좋아요 존재
    LS->> U: 200 OK (멱등 응답)
end
~~~



**좋아요 취소 시퀀스 다이어그램**

~~~mermaid
sequenceDiagram
actor U as User
participant LC as LikeController
participant US as UserService
participant LS as LikeService
participant PR as ProductRepository
participant LR as LikeRepository

U ->> LC: 상품 좋아요 취소 요청
	Note over U,LC: 상품 ID, X-USER-ID 전달
LC ->> US: 사용자 인증 확인
	Note over LC,US: X-USER-ID 전달
alt 인증 실패 (사용자 미존재, 헤더 미존재)
  US ->> U: 401 Unauthorized
else 인증 성공
  US -->> LC: 사용자 정보 반환
end
LC ->> LS: 상품 좋아요 취소 요청
LS ->> PR: 상품 정보 조회
  Note over LS,PR: 상품 ID 전달
alt 상품 미존재
  PR ->> U: 404 Not Found
else 상품 존재
  PR -->> LS: 상품 정보 응답 (좋아요 수)
end
LS ->> LR: 상품 좋아요 정보 조회
  Note over LS,LR: 상품 ID 전달
alt 좋아요 미존재
  LR ->> LS: 404 Not Found
else 좋아요 존재
  LS ->> LR: 좋아요 취소
  LS ->> PR: 좋아요 수 감소
end
~~~



**내가 좋아요한 상품 목록 조회 시퀀스 다이어그램**

~~~mermaid
sequenceDiagram
actor U as User
participant LC as LikeController
participant US as UserService
participant LS as LikeService
participant LR as LikeRepository

U ->> LC: 상품 좋아요 취소 요청
	Note over U,LC: 상품 ID, X-USER-ID 전달
LC ->> US: 사용자 인증 확인
	Note over LC,US: X-USER-ID 전달
alt 인증 실패 (사용자 미존재, 헤더 미존재)
  US ->> U: 401 Unauthorized
else 인증 성공
  US -->> LC: 사용자 정보 반환
end
LC ->> LR: 내가 좋아요한 상품 목록 조회 요청
LR ->> LC: 내가 좋아요한 상품 목록 응답
	Note over LR,LC: 상품명, 브랜드명, 가격 정보, 총 좋아요 수
~~~





# 주문 / 결제 시퀀스 다이어그램

~~~mermaid
sequenceDiagram
		actor U as User
    participant OS as OrderService
    participant PDS as ProductService
    participant PTS as PointService
    participant PMS as PaymentService
    participant PG as PGSystem
    participant OR as OrderRepository

    %% 사용자 입력
    U ->> OS: 주문 생성 요청
      Note over U,OS: (주문자 이름, 주소, 연락처, 결제 수단, 포인트)

    %% 재고 확인 및 차감
    OS ->> PDS: 상품 재고 확인
    alt 재고 없음
    OS ->> U: 404 Not Found
		else 재고 충분
   	OS ->> PDS: 재고 차감
		end

    %% 포인트 확인 및 차감
    OS ->> PTS: 포인트 사용 가능 여부 확인
    alt 포인트 없음
    OS ->> U: 404 Not Found
		else 포인트 사용 가능
   	OS ->> PTS: 포인트 차감
		end

    %% 결제 요청
    OS ->> PMS: 결제 요청
    PMS ->> PG: PG사 결제 처리
   
    alt 결제 실패
    PG ->> U: 결제 실패 Error
		else 결제 성공
   	PMS -->> OS: 결제 성공
		end

    %% 주문 저장 및 완료 처리
    OS ->> OR: 주문 저장 (결제 완료 상태)
    OS -->> U: 주문 완료 정보 응답 (주문 번호, 일자, 상품, 결제금액 등)

~~~






