package com.example.ms_proveedor.repository;
import com.example.ms_proveedor.entity.Proveedor;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    @Query("SELECT COUNT(c) > 0 FROM Proveedor c WHERE c.dniOrRuc = :numero")
    boolean existsByDniOrRuc(@Param("numero") String numero);
}