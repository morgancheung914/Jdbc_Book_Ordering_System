
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;




public class BookstoreApp {

    



    public static void customerInterface(Connection connection, Date currentDate) {
        try{connection.setAutoCommit(false);} catch (SQLException e) {System.out.printf("SQL Exception occured: %s", e);}
        Scanner scanner = new Scanner(System.in);
        boolean customerRunning = true;
        while (customerRunning) {
            System.out.println("<This is the customer interface>");
            System.out.println("----------------------------------");
            System.out.println("1. Book Search.");
            System.out.println("2. Order Creation.");
            System.out.println("3. Order Altering.");
            System.out.println("4. Order Query.");
            System.out.println("5. Back to main menu.");
            System.out.print("Please enter your choice: ");
            String choiceString = scanner.nextLine();
            int choice;
            try {choice = Integer.parseInt(choiceString);} catch (NumberFormatException e)
            {System.out.println("Input cannot be parsed to integer."); 
            continue;}

            switch (choice) {
                case 1:
                    bookSearch(connection);
                    break;
                case 2:
                    createOrder(connection, currentDate);
                    break;
                case 3:
                    orderAlteration(connection, currentDate);
                    break; 
                case 4:
                    System.out.print("Enter Customer ID: ");
                    String customerId = scanner.nextLine();
                    
                    System.out.print("Enter Year: ");
                    int year = -1;
                    try{year = Integer.parseInt(scanner.nextLine());} catch (NumberFormatException e) {System.out.println("Invalid year."); continue;}
                    orderQuery(connection, customerId, year);
                    break;
                case 5:
                    customerRunning = false;
                    try{connection.setAutoCommit(true);} catch (SQLException e) {System.out.printf("SQL Exception occured: %s", e);}

                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

        
    }

    private static void bookSearch(Connection connection) {
        Scanner scanner = new Scanner(System.in);
        boolean searching = true;
        while (searching) {
            System.out.println("What do u want to search?");
            System.out.println("1 ISBN");
            System.out.println("2 Book Title");
            System.out.println("3 Author Name");
            System.out.println("4 Exit");

            System.out.print("Please enter your choice: ");
            int searchChoice = App.HandleInput(scanner, 4);

            switch (searchChoice) {
                case 1:
                    System.out.print("Enter ISBN to search: ");
                    String ISBN = scanner.nextLine();
                    searchByISBN(ISBN, connection);
                    break;
                case 2:
                    System.out.print("Enter book title to search: ");
                    String title = scanner.nextLine();
                    searchByTitle(title, connection);
                    break;
                case 3:
                    System.out.print("Enter author name to search: ");
                    String author = scanner.nextLine();
                    searchByAuthor(author, connection);
                    break;
                case 4:

                    searching = false;
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
        // Close scanner
        scanner.close();
    }

    private static void searchByTitle(String title, Connection connection) {

        // SQL query to retrieve book information by title

        String sql ="SELECT B.title, B.ISBN, B.unit_price, B.no_of_copies, TRIM(A.author_name) AS trim_author \n" +
        "FROM Books B \n" +
        "LEFT JOIN book_authors A ON B.ISBN = A.ISBN \n" +
        "WHERE (LOWER(TRIM(b.title)) LIKE LOWER(?)) ORDER BY CASE WHEN TITLE = ? THEN 0 ELSE 1 END, TITLE, ISBN";
            
        String dePercentString = title.replace("%", "");
            // Execute the query
            try  {
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, title);
                statement.setString(2, dePercentString);
                ResultSet resultSet = statement.executeQuery();
                boolean exactMatchFound = false;
                boolean hasNextAuthor = true;
                boolean hasNext = true;
                while (hasNext && (!hasNextAuthor || resultSet.next())) {
                    exactMatchFound = true;
                    String titleResult = resultSet.getString("title");
                    String isbnResult = resultSet.getString("ISBN");
                    double unitPrice = resultSet.getDouble("unit_price");
                    int no_of_copies = resultSet.getInt("no_of_copies");

                    System.out.println("ISBN: " + isbnResult);
                    System.out.println("Book Title: " + titleResult);
                    System.out.println("Unit Price: " + unitPrice);
                    System.out.println("No of Available: " + no_of_copies);
                    System.out.println("Authors:");
                    hasNextAuthor = false;
                    do {
                        String authorName = resultSet.getString("trim_author");
                        System.out.print(authorName + " ");
                        hasNext = resultSet.next();
                        hasNextAuthor = hasNext && isbnResult.equals(resultSet.getString("ISBN"));

                    } while (hasNextAuthor);
                    System.out.print("\n");
                } if (!exactMatchFound) {
                    System.out.println("No match found for \"" + title + "\"");
                }
            } catch (SQLException e) {
            System.out.println("Error executing SQL query: " + e.getMessage());
        }
    }

    private static void searchByAuthor(String author, Connection connection) {

        String sql = "SELECT B.title, B.ISBN, B.unit_price, B.no_of_copies, TRIM(A.author_name) as trim_author \n"+
        "FROM Books B JOIN Book_authors A ON B.ISBN = A.ISBN WHERE B.ISBN IN (SELECT B.ISBN FROM Books B JOIN Book_authors A ON B.ISBN = A.ISBN\n"+
        "WHERE LOWER(TRIM(A.author_name)) LIKE LOWER(?)) ORDER BY CASE WHEN TRIM(B.TITLE) = ? THEN 0 ELSE 1 END, B.TITLE, B.ISBN";
        
        String dePercentString = author.replace("%", "");

            try  {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, author);
            statement.setString(2, dePercentString);
            
            
            ResultSet resultSet = statement.executeQuery();
            boolean exactMatchFound = false;
            boolean hasNextAuthor = true;
            boolean hasNext = true;
            while (hasNext && (!hasNextAuthor || resultSet.next())) {
                exactMatchFound = true;
                String titleResult = resultSet.getString("title");
                String isbnResult = resultSet.getString("ISBN");
                double unitPrice = resultSet.getDouble("unit_price");
                int no_of_copies = resultSet.getInt("no_of_copies");

                System.out.println("ISBN: " + isbnResult);
                System.out.println("Book Title: " + titleResult);
                System.out.println("Unit Price: " + unitPrice);
                System.out.println("No of Available: " + no_of_copies);
                System.out.println("Authors:");
                hasNextAuthor = false;
                do {
                    String authorName = resultSet.getString("trim_author");
                    System.out.print(authorName + " ");
                    hasNext = resultSet.next();
                    hasNextAuthor = hasNext && isbnResult.equals(resultSet.getString("ISBN"));

                } while (hasNextAuthor);    
                System.out.print("\n");

            } if (!exactMatchFound) {
                System.out.println("No match found for \"" + author + "\"");
            }
            } catch (SQLException e) {
            System.out.println("Error executing SQL query: " + e.getMessage());}
        
    }

    private static void searchByISBN(String ISBN, Connection connection) {
        String sql = "SELECT B.title, B.ISBN, B.unit_price, B.no_of_copies, TRIM(A.author_name) as trim_author FROM Books B LEFT JOIN　Book_authors A ON B.ISBN = A.ISBN WHERE B.ISBN = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, ISBN);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String title = resultSet.getString("title");
                    String isbnResult = resultSet.getString("ISBN");
                    double unitPrice = resultSet.getDouble("unit_price");
                    int no_of_copies = resultSet.getInt("no_of_copies");

                    System.out.println("ISBN: " + isbnResult);
                    System.out.println("Book Title: " + title);
                    System.out.println("Unit Price: " + unitPrice);
                    System.out.println("No of Available: " + no_of_copies);
                    System.out.println("Authors:");
                    do {
                        String authorName = resultSet.getString("trim_author");
                        System.out.print(authorName + " ");
                    } while (resultSet.next());
                    System.out.print("\n");

                } else {
                    System.out.println("ISBN not found.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error executing SQL query: " + e.getMessage());
            
        }
    }

    private static boolean bookExists(Connection connection, String isbn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM books WHERE isbn = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, isbn);
            try (ResultSet resultSet = statement.executeQuery()) {
                
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }
    
    private static void createOrder(Connection connection, Date currentDate) {
        try  {
            connection.setAutoCommit(false);

            Scanner scanner = new Scanner(System.in);

            System.out.print("Please enter your customerID: ");
            String customerId = scanner.nextLine();
            String newOrderId = "00000001";
            
            if (!isValidCustomer(customerId, connection)) {System.out.println("Invalid Customer or year."); return;}
            //query existing orders 

            
            
            
            List<List<Object>> orderItems = new ArrayList();
            boolean has_order = false;
            while (true) {
                System.out.print("Enter ISBN or L to look at the order list, F to finish order: ");
                String input = scanner.nextLine();
                int quantity = -1;
                if (input.equalsIgnoreCase("F")) {
                    
                    if (!has_order){return;}
                    Statement getMaxOrderIdStmt = connection.createStatement();
                    ResultSet maxOrderIdResult = getMaxOrderIdStmt.executeQuery("SELECT MAX(order_id) FROM orders");

                    //get new order ID
                    if (maxOrderIdResult.next()) {
                        String maxOrderId = maxOrderIdResult.getString(1);
                        int numericOrderId = Integer.parseInt(maxOrderId) + 1;
                        newOrderId = String.format("%08d", numericOrderId);
        
                    }


                    break;

                } else if (input.equalsIgnoreCase("L")) {
                    lookOrder(customerId, connection, orderItems);
                    continue;
                
                    
                } else {
                    int bookCharge = 0;
                    //Ordering books option selected
                    if (!bookExists(connection, input)) {
                        System.out.println("Book with ISBN " + input + " does not exist.");
                        continue;
                    }
                    
                
                    System.out.print("Enter quantity for ISBN " + input + ": ");
                    
                    try{quantity = Integer.parseInt(scanner.nextLine());} catch (NumberFormatException e) {System.out.println("Invalid Quantity."); continue;}

                    //Check if quantity is suppliable
                    
                    if (!isEnoughBooks(input, quantity, connection)) {
                        System.out.println("Not enough stock."); 
                        continue;}
                    
                    //input == isbn, quantity == quantity 
                    bookCharge = getCharge(input, quantity, connection);

                    List<Object> order1 = new ArrayList<>();
                    
                    order1.add(input); //isbn
                    order1.add(quantity); //quantity 
                    order1.add(bookCharge); //charge
                    
                    orderItems.add(order1);
                    has_order = true; 

                    
                }
            }
            //insert order
                    //to the order table
                    int total_charge = 0;
                    for (int i = 0; i < orderItems.size(); i++) {
                        total_charge += (int) orderItems.get(i).get(2);
                    }
                    PreparedStatement insertOrderStmt = connection.prepareStatement(
                    "INSERT INTO orders (order_id, o_date, shipping_status, charge, customer_id) VALUES (?, TO_DATE(?, 'YYYY-MM-DD'), 'N', ?, ?)");
                    insertOrderStmt.setString(1, newOrderId);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String currentDateTime = sdf.format(currentDate);
                    insertOrderStmt.setString(2, currentDateTime);
                    insertOrderStmt.setInt(3, total_charge);
                    insertOrderStmt.setString(4, customerId);
                    insertOrderStmt.executeUpdate();
        
                    
                
                
                    //to the ordering table
                PreparedStatement insertOrderingStmt = connection.prepareStatement(
                        "INSERT INTO ordering (order_id, isbn, quantity) VALUES (?, ?, ?)");
                        
                
                for (List<Object> orderItem: orderItems) {
                    insertOrderingStmt.setString(1, newOrderId);
                    insertOrderingStmt.setString(2,  orderItem.get(0).toString());
                    insertOrderingStmt.setInt(3, ((int) orderItem.get(1)));
                    insertOrderingStmt.executeUpdate();

                }


                //update new quantity
                PreparedStatement updateQStmt = connection.prepareStatement("UPDATE Books SET no_of_copies = no_of_copies - ? WHERE ISBN = ?");

                for (List<Object> orderItem: orderItems) {
                updateQStmt.setInt(1, ((int) orderItem.get(1)));
                updateQStmt.setString(2, orderItem.get(0).toString());
                updateQStmt.executeUpdate();
                }

                connection.commit();
                has_order = false;
                System.out.printf("Insert to Orders success, order id: %s, customer_id: %s\n", newOrderId, customerId);

                
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackException) {rollbackException.printStackTrace();}
            System.out.println("Error creating order: " + e.getMessage());
        } 
        

        
    }
    
