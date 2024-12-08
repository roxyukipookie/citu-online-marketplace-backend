package com.appdev.marketplace.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.appdev.marketplace.dto.ChangePassword;
import com.appdev.marketplace.dto.Login;
import com.appdev.marketplace.entity.AdminEntity;
import com.appdev.marketplace.entity.ProductEntity;
import com.appdev.marketplace.entity.SellerEntity;
import com.appdev.marketplace.service.AdminService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NameNotFoundException;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping(method = RequestMethod.GET,path="/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;
    
	//private static final String UPLOAD_DIR = "C:/Users/Lloyd/Downloads/uploads"; // path to save the images
	private static final String UPLOAD_DIR = "C:/Users/chriz/Downloads/";

	@PostMapping("/uploadProfilePhoto/{username}")
	public ResponseEntity<Map<String,String>> uploadProfilePhoto(@PathVariable String username, @RequestParam("file") MultipartFile file) throws NameNotFoundException {
		try {
			if(file.isEmpty()) 
				return ResponseEntity.badRequest().body(Map.of("message", "No file selected"));
			
			//Getting the filename
			String fileName = StringUtils.cleanPath(file.getOriginalFilename());
			//Save image to target location
			Path targetLocation = Paths.get(UPLOAD_DIR, fileName);
	        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
			
	        //Save only the filename to the database
			AdminEntity admin = adminService.getAdminByUsername(username);
			admin.setProfilePhoto(fileName); //stores the filename only
			adminService.putAdminDetails(username, admin);
			
			Map<String, String> response = new HashMap<>();
			response.put("message", "Profile photo uploaded successfully");
			response.put("fileName", fileName);
			return ResponseEntity.ok(response);
		} catch(IOException e) {
			return ResponseEntity.status(500).body(Map.of("message", "Failed to upload the file"));
		}
	}
	
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
			response.put("firstName", admin.getFirstName());
			response.put("lastName", admin.getLastName());
			response.put("contactNo", admin.getContactNo());
			response.put("email", admin.getEmail());
			return ResponseEntity.ok(response); 
			} else 
				return ResponseEntity.status(401).body(null);
	}
    
    @GetMapping("/getAdminRecord/{username}") 
	public AdminEntity getAdminByUsername(@PathVariable String username) throws NameNotFoundException {
		return adminService.getAdminByUsername(username);
	}
    
    @PutMapping("/putAdminRecord/{username}")
	public AdminEntity putAdminRecord(@PathVariable String username, @RequestBody AdminEntity newAdminDetails) throws NameNotFoundException {
		return adminService.putAdminDetails(username, newAdminDetails);
	}
    
  	@DeleteMapping("/deleteAdminRecord/{username}")
  	public String deleteAdmin(@PathVariable String username) {
  		return adminService.deleteAdmin(username);
  	}
  	

  	@GetMapping("/products")
    public List<ProductEntity> viewAllProducts() {
        return adminService.viewAllProducts();
    }

  	@PostMapping("/addproducts")
    public String createProduct(@RequestBody ProductEntity productEntity) {
        adminService.createProduct(productEntity);
        return "Product created successfully.";
    }

  	@GetMapping("/products/{code}")
    public ProductEntity getProductByCode(@PathVariable int code) {
        return adminService.getProductByCode(code);
    }

  	@PutMapping("/editproducts/{code}")
    public ProductEntity updateProduct(@PathVariable int code, @RequestBody ProductEntity updatedProduct) {
        return adminService.updateProduct(code, updatedProduct);
    }

    @DeleteMapping("/deleteproducts/{code}")
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