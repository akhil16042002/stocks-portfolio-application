package com.akhil.stocks_portfolio.service;

import com.akhil.stocks_portfolio.dto.Response;
import com.akhil.stocks_portfolio.entity.User;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<Response<User>> addUser(User user);
}
