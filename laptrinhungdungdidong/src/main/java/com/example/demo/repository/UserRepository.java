package com.example.demo.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.users;
import java.util.Optional;


@Repository 
public interface  UserRepository extends JpaRepository<users, Long> {
    
    Optional<users>  findByEmail(String email);

    boolean existsByEmail(String email);

}
