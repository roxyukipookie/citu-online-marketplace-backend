package com.appdev.marketplace.request;

public class ProductRequest {
    private String name;
    private String pdtDescription;
    private int qtyInStock;
    private float buyPrice;
    private String imagePath;  // Image path as string, not an actual file

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPdtDescription() {
        return pdtDescription;
    }

    public void setPdtDescription(String pdtDescription) {
        this.pdtDescription = pdtDescription;
    }

    public int getQtyInStock() {
        return qtyInStock;
    }

    public void setQtyInStock(int qtyInStock) {
        this.qtyInStock = qtyInStock;
    }

    public float getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(float buyPrice) {
        this.buyPrice = buyPrice;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
