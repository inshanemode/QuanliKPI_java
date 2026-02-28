package quanlikpi.dao;

import quanlikpi.SQLConnection;
import quanlikpi.model.Department;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO {

    public List<Department> getAllDepartments() {
        List<Department> departments = new ArrayList<>();
        String sql = "SELECT * FROM Departments";
        try (Connection conn = SQLConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Department dept = new Department();
                dept.setId(rs.getInt("Id"));
                dept.setName(rs.getString("Name"));
                dept.setParentId((Integer) rs.getObject("ParentId"));
                departments.add(dept);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return departments;
    }
}
