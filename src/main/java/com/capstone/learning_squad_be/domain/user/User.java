package com.capstone.learning_squad_be.domain.user;

import com.capstone.learning_squad_be.oauth.OAuthProvider;
import com.capstone.learning_squad_be.domain.enums.Role;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userName;    // 아이디, oauth의 경우 email

    @Column
    private String password;

    @Column(nullable = false)
    private String nickName;    // 닉네임

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private OAuthProvider oAuthProvider;

}
