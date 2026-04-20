package org.godn.interactionservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "bots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bot {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "persona_description")
    private String personaDescription;
}
