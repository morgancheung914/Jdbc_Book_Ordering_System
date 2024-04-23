import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Scanner;
import java.sql.Statement;

public class BSInterface {
    public static void BookStoreInterface(Connection connection, Date currentDate) throws SQLException {
        while (true) {
            System.out.println("* You have entered the Bookstore Interface. *");
            System.out.println("-------------------------");
            System.out.println("Options:");
            System.out.println("1. Order Update");
            System.out.println("2. Order Query");
            System.out.println("3. N Most Popular Book Query");
            System.out.println("4. Back to main menu");
            System.out.println("Please enter your choice (1 - 4):");
            
            Scanner scanner = new Scanner(System.in);

            int parsedOption = App.HandleInput(scanner, 4);
                      
            switch (parsedOption) {
                case 1:
                    System.out.println("Please input the Order ID to be updated: ");
                    String orderID = scanner.nextLine();
                    
                    try {UpdateOrder(connection, orderID, scanner);} catch (SQLException e) {
                        System.out.printf("During the update query, Exception occured: %s\n", e);
                    }
                    catch (RuntimeException e) {
                        System.out.printf("During the update query, Exception occured: %s\n", e);
                    }
                    break;
                case 2:
                    String q = "";
                    while (true) {
                        System.out.println("Please input the Month for Order Query (e.g.2005-09): ");
                        String str1 = scanner.nextLine();
                        int cut = str1.indexOf('-');
                        String y = "";
                        String m = "";
                        try {
                            y = str1.substring(0, cut);
                            m = str1.substring(cut+1);
                        }
                        catch (StringIndexOutOfBoundsException e){
                            System.out.println("Invalid input. Please enter again.");
                            continue;
                        }
                        try {
                            int Y = Integer.parseInt(y);
                            int M = Integer.parseInt(m);
                            if ((Y >= 0) && (M > 0) && (M <= 12)) {
                                q = y + '/' + m;
                                break;
                            }
                            else {
                                System.out.println("Invalid input. Please enter again.");
                            }
                        }
                        catch (NumberFormatException e) {
                            System.out.println("Invalid input. Please enter again.");
                        }
                    }
                    
                    String query = "SELECT * FROM Orders WHERE to_char(o_date, 'YYYY/MM') = ? AND shipping_status = 'Y'";
                    PreparedStatement stmt2 = connection.prepareStatement(query);
                    stmt2.setString(1, q);
                    int cnt = 1; 
                    int t_charge = 0;
                    ResultSet rs = stmt2.executeQuery();
                    while (rs.next()) {
                        String order_id = rs.getString("order_id");
                        String customer_id = rs.getString("customer_id");
                        String date = rs.getString("o_date");
                        String charge = rs.getString("charge");
                        System.out.println("Record : " + Integer.toString(cnt));
                        System.out.println("order_id : " + order_id);
                        System.out.println("customer_id : " + customer_id);
                        System.out.println("date : " + date);
                        System.out.println("charge : " + charge);
                        System.out.println("\n");
                        cnt += 1;
                        t_charge += Integer.parseInt(charge);
                    }
                    System.out.println("Total charges of the month is " + Integer.toString(t_charge));
                    break;
                case 3:
                    int parsednum;
                    System.out.println("Please input the N popular books number: ");
                    while (true) {
                        String num = scanner.nextLine();
                        try {
                            parsednum = Integer.parseInt(num);
                            if ((parsednum <= 0)) {
                                System.out.println("N must be a postive integer");
                            }
                            else{
                                break;
                            }
                        }
                        catch (NumberFormatException e){
                            System.out.println("Invalid input. Please enter again.");
                        }
                    }
                    String query2 = "CREATE OR REPLACE VIEW temp AS SELECT ISBN, sum(Quantity) AS Total_Quantity FROM Ordering GROUP BY ISBN ORDER BY Total_Quantity DESC\n";
                    String query3 = "SELECT TRIM(title) AS title, B.ISBN, Total_Quantity FROM Books B, (SELECT ISBN, total_quantity from temp WHERE ROWNUM <= ?) T WHERE B.ISBN = T.ISBN ORDER BY Total_Quantity DESC";
                    System.out.println("ISBN            Title             copies");
                    Statement stmt4 = connection.createStatement();
                    PreparedStatement stmt3 = connection.prepareStatement(query3);
                    stmt3.setInt(1, parsednum);
                    stmt4.executeUpdate(query2);
                    ResultSet rs2 = stmt3.executeQuery();
                    while (rs2.next()) {
                        String ISBN = rs2.getString("ISBN");
                        String title = rs2.getString("title");
                        String num_copies = rs2.getString("Total_Quantity");
                        System.out.println(ISBN + "   " + title + "   " + num_copies);
                    }
                    break;
                case 4:
                    return;
                        
            }
        }
    }
    public static void UpdateOrder(Connection connection, String orderID, Scanner scanner) throws SQLException, RuntimeException {
        String sql = "SELECT orders.order_id, shipping_status, SUM(quantity) as BOOK_AMOUNT \n" +
        "FROM orders, ordering WHERE (orders.order_id = ordering.order_id) AND (orders.order_id = ?) \n" +
        "GROUP BY orders.order_Id, shipping_status \n";
        
        int orderNum;
        try{orderNum = Integer.parseInt(orderID);} catch (NumberFormatException e){
            throw new RuntimeException("order id cannot be parsed to integer.");
        }
        if (orderNum < 0){throw new RuntimeException("orderNum must be a positive numeber.");}
        try {
            
            PreparedStatement stmt = connection.prepareStatement(sql);
            
            stmt.setInt(1, orderNum);
            ResultSet res = stmt.executeQuery();
            int sum = -1;
            String status = null;
            String parsedOrderId = null;
            while (res.next()) {
                sum = res.getInt("BOOK_AMOUNT");
                status = res.getString("shipping_status");
                parsedOrderId = res.getString("order_id");
            }
            res.close();

            System.out.printf("The shipping status of %s is %s and %d books ordered.\n", parsedOrderId, status, sum);
            
            if (status.equals("N") && (sum != 0)) {
                System.out.println("Update the shipping status to Yes? (Y)");
                String confirm = scanner.nextLine();
                if (confirm.equals("Y")) {
                    //update clause
                    String updatestmt = "UPDATE Orders SET Orders.shipping_status = 'Y' WHERE order_id = ?";
                    PreparedStatement stmt4 = connection.prepareStatement(updatestmt);
                    stmt4.setString(1, parsedOrderId);
                    stmt4.executeUpdate();
                    System.out.println("Update successful");

                    return;
                } else {
                    return;
                }

            } else {
                System.out.printf("No update is possible.\n");
            }

        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }


}
