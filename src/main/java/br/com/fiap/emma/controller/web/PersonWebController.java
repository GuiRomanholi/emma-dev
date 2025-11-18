package br.com.fiap.emma.controller.web;

import br.com.fiap.emma.model.Person;
import br.com.fiap.emma.model.UserRole;
import br.com.fiap.emma.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
@RequestMapping("/persons")
public class PersonWebController {

    private final PersonService personService;

    @Autowired
    public PersonWebController(PersonService personService) {
        this.personService = personService;
    }


    @GetMapping
    public String listPersons(Model model) {
        model.addAttribute("persons", personService.findAll());
        return "person/list"; // View: person/list.html
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("person", new Person());
        model.addAttribute("allowedRoles", Arrays.asList(UserRole.values()));
        return "person/form"; // View: person/form.html
    }

    @PostMapping("/create")
    public String createPerson(@ModelAttribute("person") Person person, RedirectAttributes redirectAttributes) {
        try {
            personService.save(person);
            redirectAttributes.addFlashAttribute("successMessage", "Pessoa criada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao criar pessoa: " + e.getMessage());
        }
        return "redirect:/persons";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("person", personService.findById(id));
            model.addAttribute("allowedRoles", Arrays.asList(UserRole.values()));
            return "person/form"; // View: person/form.html
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Pessoa não encontrada.");
            return "redirect:/persons";
        }
    }

    @PostMapping("/update/{id}")
    public String updatePerson(@PathVariable Long id,
                               @ModelAttribute("person") Person person,
                               RedirectAttributes redirectAttributes) {
        try {
            personService.update(id, person);
            redirectAttributes.addFlashAttribute("successMessage", "Pessoa atualizada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar pessoa: " + e.getMessage());
        }
        return "redirect:/persons";
    }

    @GetMapping("/delete/{id}")
    public String deletePerson(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            personService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Pessoa deletada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao deletar pessoa: " + e.getMessage());
        }
        return "redirect:/persons";
    }
}