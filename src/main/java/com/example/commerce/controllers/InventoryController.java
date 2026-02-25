package com.example.commerce.controllers;

import com.example.commerce.dtos.requests.AddInventoryDTO;
import com.example.commerce.dtos.requests.UpdateInventoryDTO;
import com.example.commerce.dtos.responses.ApiResponse;
import com.example.commerce.dtos.responses.InventoryResponseDTO;
import com.example.commerce.interfaces.IInventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Inventory Management")
@RestController
@RequestMapping("/api/inventory")

public class InventoryController {
    private final IInventoryService inventoryService;

    public InventoryController(IInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Operation(summary = "Add inventory", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PostMapping("/admin/add")
    public ResponseEntity<ApiResponse<InventoryResponseDTO>> addInventory(@Valid @RequestBody AddInventoryDTO request) {
        InventoryResponseDTO inventory = inventoryService.addInventory(request);
        ApiResponse<InventoryResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Inventory added successfully", inventory);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/admin/all")
    @Operation(security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<Page<InventoryResponseDTO>>> getAllInventories(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<InventoryResponseDTO> inventories = inventoryService.getAllInventories(pageable);
        ApiResponse<Page<InventoryResponseDTO>> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Inventories fetched successfully", inventories);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    @Operation(security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<InventoryResponseDTO>> getInventoryById(@PathVariable Long id) {
        InventoryResponseDTO inventory = inventoryService.getInventoryById(id);
        ApiResponse<InventoryResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Inventory fetched successfully", inventory);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/product/{productId}")
    @Operation(security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<InventoryResponseDTO>> getInventoryByProductId(@PathVariable Long productId) {
        InventoryResponseDTO inventory = inventoryService.getInventoryByProductId(productId);
        ApiResponse<InventoryResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Inventory fetched successfully", inventory);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/admin/update/{id}")
    @Operation(security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<InventoryResponseDTO>> updateInventory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInventoryDTO request) {
        InventoryResponseDTO updatedInventory = inventoryService.updateInventory(id, request);
        ApiResponse<InventoryResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Inventory updated successfully", updatedInventory);
        return ResponseEntity.ok(apiResponse);
    }

    @PatchMapping("/admin/adjust/{id}")
    @Operation(security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<InventoryResponseDTO>> adjustInventoryQuantity(
            @PathVariable Long id,
            @RequestParam Integer quantityChange) {
        InventoryResponseDTO adjustedInventory = inventoryService.adjustInventoryQuantity(id, quantityChange);
        ApiResponse<InventoryResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Inventory quantity adjusted successfully", adjustedInventory);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/admin/{id}")
    @Operation(security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<ApiResponse<Void>> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        ApiResponse<Void> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Inventory deleted successfully", null);
        return ResponseEntity.ok(apiResponse);
    }
}
