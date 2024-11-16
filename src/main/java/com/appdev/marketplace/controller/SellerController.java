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

import org.springframework.beans.factory.annotation.Autowired;
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
public class SellerController {
	@Autowired
	private SellerService sellerService;
	
	private static final String UPLOAD_DIR = "C:/Users/Lloyd/Documents/Karen/profile-images"; // path to save the images
		
	//CREATE
	@PostMapping("/postSellerRecord")
	public SellerEntity postSellerRecord(@RequestBody SellerEntity seller) throws NameAlreadyBoundException {
		return sellerService.postSellerRecord(seller);
	}
		
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
		
	//DISPLAY RECORD
	@GetMapping("/getSellerRecord")
	public List<SellerEntity> getAllSellers() {
		return sellerService.getAllSellers();
	}
	
	@GetMapping("/getSellerRecord/{username}") 
	public SellerEntity getSellerByUsername(@PathVariable String username) throws NameNotFoundException {
		return sellerService.getSellerByUsername(username);
	}
		
	//UPDATE RECORD
	@PutMapping("/putSellerRecord/{username}")
	public SellerEntity putSellerRecord(@PathVariable String username, @RequestBody SellerEntity newSellerDetails) throws NameNotFoundException {
		return sellerService.putSellerDetails(username, newSellerDetails);
	}
	
	@PutMapping("/changePassword/{username}")
	public SellerEntity updatePassword(@PathVariable String username, @RequestBody ChangePassword passwordRequest) throws NameNotFoundException {
		return sellerService.updatePassword(username, passwordRequest);
	}
		
	//DELETE RECORD
	@DeleteMapping("/deleteSellerRecord/{username}")
	public String deleteSeller(@PathVariable String username) {
		return sellerService.deleteSeller(username);
	}
}
