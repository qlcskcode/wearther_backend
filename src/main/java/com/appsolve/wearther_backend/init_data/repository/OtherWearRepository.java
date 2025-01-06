package com.appsolve.wearther_backend.init_data.repository;

import com.appsolve.wearther_backend.init_data.entity.OtherWear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OtherWearRepository extends JpaRepository<OtherWear, Long> {
    @Query("SELECT u.name FROM OtherWear u WHERE u.id = :id")
    String findNameById(Long id);
}