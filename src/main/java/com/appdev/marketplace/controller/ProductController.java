package com.appdev.marketplace.controller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

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
//import org.springframework.web.bind.annotation.RequestPart;
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
	
	private static final String UPLOAD_DIR = "C:/Users/chriz/Downloads/";
	
	@GetMapping("/print")
	public String print() {
		return "Mic Test 1 2 3, Check Mic Test";
	}
	
	
	//Create of CRUD
	/*@PostMapping("/postproduct")
	public ProductEntity postProduct(@RequestBody ProductEntity productentity) {
		return pserv.postProduct(productentity);
	}*/
	
	@PostMapping("/postproduct")
	public ResponseEntity<String> postProduct(
	        @RequestParam("name") String name,
	        @RequestParam("pdtDescription") String description,
	        @RequestParam("qtyInStock") int quantity,
	        @RequestParam("buyPrice") float price,
	        @RequestParam("image") MultipartFile image) {
	    
	    if (image.isEmpty()) {
	        return new ResponseEntity<>("Image file not found!", HttpStatus.BAD_REQUEST);
	    }

	    File uploadDir = new File(UPLOAD_DIR + "uploads");
	    if (!uploadDir.exists()) {
	        uploadDir.mkdirs();
	    }

	    String imagePath = "uploads/" + image.getOriginalFilename(); 

	    try {
	        Files.copy(image.getInputStream(), Paths.get(UPLOAD_DIR + imagePath), StandardCopyOption.REPLACE_EXISTING);
	    } catch (IOException e) {
	        return new ResponseEntity<>("Error saving image!", HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	    pserv.postProduct(name, description, quantity, price, imagePath); 

	    return new ResponseEntity<>("Product added with image path successfully", HttpStatus.OK);
	}

	
	//Read of CRUD
	@GetMapping("/getAllProducts")
	public List<ProductEntity> getAllProducts(){
		List<ProductEntity> products = pserv.getAllProducts();
	    return products;
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

