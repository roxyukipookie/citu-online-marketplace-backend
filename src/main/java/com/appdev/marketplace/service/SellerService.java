package com.appdev.marketplace.service;

import java.util.List;
import java.util.NoSuchElementException;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.appdev.marketplace.entity.SellerEntity;
import com.appdev.marketplace.repository.SellerRepository;

@Service
public class SellerService {
	@Autowired
	private SellerRepository sellerRepo;

	public SellerService() {
		super();
	}

	//CREATE
	public SellerEntity postSellerRecord(SellerEntity seller) throws NameAlreadyBoundException {
		if(sellerRepo.existsById(seller.getUsername())) {
			throw new NameAlreadyBoundException("Username " + seller.getUsername() + " is already taken. Input another username.");
		}
		return sellerRepo.save(seller);
	}
	
	//READ
	public List<SellerEntity> getAllSellers() {
		return sellerRepo.findAll();
	}
	
	//UPDATE
	public SellerEntity putSellerDetails(String username, SellerEntity newSellerDetails) throws NameNotFoundException {
	    SellerEntity seller = sellerRepo.findById(username)
	        .orElseThrow(() -> new NameNotFoundException("Seller with username: " + username + " does not exist"));

	    // Update fields with values from newSellerDetails
	    if (newSellerDetails.getPassword() != null) {
	        seller.setPassword(newSellerDetails.getPassword()); // Update password if it's not null
	    }
	    seller.setFirstName(newSellerDetails.getFirstName());
	    seller.setLastName(newSellerDetails.getLastName());
	    seller.setAddress(newSellerDetails.getAddress());
	    seller.setContactNo(newSellerDetails.getContactNo());
	    seller.setEmail(newSellerDetails.getEmail());

	    // Save and return the updated seller
	    return sellerRepo.save(seller);
	}
	
	//DELETE
	public String deleteSeller(String username) {
		String msg = "";
		if(sellerRepo.findById(username) != null) {
			sellerRepo.deleteById(username);
			msg = "Seller " + username + " successfully deleted";
		} else {
			msg = "Seller with username: " + username + " is not found";
		}
		
		return msg;
	}
	
	public SellerEntity authenticateSeller(String username, String password) {
		System.out.println("Attempting to authenticate user: " + username);
		SellerEntity seller = sellerRepo.findById(username).get(); //search user by username
		
		if (seller == null) {
	        System.out.println("Seller not found. Please register.");
	        throw new NoSuchElementException("Seller not found. Please register.");
	    }
		
		System.out.println("Retrieved seller: " + seller.getUsername() + ", Password: " + seller.getPassword());
		
		if (seller.getPassword().equals(password)) {
	        return seller; // Authentication successful
	    } else {
	        System.out.println("Password does not match.");
	        throw new RuntimeException("Invalid password");
	    }
		
		/*if(seller != null && seller.getPassword().equals(password)) {
			return seller; //return seller entity when authentication is successful
		} else {
			return null;
		}*/
	}
}
