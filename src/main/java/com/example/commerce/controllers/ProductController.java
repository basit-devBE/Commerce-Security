package com.example.commerce.controllers;


import com.example.commerce.dtos.requests.AddProductDTO;
import com.example.commerce.dtos.requests.UpdateProductDTO;
import com.example.commerce.dtos.responses.ApiResponse;
import com.example.commerce.dtos.responses.ProductResponseDTO;
import com.example.commerce.interfaces.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Product Management")
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final IProductService productService;

    public ProductController(IProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Add a new product", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PostMapping("/admin/add")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> addProduct(@Valid @RequestBody AddProductDTO request){
        ProductResponseDTO product = productService.addProduct(request);
        ApiResponse<ProductResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Product added successfully", product);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get all products")
    @GetMapping("/public/all")
    public ResponseEntity<ApiResponse<Page<ProductResponseDTO>>> getAllProducts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "direction", defaultValue = "ASC") String direction
    ){
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductResponseDTO> pagedResponse = categoryId != null 
            ? productService.getProductsByCategory(categoryId, pageable)
            : productService.getAllProducts(pageable);
        
        ApiResponse<Page<ProductResponseDTO>> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Products fetched successfully", pagedResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get product by ID", security = @SecurityRequirement(name = "Bearer Authentication"))
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> getProductById(@PathVariable Long id){
        ProductResponseDTO product = productService.getProductById(id);
        ApiResponse<ProductResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Product fetched successfully", product);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Update a product", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PutMapping("/admin/update/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updateProduct(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateProductDTO request){
        ProductResponseDTO updatedProduct = productService.updateProduct(id, request);
        ApiResponse<ProductResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Product updated successfully", updatedProduct);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Delete a product", security = @SecurityRequirement(name = "Bearer Authentication"))
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
        ApiResponse<Void> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Product deleted successfully", null);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Search product by name")
    @GetMapping("/public/search/{name} ")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> getProductByName(@PathVariable String name){
        ProductResponseDTO product = productService.getProductByName(name);
        ApiResponse<ProductResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Product fetched successfully", product);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/public/price-range")
    public  ResponseEntity<ApiResponse<Page<ProductResponseDTO>>> findProductByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ){
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<ProductResponseDTO> pagedResponse = productService.getProductsByPriceBetween(minPrice, maxPrice, pageable);
        ApiResponse<Page<ProductResponseDTO>> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Products fetched successfully", pagedResponse);
        return ResponseEntity.ok(apiResponse);
    }
}
