package com.acme.eshop;

import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Customer {
    List<Customer> list = new ArrayList<Customer>();
    private final Lorem generator = LoremIpsum.getInstance(); // random names
    private String CustomerName;
    private String CustomerLatName;
    private String CustomerCategory;
    private Integer DiscountCategory;

    public Customer(String name, String lastname, String CusCateg) {
        this.CustomerName=name;
        this.CustomerLatName=lastname;
        this.CustomerCategory=CusCateg;
        this.DiscountCategory=getDiscount();
    }


    public void addCustomer(String Name,String LastName,String Category,int Dis)
    {
      this.CustomerName=Name;//
           //Customer Item = new Customer();
      //  Item.CustomerName=Name;
         /*  Item.CustomerCategory=EshopMain.CustomerCategory.ask().toString();
           Item.CustomerLatName=generator.getLastName();
           Item.CustomerName=generator.getFirstName();
           Item.DiscountCategory=getDiscount();

        list.add(Item);*/
        System.out.println("Customer Added");
    }



    public Integer getDiscount() {
        Integer Discount=0;
        if (CustomerCategory.equals("B2C")){Discount=1;}
        else  if (CustomerCategory.equals("B2B")){Discount=2;}
        else if (CustomerCategory.equals("B2G")){Discount=3;}
           else {Discount=0;}

        return Discount;
    }
    public Integer getDiscountCategory() {
        return DiscountCategory;
    }

    public void setDiscountCategory(Integer discountCategory) {
        DiscountCategory = discountCategory;
    }

    public String getCustomerName() {
        return CustomerName;
    }

    public void setCustomerName(String customerName) {
        CustomerName = customerName;
    }

    public String getCustomerLatName() {
        return CustomerLatName;
    }

    public void setCustomerLatName(String customerLatName) {
        CustomerLatName = customerLatName;
    }

    public String getCustomerCategory() {
        return CustomerCategory;
    }

    public void setCustomerCategory(String customerCategory) {
        CustomerCategory = customerCategory;
    }
}
