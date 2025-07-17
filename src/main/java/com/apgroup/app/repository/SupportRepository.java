package com.apgroup.app.repository;

import com.apgroup.app.entity.Support;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SupportRepository extends JpaRepository<Support, Long> {
    List<Support> findByType(String type);

}
