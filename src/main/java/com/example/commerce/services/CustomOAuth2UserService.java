package com.example.commerce.services;

import com.example.commerce.entities.UserEntity;
import com.example.commerce.enums.UserRole;
import com.example.commerce.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public CustomOAuth2UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);
        try {
            String email = oAuth2User.getAttribute("email");
            if (email == null) {
                throw new OAuth2AuthenticationException("Email not found in OAuth2 provider response");
            }
            Optional<UserEntity> user = userRepository.findByEmail(email);
            if(user.isEmpty()){
                UserEntity newUser = new UserEntity();
                newUser.setEmail(email);
                newUser.setFirstName(oAuth2User.getAttribute("given_name"));
                newUser.setLastName(oAuth2User.getAttribute("family_name"));
                newUser.setPassword(passwordEncoder.encode("oauth2user"));
                newUser.setRole(UserRole.CUSTOMER) ;
                userRepository.save(newUser);
            }
            return oAuth2User;
        } catch (Exception ex) {
            throw new OAuth2AuthenticationException(ex.getMessage());
        }
    }
}