package br.com.fiap.emma.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RegisterDTO(

        String name,
        @NotBlank @Email(message = "E-mail inválido") String email,
        @NotBlank @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,25}$",
                message = "A senha deve ter entre 8 e 25 caracteres, conter ao menos uma letra maiúscula e um número, " +
                        "e não pode ter caracteres especiais.") String password,
        @Pattern(regexp = "^(ADMIN|USER)$", message = "A role deve ser 'ADMIN' ou 'USER'") String role
) {
}
