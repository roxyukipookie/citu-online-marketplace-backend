package com.appdev.marketplace.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.NoSuchElementException;

import javax.naming.NameNotFoundException;

@Hidden  // Hides the entire controller from Swagger Documentation
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping(method = RequestMethod.GET,path="/api/admin")
@Tag(name = "Admin API", description = "Endpoints for Admin Authentication and Product Management")
public class AdminController {

    @Autowired
    private AdminService adminService;
    
	private static final String UPLOAD_DIR = "C:/Users/Lloyd/Downloads/uploads"; // path to save the images
	//private static final String UPLOAD_DIR = "C:/Users/chriz/Downloads/";

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
    
    @PostMapping("/addAdmin")
    public ResponseEntity<Map<String, String>> addAdmin(@RequestBody AdminEntity adminEntity) {
        try {
            AdminEntity savedAdmin = adminService.addAdmin(adminEntity);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Admin added successfully");
            response.put("adminId", String.valueOf(savedAdmin.getId())); // assuming AdminEntity has an ID field
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to add admin: " + e.getMessage()));
        }
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
  	
 // New Endpoint to Get All Admins
    @GetMapping("/getAllAdmins")
    public List<AdminEntity> getAllAdmins() {
        return adminService.getAllAdmins();
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
    
    @DeleteMapping("/delete-products")
    public ResponseEntity<?> deleteProducts(@RequestBody List<Integer> productCodes) {
        try {
            int deletedCount = adminService.deleteProductsByCodes(productCodes);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Products deleted successfully.");
            response.put("deletedCount", deletedCount);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Seller Management Endpoints

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
    
    @PutMapping("/updateUserDetails/{role}/{username}")
    public ResponseEntity<?> updateUserDetails(
            @PathVariable String role,
            @PathVariable String username,
            @RequestBody Map<String, String> userDetails) {
        try {
            if ("admin".equalsIgnoreCase(role)) {
                AdminEntity admin = adminService.getAdminByUsername(username);

                // Update fields
                admin.setFirstName(userDetails.get("firstName"));
                admin.setLastName(userDetails.get("lastName"));
                admin.setEmail(userDetails.get("email"));
                admin.setContactNo(userDetails.get("contactNo"));

                // Save updated admin
                AdminEntity updatedAdmin = adminService.putAdminDetails(username, admin);
                return new ResponseEntity<>(updatedAdmin, HttpStatus.OK);
            } else if ("seller".equalsIgnoreCase(role)) {
                SellerEntity seller = adminService.getSellerByUsername(username);

                // Update fields
                seller.setFirstName(userDetails.get("firstName"));
                seller.setLastName(userDetails.get("lastName"));
                seller.setEmail(userDetails.get("email"));
                seller.setContactNo(userDetails.get("contactNo"));

                // Save updated seller
                SellerEntity updatedSeller = adminService.updateSellerDetails(username, seller);
                return new ResponseEntity<>(updatedSeller, HttpStatus.OK);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Invalid role. Use 'admin' or 'seller'."));
            }
        } catch (NameNotFoundException | NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/deleteUser/{role}/{username}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable String role, 
            @PathVariable String username) {
        try {
            String message = adminService.deleteUser(role, username);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (NameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @PutMapping("/changePassword/{username}")
	public AdminEntity updatePassword(@PathVariable String username, @RequestBody ChangePassword passwordRequest) throws NameNotFoundException {
		return adminService.updatePassword(username, passwordRequest);
	}
}