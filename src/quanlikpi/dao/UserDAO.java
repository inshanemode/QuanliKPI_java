package quanlikpi.dao;

import quanlikpi.SQLConnection;
import quanlikpi.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User login(String username, String password) {
        String sql = "SELECT * FROM Users WHERE Username = ? AND PasswordHash = ? AND IsActive = 1";
        try (Connection conn = SQLConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password); // Note: In a real app, use Hashing

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM Users";
        try (Connection conn = SQLConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("Id"));
        user.setUsername(rs.getString("Username"));
        user.setPasswordHash(rs.getString("PasswordHash"));
        user.setFullName(rs.getString("FullName"));
        user.setEmail(rs.getString("Email"));
        user.setRoleId(rs.getInt("RoleId"));
        user.setDepartmentId(rs.getObject("DepartmentId") != null ? rs.getInt("DepartmentId") : null);
        user.setManagerId(rs.getObject("ManagerId") != null ? rs.getInt("ManagerId") : null);
        user.setActive(rs.getBoolean("IsActive"));
        user.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
        return user;
    }
}
