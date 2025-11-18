package br.com.fiap.emma.controller;

import br.com.fiap.emma.dto.PersonRequest;
import br.com.fiap.emma.dto.PersonResponse;
import br.com.fiap.emma.model.Person;
import br.com.fiap.emma.service.PersonService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;


@RestController
@RequestMapping(value = "/api/persons", produces = {"application/json"})
@Tag(name = "Person", description = "Operações com Person")
public class PersonController {

    @Autowired
    private PersonService service;

    @Operation(summary = "Retorna todos os usuários")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuários",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PersonResponse.class)))
    })
    @GetMapping
    public ResponseEntity<Page<PersonResponse>> findAll(
            @RequestParam(defaultValue = "0") Integer page
    ) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("name").ascending());
        Page<PersonResponse> responsePage = service.findAllPageable(pageable)
                .map(this::toResponse);

        return ResponseEntity.ok(responsePage);
    }

    @Operation(summary = "Retorna um usuario por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = PersonResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Nenhum usuario encontrado",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/{id}")
    public ResponseEntity<PersonResponse> findById(@PathVariable Long id) {
        try {
            Person p = service.findById(id);
            return ResponseEntity.ok(toResponse(p));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Cria um novo usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Criado com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = PersonResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Requisição inválida",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping
    public ResponseEntity<PersonResponse> create(@RequestBody @Valid PersonRequest request) {
        Person person = new Person(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getRole()
        );

        Person saved = service.save(person);

        return ResponseEntity.created(java.net.URI.create("/persons/" + saved.getId()))
                .body(toResponse(saved));
    }

    @Operation(summary = "Atualiza um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Atualizado com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = PersonResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema()))
    })
    @PutMapping("/{id}")
    public ResponseEntity<PersonResponse> update(@PathVariable Long id,
                                                 @RequestBody @Valid PersonRequest request) {
        try {
            Person p = new Person(
                    request.getName(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getRole()
            );

            Person updated = service.update(id, p);
            return ResponseEntity.ok(toResponse(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Exclui um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Removido"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
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

    private PersonResponse toResponse(Person p) {
        Link self = WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(PersonController.class).findById(p.getId()))
                .withSelfRel();

        return new PersonResponse(
                p.getId(),
                p.getName(),
                p.getEmail(),
                p.getPassword(),
                p.getRole()
        );
    }
}
