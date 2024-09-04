package com.akhil.stocks_portfolio.controller;

import com.akhil.stocks_portfolio.dto.Response;
import com.akhil.stocks_portfolio.entity.User;
import com.akhil.stocks_portfolio.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping(value = "/create_user")
    public ResponseEntity<Response<User>> onCreateUser(@RequestBody User user) {
        return userService.addUser(user);
    }
}
