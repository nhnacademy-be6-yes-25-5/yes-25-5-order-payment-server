# Yes25.5 order-payment-server

Yes25.5의 주문 및 결제 시스템을 담당하는 서버입니다.

<br/>

## 🛠️ Stacks

### Environment
<img src="https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=white"/> <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white"/>

### Development
<img src="https://img.shields.io/badge/java-ff7f00?style=for-the-badge&logo=java&logoColor=white"/> <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/> <img src="https://img.shields.io/badge/JPA-6DB33F?style=for-the-badge&logo=JPA&logoColor=white"> 
<img src="https://img.shields.io/badge/Spring Security-6DB33F?style=for-the-badge&logo=Spring Security&logoColor=white">
<img src="https://img.shields.io/badge/spring cloud-6DB33F?style=for-the-badge&logo=spring&logoColor=white"/>

### Cloud
<img src="https://img.shields.io/badge/nhn cloud-blue?style=for-the-badge&logo=nhncloud&logoColor=white"/>

### Database
<img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white"/> <img src="https://img.shields.io/badge/redis-d1180b?style=for-the-badge&logo=redis&logoColor=white"/> 

### MessageQueue
<img src="https://img.shields.io/badge/RabbitMQ-ff7f00?style=for-the-badge&logo=rabbitmq&logoColor=white">


<br/>
<br/>

## 🖥️ 화면 구성

<img width="1352" alt="image" src="https://github.com/user-attachments/assets/e48f7833-71b9-4ca9-abe8-759d3f4037bc">

> 주문 페이지

<br/>

<img width="890" alt="image" src="https://github.com/user-attachments/assets/32dc746a-547c-4570-90c6-b2532a72f84d">

> 결제 수단 선택

<br/>

<img width="1363" alt="image" src="https://github.com/user-attachments/assets/db83a048-0edc-4045-b1e7-b90e2bab603b">

> 결제 화면


<br/>
<br/>

## ✅ 주요 기능

### 원하는 도서를 선택하여, 주문하는 서비스 제공
- 주문 시, Redis를 통해 가주문 저장하여 분산처리 구현
- 도서별 쿠폰 적용 가능

### 주문 금액에 맞춰 배송비 결정
- 주문 금액에 따라 적용되는 배송비 정책을 결정
- 무료 배송비까지 남은 금액을 시각적으로 표시

### 메세지 기반의 결제 서비스 제공
- TossPayments를 통한 결제 서비스 제공
- 결제 처리에 대한 순서를 보장하여, 데이터 일관성을 유지하고 트랜잭션 무결성을 확보함
- 전략 패턴을 활용한 유연하고 유지보수성이 좋은 아키텍처 구현

### 배송시작 전이라면 언제든 결제 취소 가능
- 배송시작 전, 언제든 주문서 페이지에서 결제 취소 가능
- 배송이 완료됐다면 반품이 이루어진 후, 환불 가능
- 환불은 관리자의 승인이 필요


## 🔫 트러블 슈팅

> 링크 추가 예정

- [가주문 도입기]()
- [메세지 기반 아키텍처의 필요성]()
- [If문 지옥을 벗어나자]()


<br/>
<br/>

## ⚙️ 패키지 구조

