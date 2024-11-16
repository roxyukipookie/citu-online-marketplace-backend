package com.appdev.marketplace.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.appdev.marketplace.entity.ProductEntity;

import jakarta.transaction.Transactional;

@Repository
public interface ProductRepo extends JpaRepository<ProductEntity, Integer>{
	public ProductEntity findByName(String name);
	public Optional<ProductEntity> findByCode(int code);
	
    //public void deleteByCode(int code);
	
    @Modifying
    @Transactional  
    void deleteByCode(int code);  
    
    //find by seller
    List<ProductEntity> findBySellerUsername(String sellerUsername);
	
}