package com.appdev.marketplace.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.appdev.marketplace.entity.ProductEntity;
import com.appdev.marketplace.entity.SellerEntity;
import com.appdev.marketplace.repository.ProductRepo;
import com.appdev.marketplace.repository.SellerRepository;

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

	//Create of CRUD
	
	/*public ProductEntity postProduct(ProductEntity productentity) {
		return prepo.save(productentity);
	}*/
	//get products by seller
	public List<ProductEntity> getProductsBySeller(String sellerUsername) {
	    return prepo.findBySellerUsername(sellerUsername);
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
    
	//Read of CRUD 
	public List< ProductEntity> getAllProducts(){
		return prepo.findAll();
	}
	
	public ProductEntity getProductByCode(int code) {
        Optional<ProductEntity> product = prepo.findById(code);
        return product.orElse(null); 
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