package com.appdev.marketplace.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.appdev.marketplace.entity.SellerEntity;

@Repository
public interface SellerRepository extends JpaRepository<SellerEntity, String> {

}
