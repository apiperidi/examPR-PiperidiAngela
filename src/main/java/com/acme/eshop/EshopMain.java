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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;
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

        //Product ProductClass =new Product();


        // Create table Products
        eshop.createProducts();
        Thread.sleep(3000);
        // Insert table Products
        eshop.insertListOfProducts();
        // Select all table Products
        eshop.SelectListOfProducts();


        //Create Customers

        eshop.createCustomer();
        Thread.sleep(3000);
        // Insert table Products
        eshop.insertListOfCustomers();
eshop.SelectListOfCustomer();

// Stop H2 database server
        // Stop H2 database server via shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> eshop.stopH2Server()));

        while (true) {
        }

    }



   public enum CustomerCategory {
        B2C,B2B,B2G;

       static CustomerCategory ask(){
            int prob = (int) (100*ThreadLocalRandom.current().nextDouble());

            String returnS=null;
            if(prob < 10)
                return  CustomerCategory.B2C;
            else if (prob>10 && prob<40)
                 return CustomerCategory.B2B;
            else
                return CustomerCategory.B2G;

        }
    }

    private void initiateConnectionPooling() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver"); //δηλώνω τον driver
        config.setJdbcUrl(DB_URL);
        config.setUsername(DB_USERNAME);
        config.setPassword(DB_PASSWORD);

        config.setConnectionTimeout(15000);
        config.setIdleTimeout(60000);
        config.setMaxLifetime(1800000);
        config.setMinimumIdle(1);
        config.setMaxLifetime(5);
        config.setAutoCommit(true);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtsCacheSize", "500");
        hikariDatasource = new HikariDataSource(config);
    }

    private void loadSqlCommands() {
        try (InputStream inputStream = EshopMain.class.getClassLoader().getResourceAsStream("sql.properties")) {
            System.out.println("ddd");
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

    private void createProducts() {
        try (Statement statement = hikariDatasource.getConnection().createStatement()) {
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
            prStatement.setString(3, "ProductName"+i);
            prStatement.setInt(4, ThreadLocalRandom.current().nextInt(1,30));

           // double price = ThreadLocalRandom.current().nextInt(10 * 10, 100 * (10000 + 1)) / 100d;
           // prStatement.setDouble(5,price);
            double dbprice = ThreadLocalRandom.current().nextDouble(100,250);
            prStatement.setDouble(5,Math.round(dbprice*100)/100);
            prStatement.addBatch();


        }
    }
    private void insertListOfProducts() {
        try ( PreparedStatement prStatement = hikariDatasource.getConnection().prepareStatement(sqlCommands.getProperty("insert.table.Products"))) {
            generateData(prStatement,10);
            int[] resultRows =prStatement.executeBatch();
            loggerAng.info("Insert statement of Product returned {}" , Arrays.stream(resultRows).sum());

        } catch (SQLException throwables) {
            loggerAng.error("Error occurred while inserting Product list",throwables);
            exit(-1);
        }
    }
    private void SelectListOfProducts() {
        try (Statement statement = hikariDatasource.getConnection().createStatement();
             ResultSet rs= statement.executeQuery(sqlCommands.getProperty("select.table.Products")) )
        {
            while (rs.next())
            {
                loggerAng.info("ID: {} Product Code: {}. Product Description: {}. Product Quantity: {}. Product Price:{} $ ",rs.getLong("id"), rs.getString("Code"),rs.getString("Description"),rs.getInt("Quantity"),rs.getDouble("Price"));
                //  System.out.println();
            }

        } catch (SQLException throwables) {
            loggerAng.error("Error occurred while selecting data",throwables);
            exit(-1);
        }

    }

    private void createCustomer() {
        try (Statement statement = hikariDatasource.getConnection().createStatement()) {
            int resultRows = statement.executeUpdate(sqlCommands.getProperty("create.table.Customer"));

            loggerAng.info("Create statement Customer returned {}" , resultRows);

        } catch (SQLException throwables) {
            loggerAng.error("Unable to create Customer table",throwables);
            exit(-1);
        }
    }
    private void generateCustomerData (PreparedStatement prStatement, int HowMany) throws SQLException {
        CustomerCategory result = null;
        for (int i=0; i<HowMany; i++) {

            prStatement.clearParameters();
            prStatement.setLong(1, 1 + i);
            prStatement.setString(2, generator.getFirstName());
            prStatement.setString(3, generator.getLastName());
            prStatement.setString(4, generator.getCity());
            prStatement.setString(5, generator.getEmail());
            prStatement.setString(6, result.ask().toString());

            prStatement.addBatch();


        }
    }
    private void insertListOfCustomers() {
        try ( PreparedStatement prStatement = hikariDatasource.getConnection().prepareStatement(sqlCommands.getProperty("insert.table.Customer"))) {
            generateCustomerData(prStatement,5);
            int[] resultRows =prStatement.executeBatch();
            loggerAng.info("Insert statement of Customer returned {}" , Arrays.stream(resultRows).sum());

        } catch (SQLException throwables) {
            loggerAng.error("Error occurred while inserting customer list",throwables);
            exit(-1);
        }
    }
    private void SelectListOfCustomer() {
        try (Statement statement = hikariDatasource.getConnection().createStatement();
             ResultSet rs= statement.executeQuery(sqlCommands.getProperty("select.table.Customer")) )
        {
            while (rs.next())
            {
                loggerAng.info("ID: {} LastName: {}. Name: {}. Email: {}. City:{}  Category:{} ",
                        rs.getLong("id"), rs.getString("LastName"),rs.getString("Name"),rs.getString("Email")
                        ,rs.getString("City"),rs.getString("CustomerCategory"));
                //  System.out.println();
            }

        } catch (SQLException throwables) {
            loggerAng.error("Error occurred while selecting data",throwables);
            exit(-1);
        }

    }


}
