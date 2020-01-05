package com.concourse.controllers;

import com.concourse.models.User;
import com.concourse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*", allowCredentials = "true", allowedHeaders = "*")
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    public UserController(UserRepository userRepository  ) {
        this.userRepository = userRepository;
    }
}
