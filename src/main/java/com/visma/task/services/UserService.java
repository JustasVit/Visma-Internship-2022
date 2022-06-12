package com.visma.task.services;

import com.visma.task.models.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers() throws Exception;

    User getUser(long id) throws Exception;

    User getUser(String username) throws  Exception;

    User insertUser(User user) throws Exception;
}
