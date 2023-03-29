package com.nimbusventure.band.pet;

import com.nimbusventure.band.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.time.LocalDate;

import static javax.persistence.GenerationType.SEQUENCE;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table
public class Pet {
    @Id
    @SequenceGenerator(
            name="pet_sequence",
            sequenceName="pet_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = SEQUENCE,
            generator = "pet_sequence"
    )
    @Column(
            name="pet_id",
            updatable = false
    )
    private Long id;


    @Enumerated(EnumType.STRING)
    private PetType type;

    private LocalDate dateOfBirth;

    private String name;

    private Gender gender;

    @ManyToOne
    @JoinColumn(
            name="user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "user_id_fk"
            )
    )
    private User user;
}
