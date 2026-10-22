package com.tranky06.commerce.common.exception;

import com.tranky06.commerce.product.exception.DuplicateSkuException;
import com.tranky06.commerce.product.exception.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateSkuException.class)
    public ProblemDetail handleDuplicateSku(
            DuplicateSkuException exception
    ) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.CONFLICT,
                        exception.getMessage()
                );

        problemDetail.setTitle("Duplicate product SKU");

        return problemDetail;
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail handleProductNotFound(
            ProductNotFoundException exception
    ) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.NOT_FOUND,
                        exception.getMessage()
                );

        problemDetail.setTitle("Product not found");

        return problemDetail;
    }
}