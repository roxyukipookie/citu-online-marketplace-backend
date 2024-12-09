package com.appdev.marketplace.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.appdev.marketplace.dto.ChangePassword;
import com.appdev.marketplace.entity.AdminEntity;
import com.appdev.marketplace.entity.ProductEntity;
import com.appdev.marketplace.entity.SellerEntity;
import com.appdev.marketplace.repository.AdminRepository;
import com.appdev.marketplace.repository.ProductRepo;
import com.appdev.marketplace.repository.SellerRepository;
import jakarta.transaction.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;

@Service
@Transactional
public class AdminService {

    @Autowired
    private AdminRepository adminRepo;

    @Autowired
    private ProductRepo productRepo;
    
    @Autowired
    private SellerRepository sellerRepo;
   
    public AdminEntity authenticateAdmin(String username, String password) {
        try {
            System.out.println("Attempting to authenticate admin: " + username);
            
            // Attempt to find the admin by username
            AdminEntity admin = adminRepo.findByUsername(username).orElseThrow(() -> {
                System.out.println("Admin not found. Please register.");
                return new NoSuchElementException("Admin not found. Please register.");
            });
            
            System.out.println("Retrieved admin: " + admin.getUsername() + ", Password: " + admin.getPassword());
            
            // Check if the password matches
            if (admin.getPassword().equals(password)) {
                return admin; // Authentication successful
            } else {
                System.out.println("Password does not match.");
                throw new RuntimeException("Invalid password");
            }
        } catch (NoSuchElementException e) {
            System.err.println("Error: " + e.getMessage());
            throw e; // Re-throwing the exception to propagate it if necessary
        } catch (RuntimeException e) {
            System.err.println("Authentication failed: " + e.getMessage());
            throw e; // Re-throwing the exception to propagate it if necessary
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            throw new RuntimeException("Unexpected error during authentication");
        }
    }
    
    //UPDATE
  	public AdminEntity putAdminDetails(String username, AdminEntity newAdminDetails) throws NameNotFoundException {
  	    AdminEntity admin = adminRepo.findByUsername(username)
  	        .orElseThrow(() -> new NameNotFoundException("Seller with username: " + username + " does not exist"));

  	    if (newAdminDetails.getProfilePhoto() != null) {
  	        System.out.println("Updated Profile Photo: " + newAdminDetails.getProfilePhoto()); // Add a log here
  	    }
  	    
  	    //seller.setProfilePhoto(newSellerDetails.getProfilePhoto());
  	    admin.setFirstName(newAdminDetails.getFirstName());
  	    admin.setLastName(newAdminDetails.getLastName());
  	    admin.setContactNo(newAdminDetails.getContactNo());
  	    admin.setEmail(newAdminDetails.getEmail());

  	    return adminRepo.save(admin);
  	}
  	

    // ========================= Admin Management =========================

    // Get all admins
    public List<AdminEntity> getAllAdmins() {
        return adminRepo.findAll();
    }

    // Get admin by username
    public AdminEntity getAdminByUsername(String username) {
        return adminRepo.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Admin with username " + username + " not found"));
    }

    // Create a new admin
    public AdminEntity createAdmin(AdminEntity adminEntity) {
        if (adminRepo.existsByUsername(adminEntity.getUsername())) {
            throw new IllegalArgumentException("Admin with username " + adminEntity.getUsername() + " already exists");
        }
        return adminRepo.save(adminEntity);
    }

    //DELETE ADMIN ACCOUNT
  	public String deleteAdmin(String username) {
  		String msg = "";
  		if(adminRepo.findByUsername(username) != null) {
  			adminRepo.deleteByUsername(username);
  			msg = "Admin " + username + " successfully deleted";
  		} else {
  			msg = "Admin with username: " + username + " is not found";
  		}
  		
  		return msg;
  	}

    // ========================= Product Management =========================

    // View all products
    public List<ProductEntity> viewAllProducts() {
        return productRepo.findAll();
    }
    
