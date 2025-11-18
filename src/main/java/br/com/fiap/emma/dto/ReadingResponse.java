package br.com.fiap.emma.dto;

import org.springframework.hateoas.Link;
import java.time.LocalDateTime;
import java.util.List;

public record ReadingResponse(Long id, LocalDateTime date, String description, String humor, Long personId, Link link) {}
