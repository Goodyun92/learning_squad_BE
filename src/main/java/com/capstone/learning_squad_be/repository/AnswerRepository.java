package com.capstone.learning_squad_be.repository;

import com.capstone.learning_squad_be.domain.Answer;
import com.capstone.learning_squad_be.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer,Long> {
    Answer findByQuestion(Question question);
    void deleteByQuestion(Question question);
}
