package com.lcwr.user.service.services;

import com.lcwr.user.service.entities.User;

import java.util.List;

public interface UserService {

    //user operations

    // create user

    User saveUser(User user);

    //get all users
    List<User> getAllUser();

    //get single user of given userId

    User getUser(String userId);

}
