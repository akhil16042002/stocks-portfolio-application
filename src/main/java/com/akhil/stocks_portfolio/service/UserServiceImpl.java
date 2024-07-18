package com.akhil.stocks_portfolio.service;

import com.akhil.stocks_portfolio.dto.Response;
import com.akhil.stocks_portfolio.entity.User;
import com.akhil.stocks_portfolio.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl implements UserService{

    @Autowired
    UserRepository userRepository;

    @Override
    public ResponseEntity<Response<User>> addUser(User user) {
        try {
            log.info("Saving user: {}", user.getUserName());
            User getUser = userRepository.save(user);
            log.info("Successfully saved user: {}", user.getUserName());
            return Response.success(HttpStatus.OK, getUser);
        } catch (DataIntegrityViolationException e) {
            log.error("user: {} already exists", user.getUserName(), e);
            return Response.failed(HttpStatus.BAD_REQUEST, "user: " +  user.getUserName() + " already exists");
        } catch (Exception e) {
            log.error("Error while saving user: {}", user.getUserName(), e);
            return Response.failed(HttpStatus.BAD_REQUEST, "Error while saving user: " +  user.getUserName());
        }
    }
}