```
├── Yes255OrderPaymentServerApplication.java
├── application
│   ├── dto
│   │   ├── request
│   │   │   ├── CancelPaymentRequest.java
│   │   │   ├── ReadBookInfoResponse.java
│   │   │   ├── StockRequest.java
│   │   │   ├── UpdateCouponRequest.java
│   │   │   ├── UpdatePointMessage.java
│   │   │   ├── UpdatePointRequest.java
│   │   │   ├── UpdateRefundRequest.java
│   │   │   ├── UpdateUserCartQuantityRequest.java
│   │   │   └── enumtype
│   │   │       └── OperationType.java
│   │   └── response
│   │       ├── ReadBookResponse.java
│   │       ├── ReadPurePriceResponse.java
│   │       ├── SuccessPaymentResponse.java
│   │       └── UpdatePointResponse.java
│   └── service
│       ├── AdminOrderService.java
│       ├── MyPageOrderService.java
│       ├── OrderService.java
│       ├── PolicyService.java
│       ├── PreOrderService.java
│       ├── context
│       │   ├── OrderStatusContext.java
│       │   └── PaymentContext.java
│       ├── impl
│       │   ├── AdminOrderServiceImpl.java
│       │   ├── MyPageOrderServiceImpl.java
│       │   ├── OrderServiceImpl.java
│       │   ├── PolicyServiceImpl.java
│       │   └── PreOrderServiceImpl.java
│       ├── queue
│       │   ├── consumer
│       │   │   ├── BookStockConsumer.java
│       │   │   ├── CartConsumer.java
│       │   │   ├── CouponConsumer.java
│       │   │   ├── OrderConsumer.java
│       │   │   └── PointConsumer.java
│       │   └── producer
│       │       └── MessageProducer.java
│       ├── scheduler
│       │   └── OrderScheduler.java
│       └── strategy
│           ├── payment
│           │   ├── PaymentStrategy.java
│           │   ├── PaymentStrategyProvider.java
│           │   └── impl
│           │       ├── KakaoPayment.java
│           │       ├── NaverPayment.java
│           │       └── TossPayment.java
│           └── status
│               ├── OrderStatusStrategy.java
│               ├── OrderStatusStrategyProvider.java
│               └── impl
│                   ├── CancelStatusStrategy.java
│                   ├── RefundStatusStrategy.java
│                   └── ReturnStatusStrategy.java
├── common
│   ├── appender
│   │   └── HttpAppender.java
│   ├── config
│   │   ├── AppConfig.java
│   │   ├── EurekaConfig.java
│   │   ├── FeignClientConfig.java
│   │   ├── RabbitMQConfig.java
│   │   ├── RedissonConfig.java
│   │   ├── RetryConfig.java
│   │   ├── SchedulerConfig.java
│   │   ├── SecurityConfig.java
│   │   └── SwaggerConfig.java
│   ├── decoder
│   │   └── CustomErrorDecoder.java
│   ├── exception
│   │   ├── AccessDeniedException.java
│   │   ├── ApplicationException.java
│   │   ├── DefaultException.java
│   │   ├── EntityNotFoundException.java
│   │   ├── FeignClientException.java
│   │   ├── JwtException.java
│   │   ├── PaymentException.java
│   │   ├── TokenCookieMissingException.java
│   │   └── payload
│   │       └── ErrorStatus.java
│   ├── handler
│   │   └── GlobalRestControllerAdvice.java
│   ├── interceptor
│   │   ├── JwtAuthorizationRequestInterceptor.java
│   │   └── LoggingRequestInterceptor.java
│   ├── jwt
│   │   ├── HeaderUtils.java
│   │   ├── JwtFilter.java
│   │   ├── JwtProvider.java
│   │   ├── JwtUserDetails.java
│   │   └── annotation
│   │       └── CurrentUser.java
│   └── utils
│       └── AsyncSecurityContextUtils.java
├── infrastructure
│   └── adaptor
│       ├── AuthAdaptor.java
│       ├── BookAdaptor.java
│       ├── CouponAdaptor.java
│       ├── KeyManagerAdaptor.java
│       ├── TossAdaptor.java
│       └── UserAdaptor.java
├── persistance
│   ├── RefundStatus.java
│   ├── domain
│   │   ├── Delivery.java
│   │   ├── Order.java
│   │   ├── OrderBook.java
│   │   ├── OrderCoupon.java
│   │   ├── OrderStatus.java
│   │   ├── Payment.java
│   │   ├── PreOrder.java
│   │   ├── Refund.java
│   │   ├── ShippingPolicy.java
│   │   ├── Takeout.java
│   │   └── enumtype
│   │       ├── CancelStatus.java
│   │       ├── OrderStatusType.java
│   │       ├── PaymentProvider.java
│   │       └── TakeoutType.java
│   └── repository
│       ├── DeliveryRepository.java
│       ├── OrderBookRepository.java
│       ├── OrderCouponRepository.java
│       ├── OrderRepository.java
│       ├── OrderStatusRepository.java
│       ├── PaymentRepository.java
│       ├── RefundRepository.java
│       ├── RefundStatusRepository.java
│       ├── ShippingPolicyRepository.java
│       └── TakeoutRepository.java
└── presentation
    ├── controller
    │   ├── AdminOrderController.java
    │   ├── MyPageController.java
    │   ├── OrderController.java
    │   ├── PaymentController.java
    │   └── PolicyController.java
    └── dto
        ├── request
        │   ├── CancelOrderRequest.java
        │   ├── CreateOrderRequest.java
        │   ├── CreatePaymentRequest.java
        │   ├── ReadMyOrderHistoryRequest.java
        │   ├── UpdateOrderRequest.java
        │   └── UpdateOrderStatusRequest.java
        └── response
            ├── CancelOrderResponse.java
            ├── CreateOrderResponse.java
            ├── CreatePaymentResponse.java
            ├── JwtAuthResponse.java
            ├── KeyManagerResponse.java
            ├── ReadAllOrderResponse.java
            ├── ReadAllUserOrderCancelStatusResponse.java
            ├── ReadMyOrderHistoryResponse.java
            ├── ReadOrderDeliveryResponse.java
            ├── ReadOrderDetailResponse.java
            ├── ReadOrderStatusResponse.java
            ├── ReadPaymentOrderResponse.java
            ├── ReadShippingPolicyResponse.java
            ├── ReadTakeoutResponse.java
            ├── ReadUserOrderAllResponse.java
            ├── ReadUserOrderResponse.java
            └── UpdateOrderResponse.java
```




