package com.oracle.www.apiempleados.controller;

import com.oracle.www.apiempleados.entity.UsuariosLogin;
import com.oracle.www.apiempleados.security.repository.UsuariosLoginRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthApiController {

    private final BCryptPasswordEncoder passwordEncoder;
    private final UsuariosLoginRepository usuariosLoginRepository;

    public AuthApiController(BCryptPasswordEncoder passwordEncoder, UsuariosLoginRepository usuariosLoginRepository) {
        this.passwordEncoder = passwordEncoder;
        this.usuariosLoginRepository = usuariosLoginRepository;
    }

    @GetMapping({"/", "", " ", "/ ", "/login"})
    public String loginView(){
        return "login";
    }

    @PostMapping("/auth/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        UsuariosLogin usuario = usuariosLoginRepository.findByUsername(username);

        if (usuario == null || !passwordEncoder.matches(password, usuario.getPassword())) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
            return "error-session";
        }

        session.setAttribute("usuario", usuario);

        String redirect = (String) session.getAttribute("redirectAfterLogin");
        if (redirect != null) {
            session.removeAttribute("redirectAfterLogin");
            return "redirect:" + redirect;
        }

        return "redirect:/employee/inicio";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "redirect:/login";
    }
}
