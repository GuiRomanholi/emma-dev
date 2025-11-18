package br.com.fiap.emma.controller;

import br.com.fiap.emma.model.Review;
import br.com.fiap.emma.service.ReviewService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/emma")
@Tag(name = "Emma Assistant", description = "Suporte emocional com IA")
public class EmmaAssistantController {

    private final ChatClient chatClient;

    @Autowired
    private ReviewService reviewService;

    public EmmaAssistantController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/feeling")
    @Operation(
            summary = "Obter apoio emocional baseado no sentimento",
            description = "Retorna dicas e estratégias acolhedoras com base no sentimento e descrição fornecidos."
    )
    @ApiResponse(responseCode = "200", description = "Resposta gerada com sucesso")
    public FeelingResponse getFeelingSupport(
            @Parameter(description = "Sentimento atual do usuário (ex: ansioso, triste, estressado)")
            @RequestParam(defaultValue = "sobrecarregado") String feeling,

            @Parameter(description = "Descrição adicional ou contexto da situação (opcional)")
            @RequestParam(defaultValue = "Muito Cansado") String description,

            @RequestParam Long readingId // <-- ID da leitura associada
    ) {

        String prompt = """
                Você é Emma, uma assistente de bem-estar gentil, empática e acolhedora.
                Responda em primeira pessoa com 3 a 5 estratégias práticas.
                Seja breve, empática e jamais forneça conselhos médicos ou diagnósticos.
                E de respostas com no maximo 1100 caracteres

                Sentimento informado: %s
                Descrição/contexto: %s
                """.formatted(feeling, description);

        String response = chatClient.prompt()
                .system("""
                        Você é Emma, uma assistente emocional solidária e acolhedora.
                        Sempre responda de forma compreensiva, curta e prática.
                """)
                .user(prompt)
                .call()
                .content();

        Review review = new Review();
        review.setDescription(response);

        reviewService.create(readingId, review);

        return new FeelingResponse(response);
    }

    public record FeelingResponse(String message) {}
}