    // Retrieve all products with corresponding seller username
    public List<Map<String, Object>> getAllProductsWithSellers() {
        return productRepo.findAll().stream()
            .map(product -> {
                Map<String, Object> details = new HashMap<>();
                details.put("product", product);
                details.put("productName", product.getName());
                details.put("productCode", product.getCode());
                details.put("category", product.getCategory());
                details.put("status", product.getStatus());
                details.put("image", product.getImagePath());
                details.put("sellerUsername", product.getSeller() != null ? product.getSeller().getUsername() : "Unknown");
                return details;
            })
            .collect(Collectors.toList());
    }
    
    // Create a new product
    public ProductEntity createProduct(ProductEntity productEntity) {
        return productRepo.save(productEntity);
    }

    // Get product by code
    public ProductEntity getProductByCode(int code) {
        return productRepo.findById(code)
                .orElseThrow(() -> new NoSuchElementException("Product with code " + code + " not found"));
    }

    // Update an existing product
    public ProductEntity updateProduct(int code, ProductEntity updatedProduct) {
        ProductEntity existingProduct = productRepo.findById(code)
                .orElseThrow(() -> new NoSuchElementException("Product with code " + code + " not found"));

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setPdtDescription(updatedProduct.getPdtDescription());
        existingProduct.setQtyInStock(updatedProduct.getQtyInStock());
        existingProduct.setBuyPrice(updatedProduct.getBuyPrice());
        existingProduct.setCategory(updatedProduct.getCategory());
        existingProduct.setStatus(updatedProduct.getStatus());
        existingProduct.setConditionType(updatedProduct.getConditionType());
        existingProduct.setImagePath(updatedProduct.getImagePath());

        return productRepo.save(existingProduct);
    }

    // Delete a product by code
    public String deleteProduct(int code) {
        if (productRepo.existsById(code)) {
            productRepo.deleteById(code);
            return "Product with code " + code + " has been successfully deleted.";
        } else {
            throw new NoSuchElementException("Product with code " + code + " not found.");
        }
    }
    
 // ========================= Seller Management =========================

    // Admin creates a new seller
    public SellerEntity createSeller(SellerEntity seller) throws NameAlreadyBoundException {
        if (sellerRepo.existsById(seller.getUsername())) {
            throw new NameAlreadyBoundException("Username " + seller.getUsername() + " is already taken. Input another username.");
        }

        if (sellerRepo.existsByEmail(seller.getEmail())) {
            throw new NameAlreadyBoundException("Email already exists");
        }

        return sellerRepo.save(seller);
    }

    // Admin gets all sellers
    public List<SellerEntity> getAllSellers() {
        return sellerRepo.findAll();
    }

    // Admin gets seller by username
    public SellerEntity getSellerByUsername(String username) throws NameNotFoundException {
        return sellerRepo.findById(username)
                .orElseThrow(() -> new NameNotFoundException("Seller with username: " + username + " not found."));
    }

    // Admin updates seller details
    public SellerEntity updateSellerDetails(String username, SellerEntity newSellerDetails) throws NameNotFoundException {
        SellerEntity seller = sellerRepo.findById(username)
                .orElseThrow(() -> new NameNotFoundException("Seller with username: " + username + " does not exist"));

        // Updating fields
        seller.setFirstName(newSellerDetails.getFirstName());
        seller.setLastName(newSellerDetails.getLastName());
        seller.setAddress(newSellerDetails.getAddress());
        seller.setContactNo(newSellerDetails.getContactNo());
        seller.setEmail(newSellerDetails.getEmail());

        return sellerRepo.save(seller);
    }

    // Admin deletes a seller
    public String deleteSeller(String username) {
        if (sellerRepo.existsById(username)) {
            sellerRepo.deleteById(username);
            return "Seller " + username + " successfully deleted";
        } else {
            throw new NoSuchElementException("Seller with username: " + username + " is not found");
        }
    }

    // Admin resets a seller's password
    public SellerEntity resetSellerPassword(String username, ChangePassword passwordRequest) throws NameNotFoundException {
        SellerEntity seller = sellerRepo.findById(username)
                .orElseThrow(() -> new NameNotFoundException("Seller with username: " + username + " does not exist"));

        // Reset password logic
        seller.setPassword(passwordRequest.getNewPassword());
        return sellerRepo.save(seller);
    }
}

