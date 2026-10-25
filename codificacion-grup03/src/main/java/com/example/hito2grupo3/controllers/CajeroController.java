package com.example.hito2grupo3.controllers;

import com.example.hito2grupo3.entities.Cajero;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.repositories.StaffRepository;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cajero")
public class CajeroController {

    private final StaffRepository staffRepository;

    public CajeroController(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

   @GetMapping("/index")
public String mostrarIndex(Principal principal, Model model) {

    if (principal == null) {
        return "redirect:/login";
    }

    Staff staff = staffRepository
            .findByUsuario_Email(principal.getName())
            .orElse(null);

    if (staff == null) {
        return "redirect:/login?error=empleadoNoEncontrado";
    }

    if (!(staff instanceof Cajero)) {
        return "redirect:/empleado/index";
    }

    model.addAttribute("cajero", staff);
    model.addAttribute("esCajero", true);

    return "cajero/Cajero-index";
}
}
