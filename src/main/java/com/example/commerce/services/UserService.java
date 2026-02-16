package com.example.commerce.services;

import com.example.commerce.dtos.requests.LoginDTO;
import com.example.commerce.dtos.requests.UpdateUserDTO;
import com.example.commerce.dtos.requests.UserRegistrationDTO;
import com.example.commerce.dtos.responses.AuthResponseDTO;
import com.example.commerce.dtos.responses.LoginResponseDTO;
import com.example.commerce.dtos.responses.RefreshTokenResponseDTO;
import com.example.commerce.dtos.responses.userSummaryDTO;
import com.example.commerce.entities.UserEntity;
import com.example.commerce.errorhandlers.ResourceAlreadyExists;
import com.example.commerce.errorhandlers.ResourceNotFoundException;
import com.example.commerce.errorhandlers.UnauthorizedException;
import com.example.commerce.interfaces.IUserService;
import com.example.commerce.mappers.UserMapper;
import com.example.commerce.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Caching(put = {
        @CachePut(value = "userById", key = "#result.id"),
        @CachePut(value = "userByEmail", key = "#result.email")
    })
    public LoginResponseDTO addUser(UserRegistrationDTO userDTO){

        Optional<UserEntity> existingUser = userRepository.findByEmail(userDTO.getEmail());
        if(existingUser.isPresent()){
            throw new ResourceAlreadyExists("Email already exists: " + userDTO.getEmail());
        } else {
            UserEntity userEntity = userMapper.toEntity(userDTO);
            String hashedPassword = passwordEncoder.encode(userDTO.getPassword());
            userEntity.setPassword(hashedPassword);

            UserEntity savedUser = userRepository.save(userEntity);
            return userMapper.toResponseDTO(savedUser);
        }
    }

    public AuthResponseDTO loginUser(LoginDTO loginDTO){
        log.info("Attempting login for email: {}", loginDTO.getEmail());
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getEmail(),
                            loginDTO.getPassword()
                    )
            );
            if(authentication.isAuthenticated()){
                UserEntity userEntity = (UserEntity) authentication.getPrincipal();
                //TODO: surround with an if to check if user is active or not
                assert userEntity != null;
                String accesstoken = jwtService.generateAccessToken(userEntity);
                String refreshtoken = jwtService.generateRefreshToken(userEntity);
                LoginResponseDTO loginResponseDTO = userMapper.toResponseDTO(userEntity);
                loginResponseDTO.setToken(accesstoken);
                log.info("Login successful for email: {}", loginDTO.getEmail());
                return new AuthResponseDTO(loginResponseDTO, refreshtoken);
            } else {
                throw new ResourceNotFoundException("Invalid email or password");
            }
        }catch (Exception e){
            log.error("Login failed for email: {}. Reason: {}", loginDTO.getEmail(), e.getMessage());
            throw new ResourceNotFoundException("Invalid email or password");
        }
    }


    @Cacheable(value = "userByEmail", key = "#email")
    public userSummaryDTO findUserByEmail(String email){
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if(userOpt.isPresent()){
            return userMapper.toSummaryDTO(userOpt.get());
        } else {
            throw new ResourceNotFoundException("User not found with email: " + email);
        }
    }

    @Cacheable(value = "userById", key = "#id")
    public userSummaryDTO findUserById(Long id){
        Optional<UserEntity> userOpt = userRepository.findById(id);
        if(userOpt.isPresent()){
            return userMapper.toSummaryDTO(userOpt.get());
        } else {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
    }

    @CachePut(value = "userById", key = "#id")
    @CacheEvict(value = "userByEmail", allEntries = true)
    public userSummaryDTO updateUser(Long id, @Valid UpdateUserDTO userDTO){
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        if(userDTO.getFirstName() != null && !userDTO.getFirstName().isBlank()){
            userEntity.setFirstName(userDTO.getFirstName());
        }
        if(userDTO.getLastName() != null && !userDTO.getLastName().isBlank()){
            userEntity.setLastName(userDTO.getLastName());
        }
        if(userDTO.getEmail() != null && !userDTO.getEmail().isBlank()) {
            userEntity.setEmail(userDTO.getEmail());
        }
        UserEntity updatedUser = userRepository.save(userEntity);
        return userMapper.toSummaryDTO(updatedUser);
    }

    public Page<userSummaryDTO> getAllUsers(Pageable pageable){
      return userRepository.findAll(pageable).map(userMapper::toSummaryDTO);
    }
    
    public Page<userSummaryDTO> searchUsers(String search, Pageable pageable) {
        return userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            search, search, search, pageable
        ).map(userMapper::toSummaryDTO);
    }

    public List<userSummaryDTO> getAllUsersList() {
        return userRepository.findAll().stream().map(userMapper::toSummaryDTO).toList();
    }

    @CacheEvict(value = {"userById", "userByEmail"}, allEntries = true)
    public void deleteUser(Long id){
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(userEntity);
    }

    public RefreshTokenResponseDTO validateAndReturnTokens(String refreshToken){
        if(jwtService.validateRefreshToken(refreshToken)){
            String email = jwtService.extractEmailFromRefreshToken(refreshToken);
            UserEntity userEntity = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

            String newAccessToken = jwtService.generateAccessToken(userEntity);
            String newRefreshToken = jwtService.generateRefreshToken(userEntity);
            
            return new RefreshTokenResponseDTO(newAccessToken, newRefreshToken);
        } else {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }
    }
}