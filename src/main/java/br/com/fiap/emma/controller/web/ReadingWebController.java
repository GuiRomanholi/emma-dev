package br.com.fiap.emma.controller.web;

import br.com.fiap.emma.dto.ReadingRequest;
import br.com.fiap.emma.model.Reading;
import br.com.fiap.emma.service.PersonService;
import br.com.fiap.emma.service.ReadingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/readings")
public class ReadingWebController {

    private final ReadingService readingService;
    private final PersonService personService;

    @Autowired
    public ReadingWebController(ReadingService readingService, PersonService personService) {
        this.readingService = readingService;
        this.personService = personService;
    }

    @GetMapping
    public String listReadings(Model model) {
        model.addAttribute("readings", readingService.findAll());
        return "reading/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("reading", new Reading());
        model.addAttribute("persons", personService.findAll());
        return "reading/form-create";
    }

    @PostMapping("/create")
    public String createReading(@ModelAttribute("reading") Reading reading,
                                @RequestParam("personId") Long personId,
                                RedirectAttributes redirectAttributes) {
        try {
            readingService.create(personId, reading);
            redirectAttributes.addFlashAttribute("successMessage", "Leitura criada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao criar leitura: " + e.getMessage());
        }
        return "redirect:/readings";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model,
                               RedirectAttributes redirectAttributes) { // <-- CORREÇÃO AQUI
        try {
            Reading reading = readingService.findById(id);
            ReadingRequest request = new ReadingRequest();
            request.setDate(reading.getDate());
            request.setDescription(reading.getDescription());
            request.setHumor(reading.getHumor());
            request.setPersonId(reading.getPerson() != null ? reading.getPerson().getId() : null);

            model.addAttribute("readingRequest", request);
            model.addAttribute("readingId", id);
            model.addAttribute("persons", personService.findAll());

            return "reading/form-edit";
        } catch (RuntimeException e) {
            // Agora 'redirectAttributes' está disponível aqui
            redirectAttributes.addFlashAttribute("errorMessage", "Leitura não encontrada.");
            return "redirect:/readings";
        }
    }


    @PostMapping("/update/{id}")
    public String updateReading(@PathVariable Long id,
                                @ModelAttribute("readingRequest") ReadingRequest request,
                                RedirectAttributes redirectAttributes) {
        try {
            readingService.update(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Leitura atualizada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar leitura: " + e.getMessage());
        }
        return "redirect:/readings";
    }

    @GetMapping("/delete/{id}")
    public String deleteReading(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            readingService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Leitura deletada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao deletar leitura: " + e.getMessage());
        }
        return "redirect:/readings";
    }
}