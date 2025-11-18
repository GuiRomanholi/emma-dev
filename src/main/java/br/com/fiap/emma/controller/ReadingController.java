package br.com.fiap.emma.controller;

import br.com.fiap.emma.dto.ReadingRequest;
import br.com.fiap.emma.dto.ReadingResponse;
import br.com.fiap.emma.model.Reading;
import br.com.fiap.emma.service.ReadingService;
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

@Tag(name = "Reading", description = "Operações com Reading")
@RestController
@RequestMapping(value = "/api/readings", produces = {"application/json"})
public class ReadingController {

    @Autowired
    private ReadingService service;

    @Operation(summary = "Retorna todas as leituras")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de leituras",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReadingResponse.class)))
    })
    @GetMapping
    public ResponseEntity<Page<ReadingResponse>> findAll(
            @RequestParam(defaultValue = "0") Integer page
    ) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("date").descending());
        Page<ReadingResponse> responsePage = service.findAllPageable(pageable)
                .map(this::toResponse);

        return new ResponseEntity<>(responsePage, HttpStatus.OK);
    }

    @Operation(summary = "Retorna uma leitura por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leitura encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReadingResponse.class))),
            @ApiResponse(responseCode = "404", description = "Leitura não encontrada",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReadingResponse> findById(@PathVariable Long id) {
        try {
            Reading r = service.findById(id);
            return ResponseEntity.ok(toResponse(r));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Cria uma nova leitura e a associa a um Person")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Leitura cadastrada com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReadingResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Atributos informados são inválidos ou Person não encontrado",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping
    public ResponseEntity<ReadingResponse> create(@RequestBody @Valid ReadingRequest request) {
        try {
            Reading reading = new Reading();
            reading.setDate(request.getDate());
            reading.setDescription(request.getDescription());
            reading.setHumor(request.getHumor());

            Reading saved = service.create(request.getPersonId(), reading);

            URI uri = URI.create("/readings/" + saved.getId());

            return ResponseEntity.created(uri).body(toResponse(saved));

        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Atualiza uma leitura")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atualizado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReadingResponse.class))),
            @ApiResponse(responseCode = "404", description = "Reading ou Person não encontrado",
                    content = @Content(schema = @Schema()))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ReadingResponse> update(@PathVariable Long id,
                                                  @RequestBody @Valid ReadingRequest request) {
        try {
            Reading updated = service.update(id, request);
            return ResponseEntity.ok(toResponse(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Remove uma leitura")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Removido"),
            @ApiResponse(responseCode = "404", description = "Reading não encontrada")
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

    private ReadingResponse toResponse(Reading r) {
        Link self = WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(ReadingController.class).findById(r.getId()))
                .withSelfRel();

        return new ReadingResponse(
                r.getId(),
                r.getDate(),
                r.getDescription(),
                r.getHumor(),
                r.getPerson().getId(),
                self
        );
    }
}
