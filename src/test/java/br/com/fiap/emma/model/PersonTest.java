package br.com.fiap.emma.model;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PersonTest {

    @Test
    void testGetAuthorities_Admin() {
        Person person = new Person("Admin", "admin@teste.com", "123", UserRole.ADMIN);

        List<? extends GrantedAuthority> authorities = (List<? extends GrantedAuthority>) person.getAuthorities();

        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testGetAuthorities_User() {
        Person person = new Person("User", "user@teste.com", "123", UserRole.USER);

        List<? extends GrantedAuthority> authorities = (List<? extends GrantedAuthority>) person.getAuthorities();

        assertEquals(1, authorities.size());
        assertEquals("ROLE_USER", authorities.get(0).getAuthority());
    }

    @Test
    void testGettersAndSetters() {
        Person person = new Person();
        person.setId(1L);
        person.setName("John Doe");
        person.setEmail("john@teste.com");
        person.setPassword("senha123");
        person.setRole(UserRole.ADMIN);

        assertEquals(1L, person.getId());
        assertEquals("John Doe", person.getName());
        assertEquals("john@teste.com", person.getEmail());
        assertEquals("senha123", person.getPassword());
        assertEquals(UserRole.ADMIN, person.getRole());

        // getUsername usa o email
        assertEquals("john@teste.com", person.getUsername());

        // métodos padrão do UserDetails
        assertTrue(person.isAccountNonExpired());
        assertTrue(person.isAccountNonLocked());
        assertTrue(person.isCredentialsNonExpired());
        assertTrue(person.isEnabled());
    }
}

