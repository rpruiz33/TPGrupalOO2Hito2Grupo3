
package com.example.hito2grupo3.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/responsable")
public class ResponsableController {

    @GetMapping("/index")
    public String mostrarIndex() {
        return "responsable/index";
    }
}
