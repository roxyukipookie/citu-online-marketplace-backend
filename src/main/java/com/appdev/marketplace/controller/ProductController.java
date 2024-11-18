package com.appdev.marketplace.controller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PutMapping;

import com.appdev.marketplace.entity.ProductEntity;
import com.appdev.marketplace.service.ProductService;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping(method = RequestMethod.GET,path="/api/product")
public class ProductController {
	
	@Autowired
	ProductService pserv;
	
	private static final String UPLOAD_DIR = "C:\\\\Users\\\\Lloyd\\\\Downloads"; //C:/Users/chriz/Downloads/
	
	@GetMapping("/print")
	public String print() {
		return "Mic Test 1 2 3, Check Mic Test";
	}
	
	//get products by logged in seller
	@GetMapping("/getProductsBySeller/{username}")
	public ResponseEntity<List<Map<String, Object>>> getProductsBySeller(@PathVariable String username) {
	    List<ProductEntity> products = pserv.getProductsBySeller(username);
	    
	    List<Map<String, Object>> response = new ArrayList<>();
	    for (ProductEntity product : products) {
			Map<String, Object> productData = new HashMap<>();
		    productData.put("code", product.getCode());
		    productData.put("name", product.getName());
		    productData.put("qtyInStock", product.getQtyInStock());
		    productData.put("pdtDescription", product.getPdtDescription());
		    productData.put("buyPrice", product.getBuyPrice());
		    productData.put("imagePath", product.getImagePath());
		        
		    // Get seller's username
		    if (product.getSeller() != null) {
		    	productData.put("sellerUsername", product.getSeller().getUsername());
		    }

		    response.add(productData);
		}
		    
		if (response.isEmpty()) {
			return ResponseEntity.noContent().build();
		}

		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	//fetches only the products where the seller's username does not match the logged-in seller's username 
	@GetMapping("/getAllProducts/{username}")
	public ResponseEntity<List<Map<String, Object>>> getAllProducts(@PathVariable String username) {
		List<ProductEntity> products = pserv.getAllProducts(username);
		    
		List<Map<String, Object>> response = new ArrayList<>();
		for (ProductEntity product : products) {
			Map<String, Object> productData = new HashMap<>();
		    productData.put("code", product.getCode());
		    productData.put("name", product.getName());
		    productData.put("pdtDescription", product.getPdtDescription());
		    productData.put("buyPrice", product.getBuyPrice());
		    productData.put("imagePath", product.getImagePath());
		        
		    // Get seller's username
		    if (product.getSeller() != null) {
		    	productData.put("sellerUsername", product.getSeller().getUsername());
		    }

		    response.add(productData);
		}
		    
		if (response.isEmpty()) {
			return ResponseEntity.noContent().build();
		}

		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	 @GetMapping("/getAllProductsFilter/{username}")
	    public List<ProductEntity> getAllProductsFilter(
	        @PathVariable String username,
	        @RequestParam(required = false) String category,
	        @RequestParam(required = false) String status,
	        @RequestParam(required = false) String conditionType
	    ) {
	        return pserv.getFilteredProducts(category, status, conditionType);
	    }
	
	@PostMapping("/postproduct")
	public ResponseEntity<String> postProduct(
			@RequestParam("name") String name,
            @RequestParam("pdtDescription") String description,
            @RequestParam("qtyInStock") int quantity,
            @RequestParam("buyPrice") float price,
            @RequestParam("image") MultipartFile image,
            @RequestParam("category") String category,
            @RequestParam("status") String status,
            @RequestParam("conditionType") String conditionType,
            @RequestParam("seller_username") String sellerUsername) {  // Accept seller username
		
		// Save the image
        if (image.isEmpty()) {
            return new ResponseEntity<>("Image file not found!", HttpStatus.BAD_REQUEST);
        }
        File uploadDir = new File(UPLOAD_DIR + "uploads");
        if (!uploadDir.exists()) uploadDir.mkdirs();
        String imagePath = "uploads/" + image.getOriginalFilename();
        try {
            Files.copy(image.getInputStream(), Paths.get(UPLOAD_DIR + imagePath), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            return new ResponseEntity<>("Error saving image!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        // Call the service method to save product with associated seller
        try {
            pserv.postProduct(name, description, quantity, price, imagePath, category, status, conditionType, sellerUsername);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>("Product added successfully with image path", HttpStatus.OK);
    }

	@GetMapping("/getProductByCode/{code}")
    public ResponseEntity<ProductEntity> getProductByCode(@PathVariable int code) {
        ProductEntity product = pserv.getProductByCode(code);
        if (product != null) {
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.notFound().build(); 
        }
    }
	
	//Update of CRUD
	/*@PutMapping("/putProductDetails/{code}")
	public ProductEntity putProductDetails(@PathVariable  int code, @RequestBody ProductEntity newProductEntity) {
		return pserv.putProductDetails(code, newProductEntity);
	}*/
	
	@PutMapping("/putProductDetails/{code}")
	public ProductEntity putProductDetails(
	    @PathVariable int code,
	    @RequestPart("product") ProductEntity newProductEntity, 
	    @RequestPart(value = "imagePath", required = false) MultipartFile imageFile 
	) {
	    // Handle image file
	    if (imageFile != null && !imageFile.isEmpty()) {
	        String fileName = imageFile.getOriginalFilename();
	        System.out.println("Image file received: " + fileName);
	    }

	    // Handle updating the product entity
	    return pserv.putProductDetails(code, newProductEntity);
	}

	
	//Delete of CRUD
	@DeleteMapping("deleteProduct/{code}")
	public String deleteProduc(@PathVariable int code) {
		return pserv.deleteProduct(code);
	}
}

