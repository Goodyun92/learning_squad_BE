package com.capstone.learning_squad_be.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "answers")
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "question_id", referencedColumnName = "id")
    private Question question;

    @Column(nullable = false)
    @Lob    //긴 문자열
    private String correctAnswer;    //모범 답안

    @Lob    //긴 문자열
    private String bestAnswer;    //사용자 베스트 답안

    @Column(nullable = false)
    private Integer score;    //점수
}
