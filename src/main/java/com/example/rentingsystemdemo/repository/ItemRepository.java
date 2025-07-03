package com.example.rentingsystemdemo.repository;

import com.example.rentingsystemdemo.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByAvailableTrue();
    List<Item> findByNameContainingIgnoreCase(String keyword);
    List<Item> findByOwnerId(Long ownerId);
    Optional<Item> findByName(String name);
}