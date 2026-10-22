package com.tranky06.commerce.product.service;

import com.tranky06.commerce.product.domain.Product;
import com.tranky06.commerce.product.dto.request.CreateProductRequest;
import com.tranky06.commerce.product.dto.response.ProductResponse;
import com.tranky06.commerce.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = Product.create(
                request.sku(),
                request.name(),
                request.description(),
                request.price(),
                request.stockQuantity()
        );

        if (productRepository.existsBySku(product.getSku())) {
            throw new IllegalArgumentException(
                    "Product with SKU " + product.getSku() + " already exists"
            );
        }

        Product savedProduct = productRepository.save(product);

        return ProductResponse.from(savedProduct);
    }
}