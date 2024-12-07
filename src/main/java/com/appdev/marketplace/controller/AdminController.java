package com.appdev.marketplace.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.appdev.marketplace.dto.ChangePassword;
import com.appdev.marketplace.dto.Login;
import com.appdev.marketplace.entity.AdminEntity;
import com.appdev.marketplace.entity.ProductEntity;
import com.appdev.marketplace.entity.SellerEntity;
import com.appdev.marketplace.service.AdminService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping(method = RequestMethod.GET,path="/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;
    
    @PostMapping("login")
	public ResponseEntity<Map<String, String>> login(@RequestBody Login loginRequest) {
		String username = loginRequest.getUsername();
		String password = loginRequest.getPassword();
			
		AdminEntity admin = adminService.authenticateAdmin(username, password);
		if(admin != null) {
			Map<String, String> response = new HashMap<>();
			response.put("message", "Login Successful");
			response.put("username", admin.getUsername());
			response.put("password", admin.getPassword());
			response.put("firstName", admin.getFirstname());
			response.put("lastName", admin.getLastname());
			response.put("contactNo", admin.getContactNo());
			response.put("email", admin.getEmail());
			return ResponseEntity.ok(response); 
			} else 
				return ResponseEntity.status(401).body(null);
	}

    @GetMapping("/products")
    public List<ProductEntity> viewAllProducts() {
        return adminService.viewAllProducts();
    }

    @PostMapping("/products")
    public String createProduct(@RequestBody ProductEntity productEntity) {
        adminService.createProduct(productEntity);
        return "Product created successfully.";
    }

    @GetMapping("/products/{code}")
    public ProductEntity getProductByCode(@PathVariable int code) {
        return adminService.getProductByCode(code);
    }

    @PutMapping("/products/{code}")
    public ProductEntity updateProduct(@PathVariable int code, @RequestBody ProductEntity updatedProduct) {
        return adminService.updateProduct(code, updatedProduct);
    }

    @DeleteMapping("/products/{code}")
    public String deleteProduct(@PathVariable int code) {
        return adminService.deleteProduct(code);
    }
    
    
 // Seller Management Endpoints

    @PostMapping("/sellers")
    public ResponseEntity<String> createSeller(@RequestBody SellerEntity seller) {
        try {
            adminService.createSeller(seller);
            return new ResponseEntity<>("Seller created successfully.", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/sellers")
    public ResponseEntity<?> getAllSellers() {
        try {
            List<SellerEntity> sellers = adminService.getAllSellers();
            return new ResponseEntity<>(sellers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/sellers/{username}")
    public ResponseEntity<?> getSellerByUsername(@PathVariable String username) {
        try {
            SellerEntity seller = adminService.getSellerByUsername(username);
            return new ResponseEntity<>(seller, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/sellers/{username}")
    public ResponseEntity<?> updateSeller(@PathVariable String username, @RequestBody SellerEntity updatedDetails) {
        try {
            SellerEntity updatedSeller = adminService.updateSellerDetails(username, updatedDetails);
            return new ResponseEntity<>(updatedSeller, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/sellers/{username}")
    public ResponseEntity<String> deleteSeller(@PathVariable String username) {
        try {
            String message = adminService.deleteSeller(username);
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/sellers/{username}/reset-password")
    public ResponseEntity<?> resetSellerPassword(@PathVariable String username, @RequestBody ChangePassword passwordRequest) {
        try {
            SellerEntity updatedSeller = adminService.resetSellerPassword(username, passwordRequest);
            return new ResponseEntity<>(updatedSeller, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/products-with-sellers")
    public ResponseEntity<List<Map<String, Object>>> getProductsWithSellers() {
        return ResponseEntity.ok(adminService.getAllProductsWithSellers());
    }
}
