package com.tranky06.commerce.product.service;

import org.springframework.data.domain.PageImpl;

import java.util.List;

import com.tranky06.commerce.product.domain.Product;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import com.tranky06.commerce.product.exception.ProductNotFoundException;
import com.tranky06.commerce.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tranky06.commerce.product.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductServiceTest {

    private final ProductRepository productRepository =
            mock(ProductRepository.class);

    private final ProductService productService =
            new ProductService(productRepository);

    @Test
    void getProductById_shouldThrowWhenProductDoesNotExist() {
        Long productId = 999L;

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProductById(productId)
        );

        verify(productRepository).findById(productId);
    }

    @Test
    void getProducts_shouldReturnEmptyPageWhenNoProductsExist() {
        Pageable pageable = PageRequest.of(0, 10);

        when(productRepository.findAll(pageable))
                .thenReturn(Page.empty(pageable));

        Page<ProductResponse> result =
                productService.getProducts(pageable);

        assertTrue(result.isEmpty());
        assertEquals(0L, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        assertEquals(0, result.getNumber());
        assertEquals(10, result.getSize());

        verify(productRepository).findAll(pageable);
    }

    @Test
    void getProductById_shouldReturnProductWhenProductExists() {
        // 1. Chuẩn bị sản phẩm mẫu
        Long productId = 1L;
        BigDecimal price = new BigDecimal("15000000.00");

        Product product = Product.create(
                "PRODUCT-TEST-001",
                "Laptop Test",
                "San pham dung cho unit test",
                price,
                10
        );

        ReflectionTestUtils.setField(product, "id", productId);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        ProductResponse result =
                productService.getProductById(productId);

        assertEquals(productId, result.id());
        assertEquals("PRODUCT-TEST-001", result.sku());
        assertEquals("Laptop Test", result.name());
        assertEquals(
                "San pham dung cho unit test",
                result.description()
        );
        assertEquals(price, result.price());
        assertEquals(Integer.valueOf(10), result.stockQuantity());
        assertTrue(result.active());

        verify(productRepository).findById(productId);
    }

    @Test
    void getProducts_shouldMapProductsAndPreservePagination() {
        // 1. Chuẩn bị dữ liệu mẫu của trang thứ hai
        Pageable pageable = PageRequest.of(1, 2);

        Product firstProduct = Product.create(
                "PRODUCT-TEST-003",
                "Mouse Test",
                "Chuot dung cho unit test",
                new BigDecimal("250000.00"),
                20
        );

        Product secondProduct = Product.create(
                "PRODUCT-TEST-004",
                "Keyboard Test",
                "Ban phim dung cho unit test",
                new BigDecimal("950000.00"),
                15
        );

        ReflectionTestUtils.setField(firstProduct, "id", 3L);
        ReflectionTestUtils.setField(secondProduct, "id", 4L);

        Page<Product> productPage = new PageImpl<>(
                List.of(firstProduct, secondProduct),
                pageable,
                5L
        );

        when(productRepository.findAll(pageable))
                .thenReturn(productPage);

        // 2. Gọi service
        Page<ProductResponse> result =
                productService.getProducts(pageable);

        // 3. Kiểm tra thông tin phân trang
        assertEquals(1, result.getNumber());
        assertEquals(2, result.getSize());
        assertEquals(2, result.getNumberOfElements());
        assertEquals(5L, result.getTotalElements());
        assertEquals(3, result.getTotalPages());

        // 4. Kiểm tra sản phẩm và thứ tự trả về
        assertEquals(
                Long.valueOf(3L),
                result.getContent().get(0).id()
        );
        assertEquals(
                "PRODUCT-TEST-003",
                result.getContent().get(0).sku()
        );

        assertEquals(
                Long.valueOf(4L),
                result.getContent().get(1).id()
        );
        assertEquals(
                "PRODUCT-TEST-004",
                result.getContent().get(1).sku()
        );

        verify(productRepository).findAll(pageable);
    }
}