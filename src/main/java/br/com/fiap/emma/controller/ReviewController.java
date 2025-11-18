package br.com.fiap.emma.controller;

import br.com.fiap.emma.dto.ReviewRequest;
import br.com.fiap.emma.dto.ReviewResponse;
import br.com.fiap.emma.model.Review;
import br.com.fiap.emma.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.net.URI;
import java.util.List;

@Tag(name = "Review", description = "Operações com Review")
@RestController
@RequestMapping(value = "/api/reviews", produces = {"application/json"})
public class ReviewController {

    @Autowired
    private ReviewService service;

    @Operation(summary = "Retorna todas as reviews")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de reviews",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReviewResponse.class)))
    })
    @GetMapping
    public ResponseEntity<Page<ReviewResponse>> findAll(
            @RequestParam(defaultValue = "0") Integer page
    ) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("id").ascending());
        Page<ReviewResponse> responsePage = service.findAllPageable(pageable)
                .map(this::toResponse);

        return new ResponseEntity<>(responsePage, HttpStatus.OK);
    }

    @Operation(summary = "Retorna uma review por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReviewResponse.class))),
            @ApiResponse(responseCode = "404", description = "Review não encontrada",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> findById(@PathVariable Long id) {
        try {
            Review r = service.findById(id);
            return ResponseEntity.ok(toResponse(r));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Cria uma review para uma Reading")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Review criada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReviewResponse.class))),
            @ApiResponse(responseCode = "400", description = "Atributos informados são inválidos ou Reading não encontrada",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping
    public ResponseEntity<ReviewResponse> create(
            @RequestBody @Valid ReviewRequest request) {

        try {
            Review review = new Review();
            review.setDescription(request.getDescription());

            Review saved = service.create(request.getReadingId(), review);

            URI uri = URI.create("/reviews/" + saved.getId());
            return ResponseEntity.created(uri).body(toResponse(saved));

        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    @Operation(summary = "Atualiza uma review")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atualizado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReviewResponse.class))),
            @ApiResponse(responseCode = "404", description = "Review ou Reading não encontrada",
                    content = @Content(schema = @Schema()))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponse> update(@PathVariable Long id,
                                                 @RequestBody @Valid ReviewRequest request) {
        try {
            Review updated = service.update(id, request);
            return ResponseEntity.ok(toResponse(updated));

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Remove uma review")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Removido"),
            @ApiResponse(responseCode = "404", description = "Review não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private ReviewResponse toResponse(Review r) {
        Link self = WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(ReviewController.class).findById(r.getId()))
                .withSelfRel();

        return new ReviewResponse(
                r.getId(),
                r.getDescription(),
                r.getReading().getId(),
                self
        );
    }
}
