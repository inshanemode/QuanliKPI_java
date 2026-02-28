package quanlikpi.dao;

import quanlikpi.SQLConnection;
import quanlikpi.model.EmployeeKPI;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeKPIDAO {

    public List<EmployeeKPI> getDashboardKPIs(quanlikpi.model.User currentUser) {
        List<EmployeeKPI> kpis = new ArrayList<>();
        String sql = "";
        boolean isAdmin = currentUser.getRoleId() == 1; // 1: ADMIN
        boolean isManager = currentUser.getRoleId() == 2; // 2: MANAGER

        if (isAdmin) {
            sql = "SELECT ek.*, u2.FullName AS AssignedUserName, d.Name AS AssignedDepartmentName " +
                    "FROM Employee_KPIs ek " +
                    "LEFT JOIN Users u2 ON ek.UserId = u2.Id " +
                    "LEFT JOIN Departments d ON ek.DepartmentId = d.Id";
        } else if (isManager) {
            sql = "SELECT ek.*, u2.FullName AS AssignedUserName, d.Name AS AssignedDepartmentName " +
                    "FROM Employee_KPIs ek " +
                    "LEFT JOIN Users u ON u.Id = ? " +
                    "LEFT JOIN Users u2 ON ek.UserId = u2.Id " +
                    "LEFT JOIN Departments d ON ek.DepartmentId = d.Id " +
                    "WHERE ek.DepartmentId = u.DepartmentId OR u2.DepartmentId = u.DepartmentId " +
                    "OR ek.UserId = ?";
        } else {
            // Employee sees personal KPIs + Department generic KPIs
            sql = "SELECT ek.*, u2.FullName AS AssignedUserName, d.Name AS AssignedDepartmentName " +
                    "FROM Employee_KPIs ek " +
                    "LEFT JOIN Users u ON u.Id = ? " +
                    "LEFT JOIN Users u2 ON ek.UserId = u2.Id " +
                    "LEFT JOIN Departments d ON ek.DepartmentId = d.Id " +
                    "WHERE ek.UserId = ? OR ek.DepartmentId = u.DepartmentId";
        }

        try (Connection conn = SQLConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (!isAdmin) {
                if (isManager) {
                    pstmt.setInt(1, currentUser.getId());
                    pstmt.setInt(2, currentUser.getId());
                } else {
                    pstmt.setInt(1, currentUser.getId());
                    pstmt.setInt(2, currentUser.getId());
                }
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    EmployeeKPI kpi = mapResultSetToKPI(rs);
                    kpi.setUserName(rs.getString("AssignedUserName"));
                    kpi.setDepartmentName(rs.getString("AssignedDepartmentName"));
                    kpis.add(kpi);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return kpis;
    }

    public boolean addKPI(EmployeeKPI kpi) {
        String sql = "INSERT INTO Employee_KPIs (CycleId, UserId, DepartmentId, ManagerId, TemplateId, KPI_Name, TargetValue, ActualValue, Unit, Weight, Score, Status) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = SQLConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, kpi.getCycleId());
            if (kpi.getUserId() != null)
                pstmt.setInt(2, kpi.getUserId());
            else
                pstmt.setNull(2, java.sql.Types.INTEGER);
            if (kpi.getDepartmentId() != null)
                pstmt.setInt(3, kpi.getDepartmentId());
            else
                pstmt.setNull(3, java.sql.Types.INTEGER);
            pstmt.setInt(4, kpi.getManagerId());
            if (kpi.getTemplateId() != null)
                pstmt.setInt(5, kpi.getTemplateId());
            else
                pstmt.setNull(5, java.sql.Types.INTEGER);
            pstmt.setString(6, kpi.getKpiName());
            pstmt.setDouble(7, kpi.getTargetValue());
            if (kpi.getActualValue() != null)
                pstmt.setDouble(8, kpi.getActualValue());
            else
                pstmt.setNull(8, java.sql.Types.DOUBLE);
            pstmt.setString(9, kpi.getUnit());
            pstmt.setDouble(10, kpi.getWeight());
            if (kpi.getScore() != null)
                pstmt.setDouble(11, kpi.getScore());
            else
                pstmt.setNull(11, java.sql.Types.DOUBLE);
            pstmt.setString(12, kpi.getStatus());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateKPI(EmployeeKPI kpi) {
        String sql = "UPDATE Employee_KPIs SET CycleId=?, UserId=?, DepartmentId=?, ManagerId=?, TemplateId=?, KPI_Name=?, TargetValue=?, ActualValue=?, Unit=?, Weight=?, Score=?, Status=?, UpdatedAt=GETDATE() WHERE Id=?";
        try (Connection conn = SQLConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, kpi.getCycleId());
            if (kpi.getUserId() != null)
                pstmt.setInt(2, kpi.getUserId());
            else
                pstmt.setNull(2, java.sql.Types.INTEGER);
            if (kpi.getDepartmentId() != null)
                pstmt.setInt(3, kpi.getDepartmentId());
            else
                pstmt.setNull(3, java.sql.Types.INTEGER);
            pstmt.setInt(4, kpi.getManagerId());
            if (kpi.getTemplateId() != null)
                pstmt.setInt(5, kpi.getTemplateId());
            else
                pstmt.setNull(5, java.sql.Types.INTEGER);
            pstmt.setString(6, kpi.getKpiName());
            pstmt.setDouble(7, kpi.getTargetValue());
            if (kpi.getActualValue() != null)
                pstmt.setDouble(8, kpi.getActualValue());
            else
                pstmt.setNull(8, java.sql.Types.DOUBLE);
            pstmt.setString(9, kpi.getUnit());
            pstmt.setDouble(10, kpi.getWeight());
            if (kpi.getScore() != null)
                pstmt.setDouble(11, kpi.getScore());
            else
                pstmt.setNull(11, java.sql.Types.DOUBLE);
            pstmt.setString(12, kpi.getStatus());
            pstmt.setInt(13, kpi.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteKPI(int kpiId) {
        String sql = "DELETE FROM Employee_KPIs WHERE Id = ?";
        try (Connection conn = SQLConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, kpiId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public double getTotalWeightForAssignee(Integer userId, Integer departmentId, int excludeKpiId, int cycleId) {
        String sql = "SELECT SUM(Weight) AS TotalWeight FROM Employee_KPIs WHERE CycleId = ? AND Id != ?";
        if (userId != null) {
            sql += " AND UserId = ?";
        } else if (departmentId != null) {
            sql += " AND DepartmentId = ?";
        } else {
            return 0;
        }

        try (Connection conn = SQLConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cycleId);
            pstmt.setInt(2, excludeKpiId);
            if (userId != null) {
                pstmt.setInt(3, userId);
            } else {
                pstmt.setInt(3, departmentId);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("TotalWeight");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getTotalScoreForAssignee(Integer userId, Integer departmentId, int cycleId) {
        String sql = "SELECT SUM(Score) AS TotalScore FROM Employee_KPIs WHERE CycleId = ?";
        if (userId != null) {
            sql += " AND UserId = ?";
        } else if (departmentId != null) {
            sql += " AND DepartmentId = ?";
        } else {
            return 0;
        }

        try (Connection conn = SQLConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cycleId);
            if (userId != null) {
                pstmt.setInt(2, userId);
            } else {
                pstmt.setInt(2, departmentId);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("TotalScore");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private EmployeeKPI mapResultSetToKPI(ResultSet rs) throws SQLException {
        EmployeeKPI kpi = new EmployeeKPI();
        kpi.setId(rs.getInt("Id"));
        kpi.setCycleId(rs.getInt("CycleId"));
        kpi.setUserId((Integer) rs.getObject("UserId"));
        kpi.setDepartmentId((Integer) rs.getObject("DepartmentId"));
        kpi.setManagerId(rs.getInt("ManagerId"));
        kpi.setTemplateId(rs.getObject("TemplateId") != null ? rs.getInt("TemplateId") : null);
        kpi.setKpiName(rs.getString("KPI_Name"));
        kpi.setTargetValue(rs.getDouble("TargetValue"));
        kpi.setActualValue(rs.getObject("ActualValue") != null ? rs.getDouble("ActualValue") : null);
        kpi.setUnit(rs.getString("Unit"));
        kpi.setWeight(rs.getDouble("Weight"));
        kpi.setScore(rs.getObject("Score") != null ? rs.getDouble("Score") : null);
        kpi.setEvidenceUrl(rs.getString("EvidenceUrl"));
        kpi.setComment(rs.getString("Comment"));
        kpi.setStatus(rs.getString("Status"));
        kpi.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
        kpi.setUpdatedAt(rs.getTimestamp("UpdatedAt").toLocalDateTime());
        return kpi;
    }
}
