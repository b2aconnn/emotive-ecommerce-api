package com.loopers.domain.order;

import com.loopers.application.order.dto.OrderCreateCommand;
import com.loopers.domain.order.dto.OrderItemCreateCommand;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductStockService;
import com.loopers.domain.product.dto.command.ProductStocksDeductCommand;
import com.loopers.domain.product.dto.result.ProductQuantityResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderService {

    private final OrderItemService orderItemService;

    private final OrderCalculator orderCalculator;

    private final ProductService productService;

    private final ProductStockService productStockService;

    private final PointRepository pointRepository;

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;


    public Order createOrder(OrderCreateCommand createCommand) {
        Order saveOrder = orderRepository.save(createCommand.toEntity());

        List<ProductQuantityResult> productItems =
                productService.getProductWithStocks(createCommand.toProductQuantityCommands());

        List<OrderItem> orderItems = getOrderItems(productItems, saveOrder);
        saveOrder.addOrderItems(orderItems);
        orderItemRepository.saveAll(orderItems);

        calculateTotalAmount(orderItems, saveOrder);

        userPoint(createCommand);

        deductStocks(productItems);

        saveOrder.complete();

        return saveOrder;
    }

    private void deductStocks(List<ProductQuantityResult> productItems) {
        List<ProductStocksDeductCommand> stocksDeductCommand = productItems.stream()
                .map(ProductQuantityResult::toProductStocksDeductCommands)
                .toList();
        productStockService.deductStocks(stocksDeductCommand);
    }

    private void userPoint(OrderCreateCommand createCommand) {
        Point point = pointRepository.findByUserId(createCommand.userId())
                .orElseThrow(() -> new IllegalStateException("포인트가 부족합니다."));
        point.use(createCommand.availablePoints());
    }

    private void calculateTotalAmount(List<OrderItem> orderItems, Order order) {
        Long totalAmount = orderCalculator.calculateTotalAmount(orderItems);
        order.setTotalAmount(totalAmount);
    }

    private List<OrderItem> getOrderItems(List<ProductQuantityResult> productItems, Order order) {
        List<OrderItemCreateCommand> orderItemCreateCommand = productItems.stream()
                .map(ProductQuantityResult::toOrderItemCommands)
                .toList();
        List<OrderItem> orderItems = orderItemService.createOrderItems(
                order, orderItemCreateCommand);

        return orderItems;
    }
}
