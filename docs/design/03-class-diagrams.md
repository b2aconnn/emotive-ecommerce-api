# 클래스 다이어그램

~~~mermaid
classDiagram
  class User {
      - id: Long
      - userId: String
      - name: String
      - email: String
      - birthDate: LocalDate
      - gender: GenderType
    }

    class Product {
      - id: Long
      - name: Integer
      - mainImageUrl: String
      - description: String
      - price: Integer
      - stockQuentity: Ingter
      - brand: Brand
      - likeCount: Integer
    }

    class Brand {
      - id: Long
      - name: String
      - logoUrl: String
      - introduction: String
    }

    class ProductLike {
      - id: Long
      - user: User
      - product: Product

      + increase()
      + decrease()
    }

    class OrderItem {
        - product: Product
        - quantity: Integer
        - unitPrice: Integer
    }

    class Order {
        - orderId: String
        - userId: String
        - orderDate: LocalDateTime
        - recipientName: String
        - deliveryAddress: String
        - recipientContact: String
        - items: List<OrderItem>
        - totalProductAmount: Integer
        - discountAmount: Integer
        - finalPaymentAmount: Integer
        - orderStatus: OrderStatus
        - usedPoints: Integer
        
        + createOrder()
        + calculateTotalAmount()
        + updateOrderStatus(newStatus)
    }

    class Payment {
        - paymentId: String
        - orderId: String
        - userId: String
        - amount: Integer
        - paymentMethod: PaymentMethodType
        - cardInfo: String 
        - paymentDate: Date
        - status: PaymentStatus
        - pgTransactionId: String
        
        + processPayment(orderInfo, paymentInfo)
        + cancelPayment(paymentId)
    }

    class Point {
        - pointId: Long
        - userId: String
        + amount: Integer
        + issueDate: LocalDateTime
        + expiryDate: LocalDateTime
        
        + checkBalance(userId)
        + deductPoints(userId, amount)
        + addPoints(userId, amount)
        + isValid()
    }
    
    
%% 연관 관계
  User "1" --> "0..*" ProductLike
  Product "1" --> "0..*" ProductLike

  User "1" --> "0..*" Order
  Order "1" --> "1..*" OrderItem
  OrderItem "1" --> "1" Product

  Order "1" --> "1" Payment
  Payment "1" --> "1" Order

  User "1" --> "0..*" Point
  Product "1" --> "1" Brand
~~~