    private static void lookOrder(String customerID, Connection connection, List<List<Object>> orderItems) {

        for (List<Object> orderItem : orderItems) {
            System.out.println("ISBN: " + orderItem.get(0) + " - Quantity Sum: " + orderItem.get(1));
        }

    }
    
    private static boolean isEnoughBooks(String input, int quantity, Connection connection) throws SQLException{
        String sql = "SELECT no_of_copies FROM books WHERE ISBN = ?";
        try{
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, input);
        ResultSet rs = statement.executeQuery();
        int comQuantity = -1;
        while(rs.next()){
            comQuantity = rs.getInt("no_of_copies");

        }
        if (comQuantity >= quantity){
            return true;

        } else {
            return false;
        }
        }
        catch (SQLException e) {throw new SQLException(e);}

    }

    private static int getCharge(String input, int quantity, Connection connection) throws SQLException {
        String sql = "SELECT unit_price FROM books WHERE ISBN = ?";
        try{
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, input);
        ResultSet rs = statement.executeQuery();
        int unitPrice = -1;
        while(rs.next()){
            unitPrice = rs.getInt("unit_price");
            
        }
        return (unitPrice+10) * quantity + 10;
        }
        catch (SQLException e) {throw new SQLException(e);}
    }

    private static boolean isValidCustomer(String customerID, Connection connection) throws SQLException {
        String sql = "SELECT CASE WHEN EXISTS (SELECT 1 FROM customers WHERE TRIM(customer_id) = ?) THEN 1 ELSE 0 END AS result FROM dual";
        
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, customerID);
            ResultSet rs = statement.executeQuery();
            
            while(rs.next()){
                if(rs.getInt("result") == 1) {
                    return true;
                }
                else {return false;}
            
            }
        return false;
    }
    
    private static void orderAlteration(Connection connection, Date currentDate) {
    String status = null; 
    Scanner scanner = new Scanner(System.in);
    Map<Integer, String> m = new HashMap<>();
    int parsedId = -1;
    int quantity = -1;
    try {
        // Ask for Order ID input
        System.out.print("Enter Order ID: ");
        
    
        while (true) {
            try{
                String orderId = scanner.nextLine();
                parsedId = Integer.parseInt(orderId);
                if (parsedId < 0) {
                    System.out.println("Order ID must be a postive integer");
                }
                else{
                    break;
                }
            }
            catch(NumberFormatException e) {
                System.out.println("Invalid input. Please enter again.");
            }
        }

        // Retrieve order details
        String bkstmt1 = "SELECT order_id, shipping_status, charge, customer_id FROM orders WHERE order_id = ?";
        try (PreparedStatement orderStatement = connection.prepareStatement(bkstmt1)) {
            orderStatement.setInt(1, parsedId);
            try (ResultSet orderResultSet = orderStatement.executeQuery()) {
                if (orderResultSet.next()) {
                    // Display order details
                    System.out.println("Order ID: " + String.format("%08d",orderResultSet.getInt("order_id")));
                    status = orderResultSet.getString("shipping_status");
                    System.out.println("Shipping Status: " + orderResultSet.getString("shipping_status"));
                    System.out.println("Charge: " + orderResultSet.getDouble("charge"));
                    System.out.println("Customer ID: " + orderResultSet.getString("customer_id"));
                } else {
                    System.out.println("Order with ID " + parsedId + " not found.");
                    return;
                }
            }
        }

        String bkstmt2 = "SELECT b.isbn, ord.quantity, ROW_NUMBER() OVER (ORDER BY b.isbn) AS book_number \n"
                + "FROM ordering ord \n"
                + "JOIN books b ON ord.isbn = b.isbn \n"
                + "WHERE ord.order_id = ?";
        try (PreparedStatement bookStatement = connection.prepareStatement(bkstmt2)) {
            bookStatement.setInt(1, parsedId);
            try (ResultSet bookResultSet = bookStatement.executeQuery()) {
                // Display list of books in the order
                System.out.println("Books in Order with ID " + parsedId + ":");
                while (bookResultSet.next()) {
                    String isbn = bookResultSet.getString("isbn");
                    quantity = bookResultSet.getInt("quantity");
                    int bookNumber = bookResultSet.getInt("book_number");
                    System.out.println("Book " + bookNumber + ": ISBN: " + isbn + ", Quantity: " + quantity);
                    m.put(bookNumber, isbn);
                }
            }
        }
    } catch (SQLException e) {
        System.out.println("Error retrieving order details: " + e.getMessage());
    } catch (NumberFormatException e) {
        System.out.println("Invalid Order ID format. Please enter a valid integer.");
    }

    if (status.equals("Y")) {
        System.out.println("The books in the order are shipped");
        return; 
    }
    int bookoption = -1;
    
    
        while (true) {
            System.out.print("Which book you want to alter (input book no.): ");
            try{
                String bookopt = scanner.nextLine();
                bookoption = Integer.parseInt(bookopt);
                if ((bookoption < 1) || (bookoption > m.size())) {
                    System.out.println("Invalid book number");
                    continue;
                }
                else{
                    break;
                }
                }

            catch(NumberFormatException e) {
                System.out.println("Invalid input. Please enter again.");
            }
        }

    boolean add = false;
    boolean remove = false;

    
    while(true) {
        System.out.println("Type 'add' if you would like to add copies of chosen book, type 'remove' to remove copies. Type 'return' to return");
        String ans = scanner.nextLine();
        if (ans.equals("add")) {
            add = true;
            break;
        }
        else if (ans.equals("remove")) {
            remove = true;
            break;
        }

        else if (ans.equals("return")) {
            return;
        }
        else {
            System.out.println("Invalid input. Please enter again.");
        }
    }

    System.out.print("Input the amount to add or delete: ");
    int parsednum = -1; 
    while (true) {
        try{
            String num = scanner.nextLine();
            parsednum = Integer.parseInt(num);
            if (parsednum <= 0) {
                System.out.println("Number of books must be a positive integer");
                continue;
            }

        }
        catch(NumberFormatException e) {
            System.out.println("Invalid input. Please enter again.");
            continue;
        }
    
        if (add) {
            try {
                
                if (isEnoughBooks(m.get(bookoption), parsednum, connection)) {
                    //add books 
                    addBooks(connection, parsednum, parsedId, m.get(bookoption), currentDate, quantity);
                    return;
                }
                else {
                    System.out.println("Not enough books in the bookstore.");
                    return;
                }

        } catch (SQLException e) {System.out.printf("during order alteration Exception occured: %s", e);}
        }

        if (remove) {
            
            subBooks(connection, parsednum, parsedId, m.get(bookoption), currentDate);
            return;
        }

    }

}
    
    private static void addBooks(Connection connection, int addAmount, int orderId, String addISBN, Date currentDate, int quantity) {
        //retrieve date string 
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = sdf.format(currentDate);

        //update ordering table
        String sql = "UPDATE ordering SET QUANTITY = QUANTITY + ? WHERE ORDER_ID = ? AND ISBN = ?";

        //update to new date 
        String sql2 = "UPDATE orders SET o_date = to_date(?, 'YYYY-MM-DD') WHERE ORDER_ID = ?";

        //update to new charge
        String sql3 = "UPDATE orders SET CHARGE = CHARGE + ? WHERE ORDER_ID = ?";

        String qCharge = "SELECT unit_price FROM Books WHERE ISBN = ?";

        String incrementString = "UPDATE books SET no_of_copies = no_of_copies - ? WHERE ISBN = ?";
       try {
        connection.setAutoCommit(false);
        //update 1
        PreparedStatement updateStmt1 = connection.prepareStatement(sql);
        updateStmt1.setInt(1, addAmount);
        updateStmt1.setInt(2, orderId);
        updateStmt1.setString(3, addISBN);
        updateStmt1.executeUpdate();


        //update 2
        PreparedStatement updateStmt2 = connection.prepareStatement(sql2);
        updateStmt2.setString(1, dateString);
        updateStmt2.setInt(2,orderId);
        updateStmt2.executeUpdate();


        //query charge
        PreparedStatement queryStatement = connection.prepareStatement(qCharge);
        queryStatement.setString(1, addISBN);
        ResultSet rs = queryStatement.executeQuery();

        
        int unit_charge = -1;
        while (rs.next()){
            unit_charge = rs.getInt("unit_price");
        }

        int charge = (unit_charge+10) * addAmount;

        if (quantity == 0) {
            charge += 10;
        }
        //update 3
        PreparedStatement updateStmt3 = connection.prepareStatement(sql3);
        updateStmt3.setInt(1,charge);
        updateStmt3.setInt(2,orderId);
        updateStmt3.executeUpdate();

        //decrement inventory 
        PreparedStatement incrStmt = connection.prepareStatement(incrementString);
        incrStmt.setInt(1,addAmount);
        incrStmt.setString(2,addISBN);
        incrStmt.executeUpdate();


        connection.commit();

        
        System.out.printf("Successfully added %d books for book '%s', added charge: %d, date at alteration: %s\n", addAmount, addISBN, charge, dateString);

       } catch (SQLException e) {System.out.printf("During the update of order, SQL Exception occured: %s", e);}
    }

    private static void subBooks(Connection connection, int delAmount, int orderId, String delISBN, Date currentDate) {

        //check if the decrement is too large
        String check = "SELECT QUANTITY FROM ORDERING WHERE (ORDER_ID = ? AND ISBN = ?)";
        //retrieve date string 
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = sdf.format(currentDate);

        //update ordering table
        String sql = "UPDATE ordering SET QUANTITY = QUANTITY - ? WHERE ORDER_ID = ? AND ISBN = ?";

        //update to new date 
        String sql2 = "UPDATE orders SET o_date = to_date(?, 'YYYY-MM-DD') WHERE ORDER_ID = ?";

        //update to new charge
        String sql3 = "UPDATE orders SET CHARGE = CHARGE - ? WHERE ORDER_ID = ?";

        String qCharge = "SELECT unit_price FROM Books WHERE ISBN = ?";

        String incrementString = "UPDATE books SET no_of_copies = no_of_copies + ? WHERE ISBN = ?";
       try {
        connection.setAutoCommit(false);

        //check 
        PreparedStatement checkStmt = connection.prepareStatement(check);
        checkStmt.setInt(1, orderId);
        checkStmt.setString(2, delISBN);
        ResultSet rs = checkStmt.executeQuery();
        int originalQ = -1;
        while (rs.next()){
            originalQ = rs.getInt("Quantity");
        }

        if (originalQ <  delAmount) {
            System.out.println("The original quantity is less than the requested remove amount.");
            return;
        }

        //update 1
        PreparedStatement updateStmt1 = connection.prepareStatement(sql);
        updateStmt1.setInt(1, delAmount);
        updateStmt1.setInt(2, orderId);
        updateStmt1.setString(3, delISBN);
        updateStmt1.executeUpdate();


        //update 2
        PreparedStatement updateStmt2 = connection.prepareStatement(sql2);
        updateStmt2.setString(1, dateString);
        updateStmt2.setInt(2,orderId);
        updateStmt2.executeUpdate();


        //query charge
        PreparedStatement queryStatement = connection.prepareStatement(qCharge);
        queryStatement.setString(1, delISBN);
        ResultSet rs2 = queryStatement.executeQuery();

        
        int unit_charge = -1;
        while (rs2.next()){
            unit_charge = rs2.getInt("unit_price");
        }

        int charge = (unit_charge + 10) * delAmount;
        if (delAmount == originalQ){
            charge += 10;
        }

        //update 3
        PreparedStatement updateStmt3 = connection.prepareStatement(sql3);
        updateStmt3.setInt(1,charge);
        updateStmt3.setInt(2,orderId);
        updateStmt3.executeUpdate();
        System.out.println("3");
        //decrement inventory 
        PreparedStatement incrStmt = connection.prepareStatement(incrementString);
        incrStmt.setInt(1,delAmount);
        incrStmt.setString(2,delISBN);
        incrStmt.executeUpdate();
        System.out.println("4");


        connection.commit();

        
        System.out.printf("Successfully removed %d books for book '%s', decremented charge: %d, date at alteration: %s\n", delAmount, delISBN, charge, dateString);

       } catch (SQLException e) {System.out.printf("During the update of order, Exception occured: %s", e);}
    }
    private static void orderQuery(Connection connection, String customerId, int year){

        try{
            if (!isValidCustomer(customerId, connection)) {
                System.out.println("Invalid Customer."); 
                return;
            }
        }
        catch (SQLException e) {System.out.printf("During order query Exception occured: %s", e);}


        String sql = "SELECT O.order_id, O.o_date, O.charge, O.shipping_status " +
                "FROM orders O " +
                "WHERE TRIM(O.customer_id) = ? " +
                "AND EXTRACT(YEAR FROM O.o_date) = ? " +
                "ORDER BY O.order_id ASC";

        
            try {
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, customerId);
                statement.setInt(2, year);
                ResultSet resultSet = statement.executeQuery();
                    int recordNumber = 1;
                    while (resultSet.next()) {
                        System.out.println("Record: " + recordNumber);

                        System.out.println("Order ID: " + String.format("%08d",resultSet.getInt("order_id")));
                        System.out.println("Order Date: " + resultSet.getDate("o_date"));
                        System.out.println("Charge: " + resultSet.getDouble("charge"));
                        System.out.println("Shipping Status: " + resultSet.getString("shipping_status") + "\n");

                        recordNumber++;
                    }
                } catch (SQLException e) {
            System.out.println("Error executing order query: " + e.getMessage());
        }
    }

   
    

}
