package com.concourse;

import com.concourse.tools.EmailServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ConcourseApplication {

    /*
    Before running the app, make sure to
    1. Fill the gmail credentials, client base url in src/main/resources/keys.properties


     */
    public static void main(String[] args) {
        SpringApplication.run(ConcourseApplication.class, args);
    }
}
