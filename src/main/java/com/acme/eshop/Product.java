package com.acme.eshop;

import java.sql.SQLException;
import java.sql.Statement;

import static java.lang.System.exit;

public class Product {

    private Integer ID;
    private String Code;
    private String Description;
    private Integer Quantity;


    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }
    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        Code = code;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public Integer getQuantity() {
        return Quantity;
    }

    public void setQuantity(Integer quantity) {
        Quantity = quantity;
    }

    public double getPrice() {
        return Price;
    }

    public void setPrice(double price) {
        Price = price;
    }

    private double Price;


   /* public Product(final Integer ID, final String Code, final String Description,final Integer Quantity,final double Price) {
        this.ID = ID;
        this.Code = Description;
        this.Description = Description;
        this.Quantity=Quantity;
       this.Price=Price;
    }*/


}
