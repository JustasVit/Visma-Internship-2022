package com.visma.task.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.visma.task.models.User;
import com.visma.task.security.MyUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MyUserDetailsServiceTest {

    @Mock
    private UserServiceImpl userService;

    private MyUserDetailsService myUserDetailsService;

    @BeforeEach
    void setUp(){
        myUserDetailsService = new MyUserDetailsService(userService);
    }


    @Test
    void loadUserByUsernameTest() throws JsonProcessingException {
        when(userService.getUser("Vardenis")).thenReturn(new User(1L,"Vardenis","Slaptazodis123"));
        UserDetails loadedUserDetails = myUserDetailsService.loadUserByUsername("Vardenis");
        assertThat(loadedUserDetails.getUsername()).isEqualTo("Vardenis");
        assertThat(loadedUserDetails.getPassword()).isEqualTo("Slaptazodis123");

    }
}
