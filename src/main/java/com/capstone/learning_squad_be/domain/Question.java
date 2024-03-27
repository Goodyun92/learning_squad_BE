package com.capstone.learning_squad_be.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "document_id", referencedColumnName = "id")
    private Document document;

    @Column(nullable = false)
    @Lob    //긴 문자열 수용
    private String content;    //문제 내용

    @Column(nullable = false)
    private Integer questionNumber;    //문제 번호

}
