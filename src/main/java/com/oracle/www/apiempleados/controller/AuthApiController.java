package com.oracle.www.apiempleados.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthApiController {

    @GetMapping({"/", "", " ", "/ ", "/login"})
    public String loginView(){
        return "login";
    }

}
