package br.com.fiap.emma.controller.web;

import br.com.fiap.emma.model.Person;
// 1. Importe o seu Enum 'UserRole' (ajuste o pacote se necessário)
import br.com.fiap.emma.model.UserRole; // <-- CORREÇÃO AQUI
import br.com.fiap.emma.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
@RequestMapping("/")
public class AuthWebController {

    @Autowired
    private PersonService personService;

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("loginError", "Email ou senha inválidos.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("person")) {
            model.addAttribute("person", new Person());
        }

        model.addAttribute("allowedRoles", Arrays.asList(UserRole.values()));

        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("person") Person person,
                               RedirectAttributes redirectAttributes) {
        try {
            personService.save(person);

            redirectAttributes.addFlashAttribute("successMessage", "Usuário registrado com sucesso! Faça seu login.");
            return "redirect:/login";

        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Este email já está em uso. Tente outro.");
            redirectAttributes.addFlashAttribute("person", person);
            return "redirect:/register";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ocorreu um erro inesperado: " + e.getMessage());
            redirectAttributes.addFlashAttribute("person", person);
            return "redirect:/register";
        }
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login";
    }
}