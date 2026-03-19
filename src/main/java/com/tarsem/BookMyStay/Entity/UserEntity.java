package com.tarsem.BookMyStay.Entity;

import com.tarsem.BookMyStay.Enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;
import java.util.Set;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true,nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_entity_role",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Enumerated(EnumType.STRING)
    private Set<Role> role;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime created_at;


}
