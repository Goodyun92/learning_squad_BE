package com.capstone.learning_squad_be.domain;

import com.capstone.learning_squad_be.domain.user.User;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(nullable = false)
    @Lob    //긴 문자열 수용
    private String url;    //문서 주소

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer questioinSize;    //문제 수
}
