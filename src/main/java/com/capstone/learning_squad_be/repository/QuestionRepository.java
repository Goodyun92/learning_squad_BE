package com.capstone.learning_squad_be.repository;

import com.capstone.learning_squad_be.domain.Document;
import com.capstone.learning_squad_be.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question,Long> {
    Optional<Question> findFirstByQuestionNumberAndDocument(Integer questionNumber, Document document);
    List<Question> findByDocument(Document document);
    void deleteAllByDocument(Document document);
}
