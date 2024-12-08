package com.appdev.marketplace.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.appdev.marketplace.entity.ProductEntity;

import jakarta.transaction.Transactional;

@Repository
public interface ProductRepo extends JpaRepository<ProductEntity, Integer>{
	public ProductEntity findByName(String name);
	public Optional<ProductEntity> findByCode(int code);
	List<ProductEntity> findByStatus(String status);
    //public void deleteByCode(int code);
	
    @Modifying
    @Transactional  
    void deleteByCode(int code);  
    
    public List<ProductEntity> findBySellerUsername(String sellerUsername);
    public List<ProductEntity> findBySellerUsernameNot(String username);
    public List<ProductEntity> findAll(Specification<ProductEntity> spec);
    
    @Query("SELECT p FROM ProductEntity p JOIN p.seller s")
    List<ProductEntity> findAllProductsWithSellers();
}