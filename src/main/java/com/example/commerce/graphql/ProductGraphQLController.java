package com.example.commerce.graphql;

import com.example.commerce.dtos.requests.AddProductDTO;
import com.example.commerce.dtos.responses.GraphQLPagedResponse;
import com.example.commerce.dtos.responses.ProductResponseDTO;
import com.example.commerce.graphql.input.PaginationInput;
import com.example.commerce.graphql.input.ProductInput.AddProductInput;
import com.example.commerce.graphql.utils.GraphQLResponseMapper;
import com.example.commerce.mappers.ProductMapper;
import com.example.commerce.services.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ProductGraphQLController {
    private final ProductService productService;
    private final GraphQLResponseMapper responseMapper;
    private final ProductMapper productMapper;

    public ProductGraphQLController(ProductService productService, GraphQLResponseMapper responseMapper, ProductMapper productMapper) {
        this.productService = productService;
        this.responseMapper = responseMapper;
        this.productMapper = productMapper;
    }


    @QueryMapping
    public List<ProductResponseDTO> allProducts() {
        return productService.getAllProductsList();
    }

    @QueryMapping
    public ProductResponseDTO productById(@Argument Long id) {
        return productService.getProductById(id);
    }

    @QueryMapping
    public GraphQLPagedResponse<ProductResponseDTO> productsPaginated(
            @Argument PaginationInput pagination,
            @Argument Long categoryId,
            @Argument String search) {
        
        // Handle null pagination with defaults
        if (pagination == null) {
            pagination = new PaginationInput(0, 10, "id", "ASC");
        }
        
        int page = pagination.getPage();
        int size = pagination.getSize();
        String sortBy = pagination.getSortBy();
        String sortDir = pagination.getSortDirection();
        
        Sort sort = sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductResponseDTO> productsPage;
        if (search != null && !search.isBlank()) {
            productsPage = productService.searchProducts(search, pageable);
        } else if (categoryId != null) {
            productsPage = productService.getProductsByCategory(categoryId, pageable);
        } else {
            productsPage = productService.getAllProducts(pageable);
        }
        
        return responseMapper.toGraphQLPagedResponse(productsPage);
    }


    @MutationMapping
    public ProductResponseDTO addProduct(@Argument AddProductInput input) {
        AddProductDTO dto = productMapper.toDTO(input);
        return productService.addProduct(dto);
    }

    @MutationMapping
    public boolean deleteProduct(@Argument Long id) {
        productService.deleteProduct(id);
        return true;
    }
}
