package br.com.fiap.emma.repository;

import br.com.fiap.emma.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    UserDetails findByEmail(String email);
}
