package br.com.fiap.emma.dto;

import org.springframework.hateoas.Link;

public record ReviewResponse(Long id, String description, Long readingId, Link link) {}
