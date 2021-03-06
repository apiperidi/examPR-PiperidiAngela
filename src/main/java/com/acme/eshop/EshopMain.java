package com.acme.eshop;

import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.System.exit;

public class EshopMain {
    private static final Logger loggerAng = LoggerFactory.getLogger(EshopMain.class);
    private static final String DB_URL = "jdbc:h2:mem:dbExam"; //memory database στο heap| name: dbExam
   private static final String DB_USERNAME = "sa";
    private static final String DB_PASSWORD = "";
    private final Properties sqlCommands = new Properties(); //η διεύθυνση στην μνήμη που έχει πάρει το object properties
    private final Lorem generator = LoremIpsum.getInstance(); // random names
    private Server h2Server, webServer; //http://localhost:8082/ kai JDBC url: jdbc:h2:mem:sample
    private HikariDataSource hikariDatasource;
   // enum CustomerCategory{}

    public static void main(String[] args) throws InterruptedException {
       EshopMain eshop =new EshopMain();

        eshop.loadSqlCommands();
        // Start H2 database server
        eshop.startH2Server();
        // Register JDBC driver and retrieve a connection
        eshop.initiateConnectionPooling();

        //**********Products*********************
        // Create table Products
        eshop.createProducts();
        // Insert table Products
        eshop.insertListOfProducts();
        // Select all table Products
        eshop.SelectListOfProducts();
        //**************************************

        //**********Db Table Customer*********************
           eshop.createCustomer();
        // **********Db Table Orders*********************
        eshop.createOrder();

        //**********Menu Shopping List*********************

        try {
            Scanner input = new Scanner(System.in);


            ShoppingList myList = new ShoppingList();
            int userOpt = 0;
            while (userOpt != 10) {
                System.out.println("");
                System.out.println("--------------------------------");
                System.out.println("------- Menu Shopping List------");
                System.out.println("(1) Add a Product to the list. ");
                System.out.println("(2) Display My Basket and total number of items. ");
                System.out.println("(3) Remove a Product from My Basket.");
                System.out.println("(4) Customer Checkout and Pay.");
                System.out.println("(5) Admin eshop Show Orders.");
                System.out.println("(6) Admin ShowOrderReport 1.");
                System.out.println("(7) Admin ShowOrderReport 2.");
                System.out.println("(8) Admin ShowOrderReport 3.");
                System.out.println("(9) Admin ShowOrderReport 4.");
                System.out.println("(10) Exit.");
                System.out.println("--------------------------------");

                userOpt = input.nextInt();

                if (userOpt == 1) {
                    myList.addItem();
                }

                if (userOpt == 2) {
                    myList.displayItem();
                }
                if (userOpt == 3) {
                    myList.removeItem();
                }
                if (userOpt == 4) {
                    getPay(eshop, myList);
                }
                if (userOpt == 5) {
                    ShowOrder(eshop);
                }
                if (userOpt == 6) {
                    ShowOrderReport(eshop);
                }
                if (userOpt == 7) {
                    ShowOrderReportSec(eshop);
                }
                if (userOpt == 8) {
                    ShowOrderReportThird(eshop);
                }
                if (userOpt == 9) {
                    ShowOrderReportF(eshop);
                }
                if (userOpt == 10) {
                    eshop.stopH2Server();
                }

            }
        }
        catch (InputMismatchException e) {
            loggerAng.info("Not a valid number");
        }



        // Stop H2 database server via shutdown hook
      //  Runtime.getRuntime().addShutdownHook(new Thread(() -> eshop.stopH2Server()));

     //   while (true) {
     //   }

    }




    public enum CustomerCategoryEnum {
        B2C,B2B,B2G;

       static CustomerCategoryEnum ask(){
            int prob = (int) (100*ThreadLocalRandom.current().nextDouble());

            String returnS=null;
            if(prob < 10)
                return  CustomerCategoryEnum.B2C;
            else if (prob>10 && prob<40)
                 return CustomerCategoryEnum.B2B;
            else
                return CustomerCategoryEnum.B2G;

        }
    }

    private void initiateConnectionPooling() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver"); //δηλώνω τον driver
        config.setJdbcUrl(DB_URL);
        config.setUsername(DB_USERNAME);
        config.setPassword(DB_PASSWORD);

        config.setConnectionTimeout(5000);
        config.setIdleTimeout(10000);
        config.setMaxLifetime(1800000);
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(10);
        config.setAutoCommit(true);

