package com.example.commerce.controllers;


import com.example.commerce.dtos.requests.LoginDTO;
import com.example.commerce.dtos.requests.UpdateUserDTO;
import com.example.commerce.dtos.requests.UserRegistrationDTO;
import com.example.commerce.dtos.responses.*;
import com.example.commerce.interfaces.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "User Management")
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final IUserService userService;
    
    @Value("${cookie.secure}")
    private boolean cookieSecure;
    
    @Value("${cookie.sameSite}")
    private String cookieSameSite;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Register a new user")
    @PostMapping("/public/register")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> registerUser(@Valid @RequestBody UserRegistrationDTO request) {
        LoginResponseDTO userResponseDTO = userService.addUser(request);
        ApiResponse<LoginResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "User registered successfully", userResponseDTO);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "User login")
    @PostMapping("/public/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> loginUser(@Valid @RequestBody LoginDTO request, HttpServletResponse response) {
        AuthResponseDTO authResponse = userService.loginUser(request);
                ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/api/users/public/refresh") // Restrict cookie to refresh endpoint
                .maxAge(7 * 24 * 60 * 60)
                .sameSite(cookieSameSite)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        ApiResponse<LoginResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "User logged in successfully", authResponse.getUser());
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/public/refresh")
    public ResponseEntity<ApiResponse<String>> refreshToken(
            @CookieValue(name="refreshToken", required = true) String refreshToken,
            HttpServletResponse response) {
        RefreshTokenResponseDTO tokenResponse = userService.validateAndReturnTokens(refreshToken);
        
        // Set new refresh token in cookie (token rotation for security)
        ResponseCookie newRefreshTokenCookie = ResponseCookie.from("refreshToken", tokenResponse.getRefreshToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite(cookieSameSite)
                .build();
        
        response.addHeader(HttpHeaders.SET_COOKIE, newRefreshTokenCookie.toString());
        
        ApiResponse<String> apiResponse = new ApiResponse<>(
            HttpStatus.OK.value(), 
            "Access token refreshed successfully", 
            tokenResponse.getAccessToken()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Logout user")
    @PostMapping("/public/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        // Clear refresh token cookie
        ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0) // Expire immediately
                .sameSite(cookieSameSite)
                .build();
        
        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());
        
        ApiResponse<Void> apiResponse = new ApiResponse<>(
            HttpStatus.OK.value(), 
            "Logged out successfully", 
            null
        );
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get authenticated user's profile")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<userSummaryDTO>> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("authenticatedUserId");
        userSummaryDTO user = userService.findUserById(userId);
        ApiResponse<userSummaryDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "User profile fetched successfully", user);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Update authenticated user's profile")
    @PutMapping("/updateProfile")
    public ResponseEntity<ApiResponse<userSummaryDTO>> updateProfile(HttpServletRequest request, @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        Long userId = (Long) request.getAttribute("authenticatedUserId");
        userSummaryDTO updatedUser = userService.updateUser(userId, updateUserDTO);
        ApiResponse<userSummaryDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "User profile updated successfully", updatedUser);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get all users")
    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<Page<userSummaryDTO>>> getAllUsers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<userSummaryDTO> usersPage = userService.getAllUsers(pageable);
        ApiResponse<Page<userSummaryDTO>> apiResponse =
                new ApiResponse<>(HttpStatus.OK.value(), "Users fetched successfully", usersPage);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<userSummaryDTO>> getUserById(@PathVariable Long id) {
        userSummaryDTO user = userService.findUserById(id);
        ApiResponse<userSummaryDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "User fetched successfully", user);

        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Update user details")
    @PutMapping("/admin/update/{id}")
    public ResponseEntity<ApiResponse<userSummaryDTO>> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserDTO request) {
        userSummaryDTO updatedUser = userService.updateUser(id, request);
        ApiResponse<userSummaryDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "User updated successfully", updatedUser);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Delete a user")
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        ApiResponse<Void> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "User deleted successfully", null);
        return ResponseEntity.ok(apiResponse);
    }
}