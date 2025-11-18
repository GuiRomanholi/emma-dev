package br.com.fiap.emma.controller.web;

import br.com.fiap.emma.model.Review;
import br.com.fiap.emma.service.ReadingService;
import br.com.fiap.emma.service.ReviewService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/emma")
public class EmmaAssistantWebController {

    private final ChatClient chatClient;
    private final ReadingService readingService;
    private final ReviewService reviewService;

    public EmmaAssistantWebController(ChatClient chatClient, ReadingService readingService, ReviewService reviewService) {
        this.chatClient = chatClient;
        this.readingService = readingService;
        this.reviewService = reviewService;
    }

    @GetMapping("/feeling")
    public String showFeelingForm(Model model) {
        model.addAttribute("readings", readingService.findAll());
        return "emma/emma-feeling";
    }

    @PostMapping("/feeling")
    public String processFeelingAndDescription(
            @RequestParam String feeling,
            @RequestParam(required = false, defaultValue = "") String description,
            @RequestParam Long readingId,
            Model model) {

        String prompt = """
                Você é Emma, uma assistente de bem-estar gentil, empática e acolhedora.
                Responda em primeira pessoa com 3 a 5 sugestões práticas e curtas.
                Nunca dê conselhos médicos, diagnósticos ou frases muito longas.
                Limite a resposta a no máximo 1100 caracteres.

                Sentimento informado: %s
                Descrição/contexto: %s
                """.formatted(feeling, description);

        String result = chatClient.prompt()
                .system("""
                        Você é Emma, uma assistente emocional solidária, gentil e acolhedora.
                        Responda de forma empática, prática e enxuta.
                """)
                .user(prompt)
                .call()
                .content();

        Review review = new Review();
        review.setDescription(result);
        reviewService.create(readingId, review);

        model.addAttribute("response", result);
        model.addAttribute("feeling", feeling);
        model.addAttribute("description", description);

        return "emma/emma-feeling-result";
    }
}