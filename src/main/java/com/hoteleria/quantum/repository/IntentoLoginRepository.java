package com.hoteleria.quantum.repository;

import com.hoteleria.quantum.entity.IntentoLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntentoLoginRepository extends JpaRepository<IntentoLogin, Long> {
}
