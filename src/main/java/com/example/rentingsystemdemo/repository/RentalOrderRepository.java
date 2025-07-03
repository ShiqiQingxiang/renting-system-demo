package com.example.rentingsystemdemo.repository;

import com.example.rentingsystemdemo.model.RentalOrder;
import com.example.rentingsystemdemo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RentalOrderRepository extends JpaRepository<RentalOrder, Long> {
    List<RentalOrder> findByRenter(User renter);
    List<RentalOrder> findByItemOwner(User owner);
}