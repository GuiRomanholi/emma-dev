package br.com.fiap.emma.controller;

import br.com.fiap.emma.dto.AuthDTO;
import br.com.fiap.emma.dto.LoginResponseDTO;
import br.com.fiap.emma.dto.RegisterDTO;
import br.com.fiap.emma.model.Person;
import br.com.fiap.emma.model.UserRole;
import br.com.fiap.emma.repository.PersonRepository;
import br.com.fiap.emma.service.PersonService;
import br.com.fiap.emma.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Endpoints de Login e Registro")
@RestController
@RequestMapping(value = "/api/auth", produces = {"application/json"})
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PersonService personService;


    @Operation(summary = "Realiza o login do usuário e retorna um token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login bem-sucedido, retorna token e dados do usuário",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (dados faltando)",
                    content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Autenticação falhou (usuário ou senha incorretos)",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthDTO authDTO) {
        var userPwd = new UsernamePasswordAuthenticationToken(
                authDTO.email(),
                authDTO.password()
        );
        var auth = this.authenticationManager.authenticate(userPwd);

        var person = (Person) auth.getPrincipal();

        var token = tokenService.generateToken(person);

        return ResponseEntity.ok(new LoginResponseDTO(
                token,
                person.getName(),
                person.getEmail(),
                person.getRole()
        ));
    }

    @Operation(summary = "Registra um novo usuário na plataforma")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário registrado com sucesso",
                    content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Erro de validação (e-mail já existe ou role inválida)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterDTO registerDTO) {
        if (personRepository.findByEmail(registerDTO.email()) != null) {
            return ResponseEntity.badRequest().body("E-mail já cadastrado.");
        }

        UserRole role;
        if (registerDTO.role() == null || registerDTO.role().isEmpty() || registerDTO.role().equalsIgnoreCase("USER")) {
            role = UserRole.USER;
        } else if (registerDTO.role().equalsIgnoreCase("ADMIN")) {
            role = UserRole.ADMIN;
        } else {
            return ResponseEntity.badRequest().body("Role inválida. Use 'USER' ou 'ADMIN'.");
        }


        Person newPerson = new Person(
                registerDTO.name(),
                registerDTO.email(),
                registerDTO.password(),
                role
        );

        try {
            personService.save(newPerson);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ocorreu um erro inesperado: " + e.getMessage());
        }
    }
}