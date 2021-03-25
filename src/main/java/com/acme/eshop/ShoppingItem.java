package com.acme.eshop;

public class ShoppingItem {
    private String ItemName;
    private double ItemPrice;
    private int ItemQty;
   private double ItemTotalPrice;



    public ShoppingItem(String ItemName, double ItemPrice, int ItemQty )
    {
        //,double TotalPrice
        this.ItemName = ItemName;
        this.ItemPrice = ItemPrice;
        this.ItemQty = ItemQty;
        this.ItemTotalPrice=getItemTotalPrice();

    }


    public String getItemName() {
        return ItemName;
    }

    public double getItemPrice() {
        return ItemPrice;
    }

    public double getItemTotalPrice() {
        return ItemPrice * ItemQty;
    }

    public int getItemQty() {
        return ItemQty;
    }

    public void setItemName(String ItemName)
    {
        this.ItemName = ItemName;
    }

    public void setItempPrice(double ItemPrice)
    {
        this.ItemPrice = ItemPrice;
    }

    public void setItemQty(int ItemQty)
    {
        this.ItemQty = ItemQty;
    }


    @Override
    public String toString()
    {
        String state = ItemName + " - " + ItemPrice + "€ x " + ItemQty+"="+ItemTotalPrice+" €";
        return state;
    }

}
