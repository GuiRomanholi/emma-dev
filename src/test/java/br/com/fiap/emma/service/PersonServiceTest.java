package br.com.fiap.emma.service;

import br.com.fiap.emma.model.Person;
import br.com.fiap.emma.model.UserRole;
import br.com.fiap.emma.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PersonServiceTest {

    @Mock
    private PersonRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PersonService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindById_Sucesso() {
        Person person = new Person("John", "john@teste.com", "123", UserRole.USER);
        when(repository.findById(1L)).thenReturn(Optional.of(person));

        Person result = service.findById(1L);

        assertNotNull(result);
        assertEquals("john@teste.com", result.getEmail());
    }

    @Test
    void testFindById_NotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.findById(1L));
    }

    @Test
    void testSave() {
        Person person = new Person("John", "john@teste.com", "123", UserRole.USER);

        when(passwordEncoder.encode("123")).thenReturn("encoded123");
        when(repository.save(person)).thenReturn(person);

        Person saved = service.save(person);

        assertEquals("encoded123", saved.getPassword());
        verify(repository, times(1)).save(person);
    }

    @Test
    void testUpdate() {
        Person existing = new Person("Old Name", "old@teste.com", "oldPasswordHash", UserRole.USER);
        Person updated = new Person("New Name", "new@teste.com", "NewPass123", UserRole.ADMIN);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("NewPass123")).thenReturn("EncodedNewPass");
        when(repository.save(existing)).thenReturn(existing);

        Person result = service.update(1L, updated);

        assertEquals("New Name", result.getName());
        assertEquals("new@teste.com", result.getEmail());
        assertEquals(UserRole.ADMIN, result.getRole());
        assertEquals("EncodedNewPass", result.getPassword());
    }

    @Test
    void testDelete() {
        doNothing().when(repository).deleteById(1L);

        service.delete(1L);

        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void testFindAll() {
        List<Person> persons = List.of(
                new Person("A", "a@teste.com", "a", UserRole.USER),
                new Person("B", "b@teste.com", "b", UserRole.ADMIN)
        );

        when(repository.findAll()).thenReturn(persons);

        List<Person> result = service.findAll();

        assertEquals(2, result.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testFindAllPageable() {
        Pageable pageable = mock(Pageable.class);
        Page<Person> page = mock(Page.class);

        when(repository.findAll(pageable)).thenReturn(page);

        Page<Person> result = service.findAllPageable(pageable);

        assertNotNull(result);
        verify(repository, times(1)).findAll(pageable);
    }

}