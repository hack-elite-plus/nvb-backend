package com.nimbusventure.band.band;

import com.nimbusventure.band.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "band_id_unique", columnNames = "band_id")
        })
public class Band {
    @Id
    @Column(
            name="band_id",
            updatable = false,
            unique = true
    )
    private String bandId;

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
