package com.appdev.marketplace.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.appdev.marketplace.entity.ProductEntity;
import com.appdev.marketplace.repository.ProductRepo;

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
	
	public ProductService() {
		super();
		// TODO Auto-generated constructor stub
	}

	//Create of CRUD
	
	/*public ProductEntity postProduct(ProductEntity productentity) {
		return prepo.save(productentity);
	}*/
	
	public void postProduct(String name, String pdtDescription, int qtyInStock, float buyPrice, String imagePath, String category, String status, String conditionType) {
        // Create and save the product
        ProductEntity productentity = new ProductEntity();
        productentity.setName(name);
        productentity.setPdtDescription(pdtDescription);
        productentity.setQtyInStock(qtyInStock);
        productentity.setBuyPrice(buyPrice);
        productentity.setImagePath(imagePath);  // Store the image path
        productentity.setCategory(category);
        productentity.setStatus(status);
        productentity.setConditionType(conditionType);

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