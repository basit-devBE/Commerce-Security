package com.example.commerce.controllers;

import com.example.commerce.dtos.requests.AddOrderDTO;
import com.example.commerce.dtos.requests.UpdateOrderDTO;
import com.example.commerce.dtos.responses.ApiResponse;
import com.example.commerce.dtos.responses.OrderResponseDTO;
import com.example.commerce.interfaces.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Order Management")
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final IOrderService orderService;

    public OrderController(IOrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Create a new order", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> createOrder(
            @Valid @RequestBody AddOrderDTO request,
            HttpServletRequest httpRequest) {
        Long authenticatedUserId = (Long) httpRequest.getAttribute("authenticatedUserId");
        request.setUserId(authenticatedUserId);
        OrderResponseDTO order = orderService.createOrder(request);
        ApiResponse<OrderResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Order created successfully", order);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/admin/all")
    @Operation(security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<Page<OrderResponseDTO>>> getAllOrders(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "direction", defaultValue = "DESC") String direction
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<OrderResponseDTO> orders = orderService.getAllOrders(pageable);
        
        ApiResponse<Page<OrderResponseDTO>> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Orders fetched successfully", orders);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/user")
    @Operation(security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<Page<OrderResponseDTO>>> getOrdersByUserId(
            HttpServletRequest httpRequest,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Long authenticatedUserId = (Long) httpRequest.getAttribute("authenticatedUserId");
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<OrderResponseDTO> orders = orderService.getOrdersByUserId(authenticatedUserId, pageable);
        ApiResponse<Page<OrderResponseDTO>> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "User orders fetched successfully", orders);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    @Operation(security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getOrderById(@PathVariable Long id) {
        OrderResponseDTO order = orderService.getOrderById(id);
        ApiResponse<OrderResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Order fetched successfully", order);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/admin/update/{id}")
    @Operation(security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<OrderResponseDTO>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderDTO request) {
        OrderResponseDTO updatedOrder = orderService.updateOrderStatus(id, request);
        ApiResponse<OrderResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Order status updated successfully", updatedOrder);
        return ResponseEntity.ok(apiResponse);
    }
}
