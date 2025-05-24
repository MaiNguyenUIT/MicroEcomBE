package com.example.user_service.serviceImpl;

import com.example.user_service.config.JwtProvider;
import com.example.user_service.dto.*;
import com.example.user_service.exception.BadRequestException;
import com.example.user_service.exception.NotFoundException;
import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class UserService implements com.example.user_service.service.UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CustomerUserDetailService customerUserDetailService;
    @Autowired
    private JwtProvider jwtProvider;

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        Authentication authentication = authenticate(username, password);

        User user = userRepository.findByusername(username);
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String jwt =  jwtProvider.generatedToken(authentication);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(jwt);
        return authResponse;
    }

    @Override
    public void register(RegisterRequest registerRequest) {
        User user = userRepository.findByusername(registerRequest.getUsername());
        if(user != null){
            throw new BadRequestException("User is already existed with username " + registerRequest.getUsername());
        }

        User createdUser = new User();
        createdUser.setUsername(registerRequest.getUsername());
        createdUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        createdUser.setDisplayName(registerRequest.getDisplayName());
        userRepository.save(createdUser);
    }

    @Override
    public User findUserByJwtToken() {
        return userRepository.findById(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(
                () -> new NotFoundException("User not found with id: " + SecurityContextHolder.getContext().getAuthentication().getName())
        );
    };

    @Override
    public User findUserByUserId(String userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User is not found with id: " + userId));
    };

    @Override
    public List<User> getAllUser(){
        return userRepository.findByRole("ROLE_USER");
    };

    @Override
    public List<User> getAllSeller(){
        return userRepository.findByRole("ROLE_SELLER");
    };


    @Override
    public User updateUserInformation(UserInforDTO userInforDTO, User user) {
        user.setPhone(userInforDTO.getPhone());
        user.setPhoto(userInforDTO.getPhoto());
        user.setAddress(userInforDTO.getAddress());
        user.setEmail(userInforDTO.getEmail());
        return userRepository.save(user);
    }


    private Authentication authenticate(String username, String password) {
        UserDetails userDetails = customerUserDetailService.loadUserByUsername(username);

        if(userDetails == null){
            throw new BadRequestException("Invalid username....");
        }

        if(!passwordEncoder.matches(password,userDetails.getPassword())){
            System.out.println("invalid password");
            throw new BadRequestException("Invalid password");
        }

        return new UsernamePasswordAuthenticationToken(userDetails,
                null, userDetails.getAuthorities());
    }

    @Override
    public User blockUser(String userId){
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User is not found with id: " + userId)
        );
        user.setBlock(true);
        return userRepository.save(user);
    }

    @Override
    public List<User> getUserByIds(List<String> userIds) {
        System.out.println("Get user by ids....");
        return userRepository.findAllById(userIds);
    }

}
