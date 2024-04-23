import java.util.Scanner;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class SysInterface {
    public static void systemInterface(Connection connection, Date currentDate) throws FileNotFoundException, IOException {
           
        try {connection.setAutoCommit(false); } catch (SQLException e) {System.out.printf("During the creating of data exception occured. %s", e);}
        String dropOrderingTable = "DROP TABLE Ordering";
        String dropOrdersTable = "DROP TABLE Orders";
        String dropCustomersTable = "DROP TABLE Customers";
        String dropBookAuthorsTable = "DROP TABLE Book_authors";
        String dropBooksTable = "DROP TABLE Books";

        String createBooksTable = "CREATE TABLE Books (\n" +
                "  ISBN CHAR(13) CHECK (REGEXP_LIKE(ISBN, '^[0-9]-[0-9]{4}-[0-9]{4}-[0-9]$')),\n" +
                "  title CHAR(100) NOT NULL,\n" +
                "  unit_price INTEGER NOT NULL CHECK (unit_price >= 0),\n" +
                "  no_of_copies INTEGER NOT NULL CHECK (no_of_copies >= 0),\n" +
                "  PRIMARY KEY (ISBN)\n" +
                ")";

        String createBookAuthorsTable = "CREATE TABLE Book_authors (\n" +
                "  ISBN CHAR(13) CHECK (REGEXP_LIKE(ISBN, '^[0-9]-[0-9]{4}-[0-9]{4}-[0-9]$')),\n" +
                "  author_name CHAR(50) CHECK (NOT REGEXP_LIKE(author_name, ',')),\n" +
                "  PRIMARY KEY (ISBN, author_name),\n" +
                "  FOREIGN KEY (ISBN) REFERENCES Books\n" +
                ")";

        String createCustomersTable = "CREATE TABLE Customers (\n" +
                "  customer_id CHAR(10) NOT NULL,\n" +
                "  name CHAR(50) NOT NULL,\n" +
                "  shipping_address CHAR(200) NOT NULL,\n" +
                "  credit_card_no CHAR(19) CHECK (REGEXP_LIKE(credit_card_no, '^[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{4}$')),\n" +
                "  PRIMARY KEY (customer_id)\n" +
                ")";
                
        String createOrdersTable = "CREATE TABLE Orders (\n" +
                "  order_id CHAR(8) CHECK(REGEXP_LIKE(order_id, '^[0-9]{8}$')),\n" +
                "  o_date DATE,\n" +
                "  shipping_status CHAR(1) CHECK(REGEXP_LIKE(shipping_status, '^[YN]$')),\n" +
                "  charge INTEGER NOT NULL CHECK(charge >= 0),\n" +
                "  customer_id CHAR(10) NOT NULL,\n" +
                "  PRIMARY KEY (order_id),\n" +
                "  FOREIGN KEY (customer_id) REFERENCES Customers\n" +
                ")";

            
        String createOrderingTable = "CREATE TABLE ORDERING (\n" +
                "  Quantity INT NOT NULL CHECK (Quantity>=0),\n" +
                "  order_id CHAR(8) CHECK(REGEXP_LIKE(order_id, '^[0-9]{8}$')),\n" +
                "  ISBN CHAR(13) CHECK(REGEXP_LIKE(ISBN, '^[0-9]-[0-9]{4}-[0-9]{4}-[0-9]$')),\n" +
                "  PRIMARY KEY (order_id, ISBN),\n" +
                "  FOREIGN KEY (order_id) REFERENCES Orders,\n" +
                "  FOREIGN KEY (ISBN) REFERENCES Books\n" +
                ")";

        while (true) {
            System.out.println("* You have entered the System Interface. *");
            System.out.println("-------------------------");
            System.out.println("Options:");
            System.out.println("1. Create Table");
            System.out.println("2. Delete Table");
            System.out.println("3. Insert Data");
            System.out.println("4. Set System Date");
            System.out.println("5. Back to main menu");
            System.out.println("Please enter your choice (1 - 5):");
            
            Scanner scanner = new Scanner(System.in);

            int parsedOption = App.HandleInput(scanner,5);
            Statement statement = null;                
            switch (parsedOption) {
                case 1:
                    try {

                    statement = connection.createStatement();

                    // Executing DROP TABLE statements


                    // Creating Books table
                    statement.executeUpdate(createBooksTable); 

                    // Creating Book_authors table
                    statement.executeUpdate(createBookAuthorsTable);

                    // Creating Customers table
                    statement.executeUpdate(createCustomersTable);

                    // Creating Orders table
                    statement.executeUpdate(createOrdersTable);

                    // Creating ORDERING table
                    statement.executeUpdate(createOrderingTable);
                    
                    statement.close();

                    connection.commit();
                    System.out.println("Table created successfully.");
                    } catch (SQLException e){
                        System.out.printf("Table creation not successful. Exception: %s", e);
                    }
                    break;
                case 2:
                    try {

                        statement = connection.createStatement();

                        statement.executeUpdate(dropOrderingTable);
                        statement.executeUpdate(dropOrdersTable);
                        statement.executeUpdate(dropCustomersTable);
                        statement.executeUpdate(dropBookAuthorsTable);
                        statement.executeUpdate(dropBooksTable);
                        connection.commit();
                        statement.close();
                        System.out.println("Tables deleted successfully.");
                    } catch (SQLException e){
                        System.out.printf("Table deletion not successful. Exception: %s", e);
                    }
                    break;

                case 3:
                    //Add data from path
                    System.out.println("Please enter path to data directory: ");
                    String pathString = scanner.nextLine();
                    List<File> fileList = null;
                    try{fileList = readFromDirectory(pathString);} catch (IllegalStateException e) {System.out.println(e);
                        break;
                    }
                    catch (FileNotFoundException e) {System.out.println(e);
                    break;
                    }
                    try {connection.setAutoCommit(false); } catch (SQLException e) {System.out.printf("During the creating of data exception occured. %s", e);}
                    try{
                    for (int i = 0; i < 5; i++) {

                        File tempFile = fileList.get(i);
                        loadData(tempFile, i+1, connection);
                            
                        
                    }

                    
                    connection.commit();
                    } catch (RuntimeException e) {
                        try {
                            connection.rollback();
                        } catch (SQLException rollbackException) {
                            rollbackException.printStackTrace();
                        }
                        System.out.println(e);
                        break;
                    } catch (SQLException e) {
                        try {
                            connection.rollback();
                        } catch (SQLException rollbackException) {
                            rollbackException.printStackTrace();
                        }
                        System.out.printf("During the loading of data, this Exception occured: %s", e);
                        break;
                    }
                    
                    
                    break;
                case 4:
                    System.out.println("Input new System Date: ");
                    String pattern = "yyyy-MM-dd";
                    SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
                    Date newDate = null;
                    String dateString = null;
                    dateString = scanner.nextLine();
                    try{
                    newDate = dateFormat.parse(dateString);
                    App.setCurrentDate(connection, newDate);
                    
                    
                    } catch (ParseException e) {
                        System.out.println("Invalid Date.");
                    }
                    break;
                case 5:
                    try {connection.setAutoCommit(true); } catch (SQLException e) {System.out.printf("During the creating of data exception occured. %s", e);}
                    return;
            }
        }
        
    }
    
    public static List<File> readFromDirectory(String pathToDir) throws FileNotFoundException {
        String directoryPath = pathToDir;
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        if (files != null && files.length > 0) {
            
            List<File> fileList = new ArrayList<>();
            fileList.add(new File(directory, "book.txt"));
            fileList.add(new File(directory, "book_author.txt"));
            fileList.add(new File(directory, "customer.txt"));
            fileList.add(new File(directory, "orders.txt"));
            fileList.add(new File(directory, "ordering.txt"));
            for (File file: fileList) {
                if (!file.exists()) {
                    throw new FileNotFoundException("Some files does not exist.");
                }
            }
            return fileList;
            
            
            }
        else {
            throw new IllegalStateException("No files found.");
        }
        
    }

    public static void loadData(File file, int tableNum, Connection connection) throws FileNotFoundException, IOException, SQLException {
        String sql = null;
        connection.setAutoCommit(false);
        switch (tableNum) {
            case 1:
                //books
                 sql = "INSERT INTO Books (ISBN, title, unit_price, no_of_copies) \n" + 
                 "VALUES (?, ?, ?, ?)";
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new FileReader(file));
                    String line;
                    PreparedStatement stmt = connection.prepareStatement(sql);
                    String[] inputs;
                    while ((line = reader.readLine()) != null) {

 
                        inputs = line.split("\\|");
                        
                        //check length
                        if (inputs.length != 4) {
                            throw new RuntimeException("Some rows have non matching columns.");
                        }
                        
                        //see if unit price and num of copies is integer parseable
                        int unitPrice = 0;
                        int numCopies = 0;
                        try {
                            unitPrice = Integer.parseInt(inputs[2]);
                            numCopies = Integer.parseInt(inputs[3]);

                        }
                        catch (NumberFormatException e){
                            throw new RuntimeException("unit price or num copies cannot be parsed to integer.");
                            
                        }
                        
                        stmt.setString(1, inputs[0]);
                        stmt.setString(2, inputs[1]);
                        stmt.setInt(3, unitPrice);
                        stmt.setInt(4, numCopies);
                        connection.setAutoCommit(false);
                        stmt.executeUpdate();
                    
                    }
                    System.out.println("Books table successfully loaded.");
                    
                } catch(SQLException e) {
                    throw new SQLException(e);
                }
                finally{
                    reader.close();
                }
                break;
            case 2:
                //book authors
                sql = "INSERT INTO Book_authors (ISBN, author_name) VALUES (?, ?)";
    
                try  {
                    reader = new BufferedReader(new FileReader(file));
                    String line;
                    PreparedStatement stmt = connection.prepareStatement(sql);
                    String[] inputs;
                    while ((line = reader.readLine()) != null) {
                        inputs = line.split("\\|");
                        stmt.setString(1, inputs[0]);
                        stmt.setString(2, inputs[1]);
                        stmt.executeUpdate();

                    
                    }
                    System.out.println("Book authors table loaded.");
                    reader.close();
                
                } catch(SQLException e) {
                    throw new SQLException(e);
                }
                break;
            case 3:
                //book authors
                sql = "INSERT INTO Customers (customer_id, name, shipping_address, credit_card_no) VALUES (?, ?, ?, ?)";
    
                try  {
                    reader = new BufferedReader(new FileReader(file));
                    String line;
                    PreparedStatement stmt = connection.prepareStatement(sql);
                    String[] inputs;
                    while ((line = reader.readLine()) != null) {
                        
                        
                        inputs = line.split("\\|");
                        if (inputs.length != 4) {
                            throw new RuntimeException("Some rows have non matching columns.");
                        }
                        stmt.setString(1, inputs[0]);
                        stmt.setString(2, inputs[1]);
                        stmt.setString(3, inputs[2]);
                        stmt.setString(4, inputs[3]);
                        stmt.executeUpdate();
                        
                    
                    }

                    System.out.println("Customer table loaded.");
                } catch(SQLException e) {
                    throw new SQLException(e);
                }
                break;
            case 4:
                //orders 
                sql = "INSERT INTO orders (order_id, o_date, shipping_status, charge, customer_id) VALUES (?, TO_DATE(?, 'YYYY-MM-DD'), ?, ?, ?)";

                try  {
                    reader = new BufferedReader(new FileReader(file));
                    String line;
                    PreparedStatement stmt = connection.prepareStatement(sql);
                    String[] inputs;
                    while ((line = reader.readLine()) != null) {
                        
            
                        inputs = line.split("\\|");
                        
                        //check length
                        if (inputs.length != 5) {
                            throw new RuntimeException("Some rows have non matching columns.");
                        }
                        
                        //see if charge is integer parseable
                        int charge = 0;
                        try {
                            charge = Integer.parseInt(inputs[3]);
                        }
                        catch (NumberFormatException e){
                            throw new RuntimeException("charge cannot be parsed to integer.");
                            
                        }
                        stmt.setString(1, inputs[0]);
                        stmt.setString(2, inputs[1]);
                        stmt.setString(3, inputs[2]);
                        stmt.setInt(4, charge);
                        stmt.setString(5, inputs[4]);

                        stmt.executeUpdate();
                    }
                    System.out.println("Orders table successfully loaded.");
                    
                } catch(SQLException e) {
                    throw new SQLException(e);
                }
                break;
            case 5:
                //ordering
                sql = "INSERT INTO ordering  (order_id, ISBN, quantity) VALUES (?, ?, ?)";

                try  {
                    reader = new BufferedReader(new FileReader(file));
                    String line;
                    PreparedStatement stmt = connection.prepareStatement(sql);
                    String[] inputs;
                    
                    while ((line = reader.readLine()) != null) {
                        
                        inputs = line.split("\\|");
                        
                        //check length
                        if (inputs.length != 3) {
                            
                            throw new RuntimeException("Some rows have non matching columns.");
                        }
                        
                        //see if quantity of copies is integer parseable
                        int quantity = 0;
                        try {
                            quantity = Integer.parseInt(inputs[2]);
                        }
                        catch (NumberFormatException e){
                            throw new RuntimeException("charge cannot be parsed to integer.");
                            
                        }
                        stmt.setString(1, inputs[0]);
                        stmt.setString(2, inputs[1]);
                        stmt.setInt(3, quantity);

                        stmt.executeUpdate();
                    }
                    System.out.println("Orders table successfully loaded.");
                    
                } catch(SQLException e) {
                    throw new SQLException(e);
                }
            default:
                System.out.println("All tables loaded.");
                break;
        }
        
    }
   
}
