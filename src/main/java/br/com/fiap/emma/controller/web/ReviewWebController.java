package br.com.fiap.emma.controller.web;

import br.com.fiap.emma.dto.ReviewRequest;
import br.com.fiap.emma.model.Review;
import br.com.fiap.emma.service.ReadingService;
import br.com.fiap.emma.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reviews")
public class ReviewWebController {

    private final ReviewService reviewService;
    private final ReadingService readingService;

    @Autowired
    public ReviewWebController(ReviewService reviewService, ReadingService readingService) {
        this.reviewService = reviewService;
        this.readingService = readingService;
    }

    @GetMapping
    public String listReviews(Model model) {
        model.addAttribute("reviews", reviewService.findAll());
        return "review/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("review", new Review());
        model.addAttribute("readings", readingService.findAll());
        return "review/form-create";
    }

    @PostMapping("/create")
    public String createReview(@ModelAttribute("review") Review review,
                               @RequestParam("readingId") Long readingId,
                               RedirectAttributes redirectAttributes) {
        try {
            reviewService.create(readingId, review);
            redirectAttributes.addFlashAttribute("successMessage", "Review criada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao criar review: " + e.getMessage());
        }
        return "redirect:/reviews";
    }


    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            Review review = reviewService.findById(id);
            ReviewRequest request = new ReviewRequest();
            request.setDescription(review.getDescription());
            request.setReadingId(review.getReading() != null ? review.getReading().getId() : null);

            model.addAttribute("reviewRequest", request);
            model.addAttribute("reviewId", id);
            model.addAttribute("readings", readingService.findAll());

            return "review/form-edit";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Review não encontrada.");
            return "redirect:/reviews";
        }
    }

    @PostMapping("/update/{id}")
    public String updateReview(@PathVariable Long id,
                               @ModelAttribute("reviewRequest") ReviewRequest request,
                               RedirectAttributes redirectAttributes) {
        try {
            reviewService.update(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Review atualizada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar review: " + e.getMessage());
        }
        return "redirect:/reviews";
    }

    @GetMapping("/delete/{id}")
    public String deleteReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Review deletada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao deletar review: " + e.getMessage());
        }
        return "redirect:/reviews";
    }
}