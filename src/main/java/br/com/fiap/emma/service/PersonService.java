package br.com.fiap.emma.service;

import br.com.fiap.emma.model.Person;
import br.com.fiap.emma.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
// IMPORT NECESSÁRIO
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonService {

    private final PersonRepository repository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PersonService(PersonRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Cacheable(value = "persons")
    public List<Person> findAll() {
        return repository.findAll();
    }

    @Cacheable(value = "person", key = "#id")
    public Person findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person não encontrada: " + id));
    }

    @CachePut(value = "person", key = "#result.id")
    @CacheEvict(value = "persons", allEntries = true)
    public Person save(Person person) {
        String encryptedPassword = passwordEncoder.encode(person.getPassword());
        person.setPassword(encryptedPassword);

        return repository.save(person);
    }

    @CachePut(value = "person", key = "#id")
    @CacheEvict(value = "persons", allEntries = true)
    public Person update(Long id, Person person) {
        Person existing = findById(id);
        existing.setName(person.getName());
        existing.setEmail(person.getEmail());
        existing.setRole(person.getRole());

        if (person.getPassword() != null && !person.getPassword().isEmpty()) {
            String encryptedPassword = passwordEncoder.encode(person.getPassword());
            existing.setPassword(encryptedPassword);
        }

        return repository.save(existing);
    }

    @CacheEvict(value = {"person", "persons"}, key = "#id", allEntries = true)
    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Person> findAllPageable(Pageable pageable) {
        return repository.findAll(pageable);
    }
}