package com.mathotech.autopartshub.repository;

import com.mathotech.autopartshub.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findByName(String name);
    
    boolean existsByName(String name);
}
