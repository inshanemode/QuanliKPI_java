package quanlikpi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConnection {
    // Database connection parameters
    private static final String SERVER_NAME = "localhost";
    private static final String INSTANCE_NAME = "SQLEXPRESS";
    private static final String DATABASE_NAME = "KPI_Management";
    
    // JDBC URL for Windows Authentication
    // Using instanceName= instead of backslash in hostname can be more reliable
    private static final String CONNECTION_URL = 
        "jdbc:sqlserver://" + SERVER_NAME + ";" +
        "instanceName=" + INSTANCE_NAME + ";" +
        "databaseName=" + DATABASE_NAME + ";" +
        "integratedSecurity=true;" +
        "encrypt=true;" +
        "trustServerCertificate=true;";

    /**
     * Get a connection to the SQL Server database.
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load the MS SQL Server JDBC driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(CONNECTION_URL);
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
            throw new SQLException("SQL Server JDBC Driver not found", e);
        }
    }

    /**
     * Main method for testing the connection.
     */
    public static void main(String[] args) {
        System.out.println("Đang kết nối tới: " + SERVER_NAME + "...");
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("Kết nối thành công tới database: " + DATABASE_NAME);
            }
        } catch (SQLException e) {
            System.err.println("Kết nối thất bại!");
            System.err.println("Lỗi: " + e.getMessage());
            
            // Helpful hints for common issues
            if (e.getMessage().contains("integratedSecurity")) {
                System.err.println("Gợi ý: Đảm bảo file 'mssql-jdbc_auth-13.2.1.x64.dll' nằm trong java.library.path.");
            }
        }
    }
}
