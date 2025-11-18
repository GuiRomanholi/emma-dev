package br.com.fiap.emma.controller;

import br.com.fiap.emma.model.Person;
import br.com.fiap.emma.model.UserRole;
import br.com.fiap.emma.service.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PersonControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PersonService service;

    @InjectMocks
    private PersonController controller;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testFindById_Success() throws Exception {
        Person p = new Person("Maria", "maria@teste.com", "abc", UserRole.ADMIN);
        p.setId(2L);

        Mockito.when(service.findById(2L)).thenReturn(p);

        mockMvc.perform(get("/api/persons/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("maria@teste.com"));
    }

    @Test
    void testFindById_NotFound() throws Exception {
        Mockito.when(service.findById(99L)).thenThrow(new RuntimeException("not found"));

        mockMvc.perform(get("/api/persons/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
