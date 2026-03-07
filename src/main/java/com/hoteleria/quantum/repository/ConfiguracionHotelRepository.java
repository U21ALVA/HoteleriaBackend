package com.hoteleria.quantum.repository;

import com.hoteleria.quantum.entity.ConfiguracionHotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracionHotelRepository extends JpaRepository<ConfiguracionHotel, String> {

    Optional<ConfiguracionHotel> findByClave(String clave);
}
