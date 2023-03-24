package com.nimbusventure.band.band;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BandRepository extends JpaRepository<Band, String> {
    Optional<Band> findBandByBandId(String id);
}
