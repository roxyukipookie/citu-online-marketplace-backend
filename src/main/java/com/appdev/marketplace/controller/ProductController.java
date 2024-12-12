package com.appdev.marketplace.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.RequestBody;

import com.appdev.marketplace.entity.ProductEntity;
import com.appdev.marketplace.service.AdminService;
import com.appdev.marketplace.service.ProductService;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping(method = RequestMethod.GET, path = "/api/product")
public class ProductController {

	@Autowired
	ProductService pserv;
	@Autowired
    private AdminService adminService;

	private static final String UPLOAD_DIR = "C:/Users/Lloyd/Downloads/"; // C:/Users/chriz/Downloads/
	//private static final String UPLOAD_DIR = "C:/Users/chriz/Downloads/";
	
	@GetMapping("/pendingApproval")
    public List<Map<String, Object>> getPendingApprovalProducts() {
        return adminService.getAllProductsWithSellers();
    }
	
	// get products by logged in seller
	@GetMapping("/getProductsBySeller/{username}")
	public ResponseEntity<List<Map<String, Object>>> getProductsBySeller(@PathVariable String username) {
		try {
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
				System.out.println("No products found for seller: " + username);
				return ResponseEntity.noContent().build();
			}

			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (NoSuchElementException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		} catch (Exception ex) {
			System.err.println("An error occurred while retrieving products: " + ex.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
	
	@GetMapping("/getSellerByProductCode/{code}")
    public ResponseEntity<Map<String, String>> getSellerByProductCode(@PathVariable int code) {
        try {
            // Fetch the product by code
            ProductEntity product = pserv.getProductByCode(code);
            
            if (product == null || product.getSeller() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Product or Seller not found"));
            }

            // Get seller's username
            String sellerUsername = product.getSeller().getUsername();

            // Return seller username as JSON response
            Map<String, String> response = Map.of("sellerUsername", sellerUsername);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error fetching seller username: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "An error occurred"));
        }
    }

	// fetches only the products where the seller's username does not match the
	// logged-in seller's username
	@GetMapping("/getAllProducts/{username}")
	public ResponseEntity<List<Map<String, Object>>> getAllProducts(@PathVariable String username) {
		try {
			List<ProductEntity> products = pserv.getAllProducts(username);

			List<Map<String, Object>> response = new ArrayList<>();
			for (ProductEntity product : products) {
				Map<String, Object> productData = new HashMap<>();
				productData.put("code", product.getCode());
				productData.put("name", product.getName());
				productData.put("status", product.getStatus());
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
		} catch (NoSuchElementException ex) {
			System.err.println("No products found for seller: " + username);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		} catch (Exception ex) {
			System.err.println("An error occurred while retrieving products: " + ex.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	// Filtering Products
	@GetMapping("/getAllProductsFilter/{username}")
	public ResponseEntity<List<Map<String, Object>>> getAllProductsFilter(@PathVariable String username,
			@RequestParam(required = false) String category, @RequestParam(required = false) String status,
			@RequestParam(required = false) String conditionType) {
		// Fetch products based on filters (excluding username as a filter)
		List<ProductEntity> products = pserv.getFilteredProducts(category, status, conditionType);

		// Build a custom response including seller's username
		List<Map<String, Object>> response = products.stream().map(product -> {
			Map<String, Object> productData = new HashMap<>();
			productData.put("code", product.getCode());
			productData.put("name", product.getName());
			productData.put("pdtDescription", product.getPdtDescription());
			productData.put("buyPrice", product.getBuyPrice());
			productData.put("imagePath", product.getImagePath());

			// Include seller's username if available
			Optional.ofNullable(product.getSeller())
					.ifPresent(seller -> productData.put("sellerUsername", seller.getUsername()));

			return productData;
		}).collect(Collectors.toList());

		// Return response
		return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
	}

	@PostMapping("/postproduct")
	public ResponseEntity<String> postProduct(@RequestParam("name") String name,
			@RequestParam("pdtDescription") String description, @RequestParam("qtyInStock") int quantity,
			@RequestParam("buyPrice") float price, @RequestParam("image") MultipartFile image,
			@RequestParam("category") String category, @RequestParam("status") String status,
			@RequestParam("conditionType") String conditionType,
			@RequestParam("seller_username") String sellerUsername) { // Accept seller username

		// Save the image
		if (image.isEmpty()) {
			return new ResponseEntity<>("Image file not found!", HttpStatus.BAD_REQUEST);
		}
		File uploadDir = new File(UPLOAD_DIR + "uploads");
		if (!uploadDir.exists())
			uploadDir.mkdirs();
		String imagePath = "uploads/" + image.getOriginalFilename();
		try {
			Files.copy(image.getInputStream(), Paths.get(UPLOAD_DIR + imagePath), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			return new ResponseEntity<>("Error saving image!", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// Call the service method to save product with associated seller
		try {
			pserv.postProduct(name, description, quantity, price, imagePath, category, status, conditionType,
					sellerUsername);
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

	@PutMapping("/putProductDetails/{code}")
	public ProductEntity putProductDetails(@PathVariable int code,
			@RequestPart("product") ProductEntity newProductEntity,
			@RequestPart(value = "imagePath", required = false) MultipartFile imageFile) {
		// Check if an image file is provided
		if (imageFile != null && !imageFile.isEmpty()) {
			try {
				// Define the directory and file path where the image will be saved
				String uploadDir = "uploads/";
				String fileName = imageFile.getOriginalFilename();
				Path filePath = Paths.get(uploadDir, fileName);

				// Ensure the directory exists
				Files.createDirectories(filePath.getParent());

				// Save the file to the specified path
				Files.write(filePath, imageFile.getBytes());

				// Set the image path in newProductEntity
				newProductEntity.setImagePath(filePath.toString());

				System.out.println("Image file saved: " + filePath);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Failed to save the image file.");
			}
		}

		// Call the service to update other product details
		return pserv.putProductDetails(code, newProductEntity);
	}

	// Delete of CRUD
	@DeleteMapping("deleteProduct/{code}")
	public String deleteProduc(@PathVariable int code) {
		return pserv.deleteProduct(code);
	}
	
	@PostMapping("/approve")
    public ResponseEntity<String> approveProduct(@RequestBody Map<String, Integer> request) {
        int productCode = request.get("productCode");
        try {
            pserv.approveProduct(productCode);  // Call the service to approve the product
            return ResponseEntity.ok("Product approved successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to approve product.");
        }
    }
	
	// Endpoint to get approved products
    @GetMapping("/approved")
    public List<ProductEntity> getApprovedProducts() {
        return pserv.getApprovedProducts();
    }
	
	// Reject Product
    @PostMapping("/reject")
    public ResponseEntity<String> rejectProduct(@RequestBody Map<String, Integer> request) {
        int productCode = request.get("productCode");
        try {
            pserv.rejectProduct(productCode);  // Call the service to reject the product
            return ResponseEntity.ok("Product rejected successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to reject product.");
        }
    }
}
