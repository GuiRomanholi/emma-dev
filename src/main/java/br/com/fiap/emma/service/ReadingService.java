package br.com.fiap.emma.service;

import br.com.fiap.emma.dto.ReadingRequest;
import br.com.fiap.emma.model.Person;
import br.com.fiap.emma.model.Reading;
import br.com.fiap.emma.repository.PersonRepository;
import br.com.fiap.emma.repository.ReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReadingService {

    private final ReadingRepository readingRepository;
    private final PersonRepository personRepository;

    @Autowired
    public ReadingService(ReadingRepository readingRepository, PersonRepository personRepository) {
        this.readingRepository = readingRepository;
        this.personRepository = personRepository;
    }

    @Cacheable(value = "readings")
    public List<Reading> findAll() {
        return readingRepository.findAll();
    }

    @Cacheable(value = "reading", key = "#id")
    public Reading findById(Long id) {
        return readingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reading não encontrada: " + id));
    }

    @CachePut(value = "reading", key = "#result.id")
    @CacheEvict(value = "readings", allEntries = true)
    public Reading create(Long personId, Reading reading) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new RuntimeException("Person não encontrada: " + personId));

        reading.setPerson(person);
        return readingRepository.save(reading);
    }

    @CachePut(value = "reading", key = "#id")
    @CacheEvict(value = "readings", allEntries = true)
    public Reading update(Long id, ReadingRequest request) {
        Reading existing = findById(id);

        existing.setDate(request.getDate());
        existing.setDescription(request.getDescription());
        existing.setHumor(request.getHumor());

        if (request.getPersonId() != null) {
            Person person = personRepository.findById(request.getPersonId())
                    .orElseThrow(() -> new RuntimeException("Person não encontrada: " + request.getPersonId()));
            existing.setPerson(person);
        } else {
            existing.setPerson(null);
        }

        return readingRepository.save(existing);
    }

    @CacheEvict(value = {"reading", "readings"}, key = "#id", allEntries = true)
    public void delete(Long id) {
        readingRepository.deleteById(id);
    }

    public Page<Reading> findAllPageable(Pageable pageable) {
        return readingRepository.findAll(pageable);
    }
}