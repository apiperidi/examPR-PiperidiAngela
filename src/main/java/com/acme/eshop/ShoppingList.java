package com.acme.eshop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ShoppingList {
    private static final Logger loggerAng = LoggerFactory.getLogger(ShoppingList.class);

    List<ShoppingItem> list = new ArrayList<ShoppingItem>();

    public void clearList(){
        list.clear();
    }
    //Add a new ShoppingItem to the list

    public void addItem()
    {
        System.out.println();
        System.out.println("Please enter in the name of your Product (Product1-10)");
        Scanner keyboard = new Scanner(System.in);
        String ItemName = keyboard.nextLine();

        System.out.println("Please enter in the price of your item");
        double ItemPrice = keyboard.nextDouble();

        System.out.println("Please enter, in the Qty of your item");
        int ItemQty = keyboard.nextInt();

        ShoppingItem Item = new ShoppingItem(ItemName, ItemPrice,
                ItemQty);
        list.add(Item);
        loggerAng.info("Product Added");

    }

    public void removeItem()
    {
        if (list.size()>0) {
            System.out.println();
            System.out.println("Please enter in, the Description of your Product to be removed (Product1-10)");
            Scanner keyboard = new Scanner(System.in);
            String ItemName = keyboard.nextLine();

            List<ShoppingItem> toRemove = new ArrayList<ShoppingItem>();
            for (ShoppingItem a : list) {
                if (a.getItemName().equalsIgnoreCase(ItemName)) {
                    toRemove.add(a);
                }
            }
            list.removeAll(toRemove);
            loggerAng.info("Product Name {} Removed.", ItemName);
        }
        else {
            loggerAng.info("The List is empty");}
    }

    //Display list and total number of items.
    public void displayItem(){
        if (list.size()>0)
        {
            loggerAng.info("List Size {}.",list.size());
        for (ShoppingItem x : list) {
           // System.out.println(x.toString());
            loggerAng.info(" {}.",x.toString());
        }}
        else {
            loggerAng.info("The List is empty");}

    }

    public Integer listSize()
    {
        return list.size();

    }
    public String displayItemName(Integer index){
      return  list.get(index).getItemName();
    }
    public Double displayTotalPrice(Integer index){
        return  list.get(index).getItemTotalPrice();
    }
    public Integer displayQuantity(Integer index){
        return  list.get(index).getItemQty();
    }
}
