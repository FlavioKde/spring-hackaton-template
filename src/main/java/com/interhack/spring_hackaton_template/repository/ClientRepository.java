package com.interhack.spring_hackaton_template.repository;

import com.interhack.spring_hackaton_template.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByZone(String zone);
    List<Client> findByActiveTrue();
}
