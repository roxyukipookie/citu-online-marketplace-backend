package com.appdev.marketplace.controller;
	
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.appdev.marketplace.dto.Login;
import com.appdev.marketplace.entity.SellerEntity;
import com.appdev.marketplace.service.SellerService;
	
@RestController
@RequestMapping("/api/seller")
@CrossOrigin(origins = "http://localhost:3000")  // Allow CORS from React app
public class SellerController {
	@Autowired
	private SellerService sellerService;
		
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
		
	//DISPLAY RECORD
	@GetMapping("/getSellerRecord")
	public List<SellerEntity> getAllSellers() {
		return sellerService.getAllSellers();
	}
		
	//UPDATE RECORD
	@PutMapping("/putSellerRecord/{username}")
	public SellerEntity putSellerRecord(@PathVariable String username, @RequestBody SellerEntity newSellerDetails) throws NameNotFoundException {
		return sellerService.putSellerDetails(username, newSellerDetails);
	}
		
	//DELETE RECORD
	@DeleteMapping("/deleteSellerRecord/{username}")
	public String deleteSeller(@PathVariable String username) {
		return sellerService.deleteSeller(username);
	}
}