       config.addDataSourceProperty("cachePrepStmts", "true");
       config.addDataSourceProperty("prepStmtsCacheSize", "500");
        hikariDatasource = new HikariDataSource(config);
    }

    private void loadSqlCommands() {
        try (InputStream inputStream = EshopMain.class.getClassLoader().getResourceAsStream("sql.properties")) {
           if (inputStream == null) {
                loggerAng.error("warn enableTranscactionMode");

                exit(-1);
            }
            sqlCommands.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startH2Server() {

        //tcpAllowOthers: Να συνδεθούν και άλλοι
        //tcpDaemon : Να σηκωθεί σαν service
        try {
            h2Server = Server.createTcpServer("-tcpAllowOthers", "-tcpDaemon");
            h2Server.start();
            webServer = Server.createWebServer("-webAllowOthers", "-webDaemon");
            webServer.start();
            loggerAng.info("H2 Database server is now accepting connections.");
        } catch (SQLException throwables) {
            loggerAng.error("Unable to start H2 database server.", throwables);
            exit(-1);
        }
        loggerAng.info("H2 server has started with status '{}'.", h2Server.getStatus());


    }

    private void stopH2Server() {

        if (h2Server == null || webServer == null) { // Εάν ο server δεν υπάρχει
            return;
        }

        if (h2Server.isRunning(true)) { //Εάν ο server τρέχει
            h2Server.stop();
            h2Server.shutdown();
        }
        if (webServer.isRunning(true)) {
            webServer.stop();
            webServer.shutdown();
        }
        loggerAng.info("H2 Database server has been shutdown.");

    }

    //**********Products*********************
    private void createProducts() {
        try (Connection connection = hikariDatasource.getConnection();Statement statement = connection.createStatement()) {
            int resultRows = statement.executeUpdate(sqlCommands.getProperty("create.table.Products"));

            loggerAng.info("Create statement Product returned {}" , resultRows);

        } catch (SQLException throwables) {
            loggerAng.error("Unable to create Product table",throwables);
            exit(-1);
        }
    }
    private void generateData (PreparedStatement prStatement, int HowMany) throws SQLException {
        for (int i=0; i<HowMany; i++) {
            prStatement.clearParameters();
            prStatement.setLong(1, 1 + i);
            prStatement.setString(2, generator.getZipCode());
            prStatement.setString(3, "Prd"+i);
            prStatement.setInt(4, ThreadLocalRandom.current().nextInt(1,30));

           // double price = ThreadLocalRandom.current().nextInt(10 * 10, 100 * (10000 + 1)) / 100d;
           // prStatement.setDouble(5,price);
            double dbprice = ThreadLocalRandom.current().nextDouble(100,250);
            prStatement.setDouble(5,Math.round(dbprice*100)/100);
            prStatement.addBatch();


        }
    }
    private void insertListOfProducts() {
        try (Connection connection = hikariDatasource.getConnection();PreparedStatement prStatement = connection.prepareStatement(sqlCommands.getProperty("insert.table.Products"))) {
            generateData(prStatement,10);
            int[] resultRows =prStatement.executeBatch();
            loggerAng.info("Insert statement of Product returned {}" , Arrays.stream(resultRows).sum());

        } catch (SQLException throwables) {
            loggerAng.error("Error occurred while inserting Product list",throwables);
            exit(-1);
        }
    }
    private void SelectListOfProducts() {
        try (Connection connection = hikariDatasource.getConnection();Statement statement = connection.createStatement();
             ResultSet rs= statement.executeQuery(sqlCommands.getProperty("select.table.Products")) )
        {
            while (rs.next())
            {
                loggerAng.info("ID: {} Product Code: {}. Product Description: {}. Product Quantity: {}. Product Price:{} € ",rs.getLong("id"), rs.getString("Code"),rs.getString("Description"),rs.getInt("Quantity"),rs.getDouble("Price"));
            }

        } catch (SQLException throwables) {
            loggerAng.error("Error occurred while selecting data",throwables);
            exit(-1);
        }

    }
    //**********End Products*********************

    //**********Create Tables Customer And Order *********************
    private void createCustomer() {
        try (Connection connection = hikariDatasource.getConnection();Statement statement = connection.createStatement()) {
            int resultRows = statement.executeUpdate(sqlCommands.getProperty("create.table.Customer"));

            loggerAng.info("Create statement Customer returned {}" , resultRows);

        } catch (SQLException throwables) {
            loggerAng.error("Unable to create Customer table",throwables);
            exit(-1);
        }
    }
    private void createOrder() {
        try (Connection connection = hikariDatasource.getConnection();Statement statement = connection.createStatement()) {
            int resultRows = statement.executeUpdate(sqlCommands.getProperty("create.table.Order"));

            loggerAng.info("Create statement Order returned {}" , resultRows);

        } catch (SQLException throwables) {
            loggerAng.error("Unable to create Order table",throwables);
            exit(-1);
        }
    }
    //**********End Create Tables Customer And Order *********************

    //**********Checkout*********************
    private static void getPay(EshopMain eshop, ShoppingList myList) {
        eshop.pay(myList);
    }


    private void SelectProductExist(List<String> ProdList)
    { try (Connection connection = hikariDatasource.getConnection();Statement statement = connection.createStatement();
           ResultSet rs= statement.executeQuery(sqlCommands.getProperty("select.table.DescProducts")) ) {
            while (rs.next()) { ProdList.add(rs.getString("Description")); }
    }
     catch (SQLException throwables) {
        loggerAng.error("Error occurred while selecting data",throwables);
        exit(-1);
    }
    }



    private  Boolean checkProductexist(ShoppingList myList)
    {
      //  int x=0;
      //  int y=0;
            List<String> ProdList = new ArrayList<String>();
             List<String> ProdBasketList = new ArrayList<String>();
            SelectProductExist(ProdList);


       for (int i = 0; i < myList.list.stream().count(); i++) { ProdBasketList.add(myList.displayItemName(i));}
            boolean found = false;
               for(String title: ProdBasketList) {
                 //   System.out.println("Gia na dw Basket"+ title);
                    for (String model : ProdList) {
                      //  System.out.println("Gia na dw Database"+ model);
                        if (title.toUpperCase().equals(model.toUpperCase())) {
                            found = true;

                           break;
                        } else {
                            found = false;
                        }


                    }
                   if (!found) {
                       break;
                   }
                }

                return found;
    }

    private void pay(ShoppingList myList )  {
            if (!myList.list.isEmpty())
           {
              boolean Productexist=checkProductexist(myList);
                if (Productexist) {
                    int OrderID = ThreadLocalRandom.current().nextInt(1, 300);
                    String Name = generator.getFirstName();
                    String LastName = generator.getLastName();
                    Customer person1 = new Customer(Name, LastName, CustomerCategoryEnum.ask().toString(), "Cash");
                    InsertCustomer(person1, OrderID);
                    try (Connection connection = hikariDatasource.getConnection(); PreparedStatement prStatement = connection.prepareStatement(sqlCommands.getProperty("insert.table.Order"))) {
                        for (int i = 0; i < myList.list.stream().count(); i++) {

                            Double dDiscountPrice = (myList.displayTotalPrice(i) * person1.getDiscount()) / 100;

                            prStatement.setLong(1, ThreadLocalRandom.current().nextInt(1, 300));
                            prStatement.setLong(2, OrderID);
                            prStatement.setString(3, myList.displayItemName(i));
                            prStatement.setString(4, person1.getCustomerLatName());
                            prStatement.setInt(5, myList.displayQuantity(i));
                            prStatement.setDouble(6, myList.displayTotalPrice(i));
                            prStatement.setDouble(7, dDiscountPrice);
                            prStatement.addBatch();
                            prStatement.clearParameters();

                        }
                        int[] resultRows = prStatement.executeBatch();
                        loggerAng.info("Insert statement of Order returned {}", Arrays.stream(resultRows).sum());
                        myList.clearList();
                    } catch (SQLException throwables) {
                        loggerAng.error("Error occurred while inserting Order", throwables);
                        exit(-1);
                    }
                }
                else { loggerAng.info("The product you selected does not exist in the catalog. Please Remove it from your Basket List.");}
           }
            else{ loggerAng.info("Your Basket is empty. Please add products");}
            }

    private void InsertCustomer(Customer person1,Integer OrderID) {
        insertListOfCustomer(person1,OrderID);        }

    public void insertListOfCustomer(Customer person1,Integer OrderID) {
        try (Connection connection = hikariDatasource.getConnection();PreparedStatement prStatement = connection.prepareStatement(sqlCommands.getProperty("insert.table.Customer"))) {

            prStatement.setLong(1,ThreadLocalRandom.current().nextInt(1,300));
            prStatement.setLong(2,OrderID);
            prStatement.setString(3,person1.getCustomerName());
            prStatement.setString(4,person1.getCustomerLatName());
            prStatement.setInt(5,person1.getDiscountCategory());
            prStatement.setString(6,person1.getCustomerCategory());
            prStatement.addBatch();
            prStatement.clearParameters();

            //generateData(prStatement,10);
            int[] resultRows =prStatement.executeBatch();
            loggerAng.info("Insert statement of Customer returned {}" , Arrays.stream(resultRows).sum());

        } catch (SQLException throwables) {
            loggerAng.error("Error occurred while inserting Customer list",throwables);
            exit(-1);
        }
    }
    //********** End Checkout*********************

    //**********Show Order*********************
    private static void ShowOrder(EshopMain eshop) {
        eshop.ShowOrders();
    }

    private void ShowOrders() {
        try (Connection connection = hikariDatasource.getConnection();Statement statement = connection.createStatement();
             ResultSet rs= statement.executeQuery(sqlCommands.getProperty("select.table.Orders")) )
        {

                while (rs.next()) {

                    loggerAng.info("Order Code: {} - Customer Name: {} {} Customer Category: {}. " +
                                    "Customer Discount By Category : {}. " +
                                    "Product Description: {}. " +
                                    "Product Quantity: {}. " +
                                    "Product Price Before Discount:{} € " +
                                    "Product Price After Discount:{} € "
                            ,
                            rs.getLong("OrderID"),
                            rs.getString("LastName"),
                            rs.getString("Name"),
                            rs.getString("CustomerCategory"),
                            rs.getDouble("CustomerDiscount"),
                            rs.getString("ProductDesc"),
                            rs.getInt("Quantity"),
                            rs.getDouble("TotalPrice"),
                            rs.getDouble("DiscountPrice"));
                    //  System.out.println();
                }



        } catch (SQLException throwables) {
            loggerAng.error("Error occurred while selecting data",throwables);
            exit(-1);
        }

    }
    //**********End Show Order*********************

    //**********Show Report 2*********************
    private static void ShowOrderReportSec(EshopMain eshop) {
        eshop.OrdersReportSec();
    }

    private void OrdersReportSec() {
        try (Connection connection = hikariDatasource.getConnection();Statement statement = connection.createStatement();
             ResultSet rs= statement.executeQuery(sqlCommands.getProperty("select.table.AverageOrderCost")) )
        {

            while (rs.next()) {

                loggerAng.info("Average order Cost: {} "
                        ,
                        rs.getDouble("AvgDiscountPrice")
                      );
                //  System.out.println();
            }



        } catch (SQLException throwables) {
            loggerAng.error("Error occurred while selecting data",throwables);
            exit(-1);
        }

    }
    //**********End Show Report 2*********************

    //**********Show Report 1*********************
    private static void ShowOrderReport(EshopMain eshop) {
        eshop.OrdersReport();
    }

    private void OrdersReport() {
        try (Connection connection = hikariDatasource.getConnection();Statement statement = connection.createStatement();
             ResultSet rs= statement.executeQuery(sqlCommands.getProperty("select.table.OrdersReport")) )
        {

            while (rs.next()) {

                loggerAng.info("Customer Name: {}  Number of Orders: {} Τhe orders had {} Products"
                        ,
                        rs.getString("Customername"),
                        rs.getString("NumofOrders"),
                        rs.getString("NumofProcucts")
                );
                //  System.out.println();
            }



        } catch (SQLException throwables) {
            loggerAng.error("Error occurred while selecting data",throwables);
            exit(-1);
        }

    }
    //**********End Show Report 1*********************

    //**********Show Report 3*********************
    private static void ShowOrderReportThird(EshopMain eshop) {
        eshop.OrdersReportThird();
    }

    private void OrdersReportThird() {
        try (Connection connection = hikariDatasource.getConnection();Statement statement = connection.createStatement();
             ResultSet rs= statement.executeQuery(sqlCommands.getProperty("select.table.AverageOrderCostGROUPBY")) )
        {

            while (rs.next()) {

                loggerAng.info("Customer Name: {}  Average order by Customer: {}. "
                        ,
                        rs.getString("Customername"),
                        rs.getDouble("AvgDiscountPrice")
                );


                //  System.out.println();
            }



        } catch (SQLException throwables) {
            loggerAng.error("Error occurred while selecting Average order by Customer",throwables);
            exit(-1);
        }

    }
    //**********End Show Report 3*********************

    //**********Show Report 4*********************
    private static void ShowOrderReportF(EshopMain eshop) {
        eshop.OrdersReportF();
    }

    private void OrdersReportF() {
        try (Connection connection = hikariDatasource.getConnection();Statement statement = connection.createStatement();
             ResultSet rs= statement.executeQuery(sqlCommands.getProperty("select.table.SumOrderCostGROUPBY")) )
        {

            while (rs.next()) {


                loggerAng.info("Customer Name: {}  SumPrice: {}. "
                        ,
                        rs.getString("Customername"),
                        rs.getDouble("SumPrice")
                );


                //  System.out.println();
            }



        } catch (SQLException throwables) {
            loggerAng.error("Error occurred while selecting Average order by Customer",throwables);
            exit(-1);
        }

    }
    //**********End Show Report 4*********************



}
