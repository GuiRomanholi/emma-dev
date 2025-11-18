package br.com.fiap.emma.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ReadingRequest {

    @NotNull(message = "A data é obrigatória")
    private LocalDateTime date;

    @NotBlank(message = "A descrição é obrigatória")
    private String description;

    @NotBlank(message = "O humor é obrigatório")
    private String humor;

    private Long personId;

    public ReadingRequest() {}

    public ReadingRequest(LocalDateTime date, String description, String humor) {
        this.date = date;
        this.description = description;
        this.humor = humor;
    }

    // Getters e Setters
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getHumor() { return humor; }
    public void setHumor(String humor) { this.humor = humor; }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }
}
