package com.appdev.marketplace.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.appdev.marketplace.entity.ProductEntity;
import com.appdev.marketplace.entity.SellerEntity;
import com.appdev.marketplace.repository.ProductRepo;
import com.appdev.marketplace.repository.SellerRepository;
import com.appdev.marketplace.specifications.ProductSpecifications;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.naming.NameNotFoundException;
 
@Service
@Transactional
public class ProductService {
	
	@Autowired
	ProductRepo prepo;
	
	@Autowired
    private SellerRepository sellerRepo;
	
	public ProductService() {
		super();
	}
	
	//get products by logged in seller
	public List<ProductEntity> getProductsBySeller(String sellerUsername) {
	    return prepo.findBySellerUsername(sellerUsername);
	}
	
	//fetches only the products where the seller's username does not match the logged-in seller's username 
    public List<ProductEntity> getAllProducts(String username) {
        List<ProductEntity> products = prepo.findBySellerUsernameNot(username);
        return products;
    }
	
	// Create a new product and associate it with a seller
    public void postProduct(String name, String pdtDescription, int qtyInStock, float buyPrice, 
                            String imagePath, String category, String status, String conditionType, 
                            String sellerUsername) throws NoSuchElementException {
        
        // Find the seller by username
        Optional<SellerEntity> sellerOpt = sellerRepo.findById(sellerUsername);
        if (sellerOpt.isEmpty()) {
            throw new NoSuchElementException("Seller with username " + sellerUsername + " not found");
        }
        
        // Create a new product and associate it with the seller
        ProductEntity productentity = new ProductEntity();
        productentity.setName(name);
        productentity.setPdtDescription(pdtDescription);
        productentity.setQtyInStock(qtyInStock);
        productentity.setBuyPrice(buyPrice);
        productentity.setImagePath(imagePath);  
        productentity.setCategory(category);
        productentity.setStatus(status);
        productentity.setConditionType(conditionType);
        productentity.setSeller(sellerOpt.get());  // Associate with seller

        prepo.save(productentity);  
    }
    
	public ProductEntity getProductByCode(int code) {
        Optional<ProductEntity> product = prepo.findById(code);
        return product.orElse(null); 
    }
	
	public List<ProductEntity> getFilteredProducts(String category, String status, String conditionType) {
        Specification<ProductEntity> spec = Specification
                .where(ProductSpecifications.hasCategory(category))
                .and(ProductSpecifications.hasStatus(status))
                .and(ProductSpecifications.hasConditionType(conditionType));
        
        return prepo.findAll(spec);
    }
	
	//Update of CRUD
	@SuppressWarnings("finally")
	public ProductEntity putProductDetails(int code, ProductEntity newProductEntity) {
		ProductEntity productentity = new ProductEntity();
		try {
			productentity = prepo.findByCode(code).get();
			
			productentity.setName(newProductEntity.getName());
			productentity.setPdtDescription(newProductEntity.getPdtDescription());
			productentity.setQtyInStock(newProductEntity.getQtyInStock());
			productentity.setBuyPrice(newProductEntity.getBuyPrice());
			productentity.setCategory(newProductEntity.getCategory());
			productentity.setStatus(newProductEntity.getStatus());
			productentity.setConditionType(newProductEntity.getConditionType());
			
		}catch(NoSuchElementException ex) {
			throw new NameNotFoundException("Product "+code+" not found!");
		}finally {
			return prepo.save(productentity);
		}
	}
	
	//Delete of CRUD
	public String deleteProduct(int code) {
		String msg ="";
		if(prepo.findByCode(code)!=null) {
			prepo.deleteByCode(code);
			msg ="Product has been successfully deleted";
		}else {
			msg = code+"NOT found!";
		}
		return msg;
	}
}