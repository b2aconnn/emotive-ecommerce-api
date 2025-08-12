package com.loopers.domain.product;

import com.loopers.domain.product.dto.command.ProductStocksDeductCommand;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ProductStockService {
    private final ProductStockRepository productStockRepository;

    public void deductStocks(List<ProductStocksDeductCommand> stocksDeductCommand) {
        List<Long> productIds = stocksDeductCommand.stream()
                .map(e -> e.product().getId()).toList();

        Map<Long, ProductStock> productStockMap = productStockRepository.findStocks(productIds)
                .stream()
                .collect(Collectors.toMap(e -> e.getProduct().getId(), Function.identity()));


        stocksDeductCommand.forEach(command -> {
            deductStock(command, productStockMap);
        });
    }

    private void deductStock(ProductStocksDeductCommand command, Map<Long, ProductStock> productStockMap) {
        ProductStock productStock = productStockMap.get(command.product().getId());
        if (productStock == null) {
            throw new EntityNotFoundException("상품 재고가 없습니다.");
        }

        productStock.deduct(command.quantity());
    }
}
