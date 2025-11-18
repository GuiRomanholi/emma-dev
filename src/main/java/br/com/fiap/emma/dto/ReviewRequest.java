package br.com.fiap.emma.dto;

import jakarta.validation.constraints.NotBlank;

public class ReviewRequest {

    @NotBlank(message = "A descrição é obrigatória")
    private String description;

    private Long readingId;

    public ReviewRequest() {}

    public ReviewRequest(String description) {
        this.description = description;
    }

    // Getters e Setters

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getReadingId() {
        return readingId;
    }

    public void setReadingId(Long readingId) {
        this.readingId = readingId;
    }
}
