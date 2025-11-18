package br.com.fiap.emma.dto;

import br.com.fiap.emma.model.UserRole;

public record LoginResponseDTO(String token, String nome, String email, UserRole role) {
}
