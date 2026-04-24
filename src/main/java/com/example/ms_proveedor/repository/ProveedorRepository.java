package com.example.ms_proveedor.repository;

import com.example.ms_proveedor.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    @Query("SELECT COUNT(p) > 0 FROM Proveedor p WHERE p.dniOrRuc = :numero")
    boolean existsByDniOrRuc(@Param("numero") String numero);
}