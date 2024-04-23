import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


class DatabaseConfig {
    public static String JDBC_URL = "url/to/JDBC";
    public static String USERNAME = "username";
    public static String PASSWORD = "password";
}


public class App {
    private static Date currentDate;
    public static void main(String[] args) throws ClassNotFoundException, SQLException, FileNotFoundException, IOException {

        Connection connection = null;
        /* 
        try {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {

            e.printStackTrace();
        }
        */
        try {
        //Establish Connection
        Class.forName("oracle.jdbc.driver.OracleDriver");
        connection = DriverManager.getConnection(DatabaseConfig.JDBC_URL, DatabaseConfig.USERNAME, DatabaseConfig.PASSWORD);
        
        
        //Current date
        currentDate = new Date();
        while (true) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            String currentDateTime = dateFormat.format(currentDate);    
                
            //Title screen 
            System.out.println("Welcome to the book ordering system!");
            System.out.printf("The System date is %s\n", currentDateTime);
            System.out.println("-------------------------");
            System.out.println("Options:");
            System.out.println("1. System Interface");
            System.out.println("2. Customer Interface");
            System.out.println("3. Bookstore Interface");
            System.out.println("4. Show System Date");
            System.out.println("5. Quit the System");
            System.out.println("Please enter your choice (1 - 5):");
            
            Scanner scanner = new Scanner(System.in);
            int parsedOption = HandleInput(scanner, 5); 

            switch (parsedOption) {
                case 1:
                    SysInterface.systemInterface(connection, currentDate);
                    break;
                case 2:
                    BookstoreApp.customerInterface(connection, currentDate);
                    break;
                case 3:
                    BSInterface.BookStoreInterface(connection, currentDate);
                    break;
                case 4:
                    System.out.printf("Current Date is %s\n", currentDateTime);
                    break;
                case 5:
                    scanner.close(); 
                    connection.close();   
                    return;

        }
        
    
        
    }

        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        } finally {
            if (connection != null) {
                System.out.println("Closing");
                connection.close();
            }

        }   
        
    }

    public static int HandleInput(Scanner scanner, int bound) {
        boolean optionValid = false;
        int parsedOption = -1;
        
        while (!optionValid) {
            
            String optionNum = scanner.nextLine();
            
            try {
                parsedOption = Integer.parseInt(optionNum);
                if ((parsedOption >= 1) && (parsedOption <= bound)) {
                    optionValid = true;
                }
                else {
                    System.out.printf("Integer must be between 1 and %d (inclusive): ", bound);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter an integer: ");
            }
            
        }
        
        return parsedOption;
    }

    public static void setCurrentDate(Connection connection, Date newDate) {
        
        String latestDateString = null;
        String sql = "SELECT O_date FROM ORDERS ORDER BY O_DATE DESC";
        String pattern = "yyyy-MM-dd";
        Date parsedDate = new Date();
        

        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        try{PreparedStatement stmt1 = connection.prepareStatement(sql);
            ResultSet rs = stmt1.executeQuery();
            
            while (rs.next()) {
                latestDateString = rs.getString("O_date");
                break;
            }
        
            parsedDate = dateFormat.parse(latestDateString);
            
            if (parsedDate.after(newDate)) {
                System.out.println("Date cannot be set to later than the date of last order.");
                return;
            } else {
                currentDate = newDate;
                System.out.println("System Date set successfully.");
                return;
            }

        

        

    } catch (SQLException e) {System.out.printf("During the changing of dates, this Exception occured: %s", e);}
    catch (ParseException e) {System.out.printf("During the changing of dates, this Exception occured: %s", e);}

    }
    
}


