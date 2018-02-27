package nu.te4.utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory
{
    public static Connection getConnection() throws SQLException, ClassNotFoundException
    {
        String url = "jdbc:mysql://localhost/sebastian_recipe_db";
        String username = "root";
        String password = "te4te4te4";
        Class.forName("com.mysql.jdbc.Driver");
        return DriverManager.getConnection(url, username, password);
    }
}
