package com.hoteleria.quantum.repository;

import com.hoteleria.quantum.entity.Huesped;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HuespedRepository extends JpaRepository<Huesped, Long> {

    Optional<Huesped> findByDocumentoIdentidad(String documentoIdentidad);

    Boolean existsByDocumentoIdentidad(String documentoIdentidad);

    List<Huesped> findByNombreCompletoContainingIgnoreCase(String nombre);

    @Query("SELECT h FROM Huesped h WHERE LOWER(h.nombreCompleto) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR h.documentoIdentidad LIKE CONCAT('%', :query, '%')")
    List<Huesped> searchByNombreOrDocumento(@Param("query") String query);
}
