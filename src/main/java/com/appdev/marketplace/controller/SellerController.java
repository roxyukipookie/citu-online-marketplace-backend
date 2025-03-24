package com.appdev.marketplace.controller;

	
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.appdev.marketplace.dto.ChangePassword;
import com.appdev.marketplace.dto.Login;
import com.appdev.marketplace.entity.SellerEntity;
import com.appdev.marketplace.service.SellerService;
	
@RestController
@RequestMapping("/api/seller")
@CrossOrigin(origins = "http://localhost:3000")  // Allow CORS from React app
@Tag(name = "Seller API", description = "Endpoints for Seller Authentication and Management")
public class SellerController {
	@Autowired
	private SellerService sellerService;
	
	private static final String UPLOAD_DIR = "C:/Users/Lloyd/Downloads/uploads"; // path to save the images
	//private static final String UPLOAD_DIR = "C:/Users/chriz/Downloads/"; // path to save the images


	@Operation(summary = "Create a New Seller", description = "Registers a new seller in the marketplace.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Seller created successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid input or missing fields"),
			@ApiResponse(responseCode = "409", description = "Seller already exists"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	//CREATE
	@PostMapping("/postSellerRecord")
	public ResponseEntity<?> postSellerRecord(@Parameter(description = "Seller entity containing required seller details", required = true, schema = @Schema(implementation = SellerEntity.class))
												  @RequestBody SellerEntity seller) throws NameAlreadyBoundException {
		if (seller.getUsername().isEmpty() || seller.getPassword().isEmpty() || seller.getFirstName().isEmpty() || seller.getLastName().isEmpty() || seller.getAddress().isEmpty() || seller.getContactNo().isEmpty() || seller.getEmail().isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "All fields are required"));
		} else if (seller.getPassword().length() < 8) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Password must be at least 8 characters long"));
	    }
		
		try {
	        SellerEntity savedSeller = sellerService.postSellerRecord(seller);
	        return ResponseEntity.ok(savedSeller);
	    } catch (NameAlreadyBoundException e) {
	        return ResponseEntity.status(HttpStatus.CONFLICT)
	                             .body(Map.of("message", e.getMessage()));
	    }
	}

	@Operation(summary = "Login Seller", description = "Authenticates a seller with username and password")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Login successful",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request"),
			@ApiResponse(responseCode = "401", description = "Unauthorized",
					content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	@PostMapping("login")
	public ResponseEntity<Map<String, String>> login(@RequestBody Login loginRequest) {
		String username = loginRequest.getUsername();
		String password = loginRequest.getPassword();
			
		SellerEntity seller = sellerService.authenticateSeller(username, password);
		if(seller != null) {
			Map<String, String> response = new HashMap<>();
			response.put("message", "Login Successful");
			response.put("username", seller.getUsername());
			response.put("password", seller.getPassword());
			response.put("firstName", seller.getFirstName());
			response.put("lastName", seller.getLastName());
			response.put("address", seller.getAddress());
			response.put("contactNo", seller.getContactNo());
			response.put("email", seller.getEmail());
			return ResponseEntity.ok(response); 
			} else 
				return ResponseEntity.status(401).body(null);
	}

	@Operation(summary = "Upload Profile Photo", description = "Uploads a profile photo for a specific seller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Photo uploaded successfully"),
			@ApiResponse(responseCode = "400", description = "No file selected"),
			@ApiResponse(responseCode = "500", description = "Failed to upload the file")
	})
	@PostMapping("/uploadProfilePhoto/{username}")
	public ResponseEntity<Map<String,String>> uploadProfilePhoto(
			@Parameter(description = "Seller's username") @PathVariable String username,
			@Parameter(description = "Profile photo file") @RequestParam("file") MultipartFile file) throws NameNotFoundException {
		try {
			if(file.isEmpty()) 
				return ResponseEntity.badRequest().body(Map.of("message", "No file selected"));
			
			//Getting the filename
			String fileName = StringUtils.cleanPath(file.getOriginalFilename());
			//Save image to target location
			Path targetLocation = Paths.get(UPLOAD_DIR, fileName);
	        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
			
	        //Save only the filename to the database
			SellerEntity seller = sellerService.getSellerByUsername(username);
			seller.setProfilePhoto(fileName); //stores the filename only
			sellerService.putSellerDetails(username, seller);
			
			Map<String, String> response = new HashMap<>();
			response.put("message", "Profile photo uploaded successfully");
			response.put("fileName", fileName);
			return ResponseEntity.ok(response);
		} catch(IOException e) {
			return ResponseEntity.status(500).body(Map.of("message", "Failed to upload the file"));
		}
	}

	@Operation(summary = "Get All Sellers", description = "Fetches all seller records")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "List of sellers retrieved successfully"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	//DISPLAY RECORD
	@GetMapping("/getSellerRecord")
	public List<SellerEntity> getAllSellers() {
		return sellerService.getAllSellers();
	}

	@Operation(summary = "Get Seller by Username", description = "Fetch a seller's details by username")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Seller record retrieved successfully"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	@GetMapping("/getSellerRecord/{username}") 
	public SellerEntity getSellerByUsername(@Parameter(description = "Seller's username") @PathVariable String username) throws NameNotFoundException {
		return sellerService.getSellerByUsername(username);
	}

	@Operation(summary = "Get a Seller's Username", description = "Fetch a seller's username")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Seller username retrieved successfully"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	@GetMapping("/getUsername/{username}")
    public String getSellerUsername(@Parameter(description = "Seller's username") @PathVariable String username) throws NameNotFoundException {
        SellerEntity seller = sellerService.getSellerByUsername(username);
        if (seller == null) {
            throw new NameNotFoundException("Seller with username " + username + " not found");
        }
        return seller.getUsername();
    }

	@Operation(summary = "Update Seller Details", description = "Updates a seller's information")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Seller record updated successfully"),
			@ApiResponse(responseCode = "404", description = "Seller not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	//UPDATE RECORD
	@PutMapping("/putSellerRecord/{username}")
	public SellerEntity putSellerRecord(@Parameter(description = "Seller's username") @PathVariable String username, @RequestBody SellerEntity newSellerDetails) throws NameNotFoundException {
		return sellerService.putSellerDetails(username, newSellerDetails);
	}

	@Operation(summary = "Update Seller Password", description = "Updates a seller's password")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Password updated successfully"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	@PutMapping("/changePassword/{username}")
	public SellerEntity updatePassword(@Parameter(description = "Seller's username") @PathVariable String username, @RequestBody ChangePassword passwordRequest) throws NameNotFoundException {
		return sellerService.updatePassword(username, passwordRequest);
	}

	@Operation(summary = "Delete Seller", description = "Deletes a seller by username")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Seller deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Seller not found")
	})
	//DELETE RECORD
	@DeleteMapping("/deleteSellerRecord/{username}")
	public String deleteSeller(@Parameter(description = "Seller's username") @PathVariable String username) {
		return sellerService.deleteSeller(username);
	}
}
