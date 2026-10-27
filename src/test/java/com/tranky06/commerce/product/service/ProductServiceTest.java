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

import com.tranky06.commerce.product.dto.request.CreateProductRequest;
import com.tranky06.commerce.product.exception.DuplicateSkuException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;

import com.tranky06.commerce.product.dto.request.UpdateProductRequest;

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

        Page<ProductResponse> result =
                productService.getProducts(pageable);

        assertEquals(1, result.getNumber());
        assertEquals(2, result.getSize());
        assertEquals(2, result.getNumberOfElements());
        assertEquals(5L, result.getTotalElements());
        assertEquals(3, result.getTotalPages());

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

    @Test
    void createProduct_shouldThrowWhenSkuAlreadyExists() {
        String normalizedSku = "PRODUCT-TEST-005";

        CreateProductRequest request = new CreateProductRequest(
                " product-test-005 ",
                "Laptop Test",
                "San pham dung cho unit test",
                new BigDecimal("15000000.00"),
                10
        );

        when(productRepository.existsBySku(normalizedSku))
                .thenReturn(true);

        assertThrows(
                DuplicateSkuException.class,
                () -> productService.createProduct(request)
        );

        verify(productRepository).existsBySku(normalizedSku);

        verify(productRepository, never())
                .save(any(Product.class));
    }

    @Test
    void createProduct_shouldSaveAndReturnProductWhenSkuDoesNotExist() {
        String normalizedSku = "PRODUCT-TEST-006";
        BigDecimal price = new BigDecimal("15000000.00");

        CreateProductRequest request = new CreateProductRequest(
                " product-test-006 ",
                " Laptop Test ",
                " San pham dung cho unit test ",
                price,
                10
        );

        when(productRepository.existsBySku(normalizedSku))
                .thenReturn(false);

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> {
                    Product productToSave = invocation.getArgument(0);

                    ReflectionTestUtils.setField(
                            productToSave,
                            "id",
                            6L
                    );

                    return productToSave;
                });

        ProductResponse result = productService.createProduct(request);

        assertEquals(Long.valueOf(6L), result.id());
        assertEquals(normalizedSku, result.sku());
        assertEquals("Laptop Test", result.name());
        assertEquals(
                "San pham dung cho unit test",
                result.description()
        );
        assertEquals(price, result.price());
        assertEquals(Integer.valueOf(10), result.stockQuantity());
        assertTrue(result.active());

        verify(productRepository).existsBySku(normalizedSku);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_shouldThrowWhenProductDoesNotExist() {
        Long productId = 999999L;

        UpdateProductRequest request = new UpdateProductRequest(
                "Laptop Updated",
                "Updated description",
                new BigDecimal("14990000.00")
        );

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.updateProduct(productId, request)
        );

        verify(productRepository).findById(productId);

        verify(productRepository, never())
                .saveAndFlush(any(Product.class));
    }

    @Test
    void updateProduct_shouldUpdateDetailsAndKeepSkuAndStock() {
        Long productId = 1L;
        BigDecimal updatedPrice = new BigDecimal("14990000.00");

        Product product = Product.create(
                "PRODUCT-TEST-008",
                "Old Laptop",
                "Old description",
                new BigDecimal("15990000.00"),
                10
        );

        ReflectionTestUtils.setField(product, "id", productId);

        UpdateProductRequest request = new UpdateProductRequest(
                " Laptop Updated ",
                " Updated description ",
                updatedPrice
        );

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(productRepository.saveAndFlush(product))
                .thenReturn(product);

        ProductResponse result =
                productService.updateProduct(productId, request);

        assertEquals(productId, result.id());
        assertEquals("Laptop Updated", result.name());
        assertEquals("Updated description", result.description());
        assertEquals(updatedPrice, result.price());
        assertEquals("PRODUCT-TEST-008", result.sku());
        assertEquals(Integer.valueOf(10), result.stockQuantity());
        assertTrue(result.active());

        verify(productRepository).findById(productId);
        verify(productRepository).saveAndFlush(product);
    }
}