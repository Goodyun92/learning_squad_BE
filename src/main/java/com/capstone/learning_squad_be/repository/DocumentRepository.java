package com.capstone.learning_squad_be.repository;

import com.capstone.learning_squad_be.domain.Document;
import com.capstone.learning_squad_be.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document,Long> {
    Optional<List<Document>> findByUser(User user);
}
