package com.concourse.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("post")
@CrossOrigin(origins = "*", allowedHeaders = "*", allowCredentials = "true")
public class PostController {



}
