package com.visma.task.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.visma.task.dto.UserDto;
import com.visma.task.security.JWTUtil;
import com.visma.task.security.model.LoginCredentials;
import com.visma.task.models.User;
import com.visma.task.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private JWTUtil jwtUtil;
    private UserService userService;
    private AuthenticationManager authManager;
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ObjectNode registerHandler(@RequestBody UserDto userDto) throws Exception {
        String encodedPass = passwordEncoder.encode(userDto.getPassword());
        userDto.setPassword(encodedPass);
        User insertedUser = userService.insertUser(convertToEntity(userDto));
        String token = jwtUtil.generateToken(userDto.getUsername());

        ObjectNode response = new ObjectMapper().createObjectNode();

        response.put("id",insertedUser.getId());
        response.put("token",token);

        return response;
    }

    @PostMapping("/login")
    public Map<String, Object> loginHandler(@RequestBody LoginCredentials body){
        try {
            UsernamePasswordAuthenticationToken authInputToken =
                    new UsernamePasswordAuthenticationToken(body.getUsername(), body.getPassword());

            authManager.authenticate(authInputToken);

            String token = jwtUtil.generateToken(body.getUsername());

            return Collections.singletonMap("jwt-token", token);
        }catch (AuthenticationException authExc){
            throw new RuntimeException("Invalid Login Credentials");
        }
    }

    private User convertToEntity(UserDto userDto){
        return new User(userDto);
    }
}