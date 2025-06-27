package lk.ijse.aad;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.apache.commons.dbcp2.BasicDataSource;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

@WebServlet("/employee")
@MultipartConfig
public class EmployeeServlet extends HttpServlet {

    private String uploadPath;

    @Override
    public void init() {
        uploadPath = getServletContext().getRealPath("") + File.separator + "uploads";
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();

        try {
            String ename = request.getParameter("ename");
            String enumber = request.getParameter("enumber");
            String eaddress = request.getParameter("eaddress");
            String edepartment = request.getParameter("edepartment");
            String estatus = request.getParameter("estatus");

            // Handle file upload
            Part filePart = request.getPart("eimage");
            String fileName = null;
            if (filePart != null && filePart.getSize() > 0) {
                fileName = UUID.randomUUID().toString() + "-" + getFileName(filePart);
                filePart.write(uploadPath + File.separator + fileName);
            }

            ServletContext sc = request.getServletContext();
            BasicDataSource ds = (BasicDataSource) sc.getAttribute("ds");

            try (Connection connection = ds.getConnection()) {
                PreparedStatement pstm = connection.prepareStatement(
                        "INSERT INTO employee (eid, ename, enumber, eaddress, edepartment, estatus, eimage) VALUES (?, ?, ?, ?, ?, ?, ?)"
                );
                pstm.setString(1, UUID.randomUUID().toString());
                pstm.setString(2, ename);
                pstm.setString(3, enumber);
                pstm.setString(4, eaddress);
                pstm.setString(5, edepartment);
                pstm.setString(6, estatus);
                pstm.setString(7, fileName);

                int executed = pstm.executeUpdate();

                if (executed > 0) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    mapper.writeValue(out, Map.of(
                            "code", "200",
                            "status", "success",
                            "message", "Employee created successfully!"
                    ));
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    mapper.writeValue(out, Map.of(
                            "code", "400",
                            "status", "error",
                            "message", "Failed to create employee"
                    ));
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(out, Map.of(
                    "code", "500",
                    "status", "error",
                    "message", "Server error: " + e.getMessage()
            ));
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();

        ServletContext sc = request.getServletContext();
        BasicDataSource ds = (BasicDataSource) sc.getAttribute("ds");

        try (Connection connection = ds.getConnection()) {
            PreparedStatement pstm = connection.prepareStatement(
                    "SELECT eid, ename, enumber, eaddress, edepartment, estatus, eimage FROM employee"
            );
            ResultSet rs = pstm.executeQuery();

            List<Map<String, String>> employees = new ArrayList<>();

            while (rs.next()) {
                String imagePath = rs.getString("eimage");
                String imageUrl = imagePath != null ?
                        request.getContextPath() + "/uploads/" + imagePath :
                        null;

                Map<String, String> emp = new HashMap<>();
                emp.put("eid", rs.getString("eid"));
                emp.put("ename", rs.getString("ename"));
                emp.put("enumber", rs.getString("enumber"));
                emp.put("eaddress", rs.getString("eaddress"));
                emp.put("edepartment", rs.getString("edepartment"));
                emp.put("estatus", rs.getString("estatus"));
                emp.put("eimage", imageUrl);
                employees.add(emp);
            }

            response.setStatus(HttpServletResponse.SC_OK);
            mapper.writeValue(out, Map.of(
                    "code", "200",
                    "status", "success",
                    "data", employees
            ));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(out, Map.of(
                    "code", "500",
                    "status", "error",
                    "message", "Failed to fetch employees"
            ));
            e.printStackTrace();
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();

        try {
            String eid = request.getParameter("eid");
            String ename = request.getParameter("ename");
            String enumber = request.getParameter("enumber");
            String eaddress = request.getParameter("eaddress");
            String edepartment = request.getParameter("edepartment");
            String estatus = request.getParameter("estatus");

            // Handle file upload if present
            Part filePart = request.getPart("eimage");
            String fileName = null;
            if (filePart != null && filePart.getSize() > 0) {
                fileName = UUID.randomUUID().toString() + "-" + getFileName(filePart);
                filePart.write(uploadPath + File.separator + fileName);
            }

            ServletContext sc = request.getServletContext();
            BasicDataSource ds = (BasicDataSource) sc.getAttribute("ds");

            try (Connection connection = ds.getConnection()) {
                String sql;
                PreparedStatement pstm;

                if (fileName != null) {
                    sql = "UPDATE employee SET ename=?, enumber=?, eaddress=?, edepartment=?, estatus=?, eimage=? WHERE eid=?";
                    pstm = connection.prepareStatement(sql);
                    pstm.setString(1, ename);
                    pstm.setString(2, enumber);
                    pstm.setString(3, eaddress);
                    pstm.setString(4, edepartment);
                    pstm.setString(5, estatus);
                    pstm.setString(6, fileName);
                    pstm.setString(7, eid);
                } else {
                    sql = "UPDATE employee SET ename=?, enumber=?, eaddress=?, edepartment=?, estatus=? WHERE eid=?";
                    pstm = connection.prepareStatement(sql);
                    pstm.setString(1, ename);
                    pstm.setString(2, enumber);
                    pstm.setString(3, eaddress);
                    pstm.setString(4, edepartment);
                    pstm.setString(5, estatus);
                    pstm.setString(6, eid);
                }

                int executed = pstm.executeUpdate();

                if (executed > 0) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    mapper.writeValue(out, Map.of(
                            "code", "200",
                            "status", "success",
                            "message", "Employee updated successfully!"
                    ));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    mapper.writeValue(out, Map.of(
                            "code", "404",
                            "status", "error",
                            "message", "Employee not found"
                    ));
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(out, Map.of(
                    "code", "500",
                    "status", "error",
                    "message", "Server error: " + e.getMessage()
            ));
            e.printStackTrace();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();

        String eid = request.getParameter("eid");
        if (eid == null || eid.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(out, Map.of(
                    "code", "400",
                    "status", "error",
                    "message", "Employee ID is required"
            ));
            return;
        }

        ServletContext sc = request.getServletContext();
        BasicDataSource ds = (BasicDataSource) sc.getAttribute("ds");

        try (Connection connection = ds.getConnection()) {
            // First get the image path to delete the file
            String imagePath = null;
            PreparedStatement selectStmt = connection.prepareStatement(
                    "SELECT eimage FROM employee WHERE eid = ?"
            );
            selectStmt.setString(1, eid);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                imagePath = rs.getString("eimage");
            }

            // Delete the employee record
            PreparedStatement deleteStmt = connection.prepareStatement(
                    "DELETE FROM employee WHERE eid = ?"
            );
            deleteStmt.setString(1, eid);
            int executed = deleteStmt.executeUpdate();

            if (executed > 0) {
                // Delete the associated image file if it exists
                if (imagePath != null) {
                    File imageFile = new File(uploadPath + File.separator + imagePath);
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                }

                response.setStatus(HttpServletResponse.SC_OK);
                mapper.writeValue(out, Map.of(
                        "code", "200",
                        "status", "success",
                        "message", "Employee deleted successfully!"
                ));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(out, Map.of(
                        "code", "404",
                        "status", "error",
                        "message", "Employee not found"
                ));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(out, Map.of(
                    "code", "500",
                    "status", "error",
                    "message", "Server error: " + e.getMessage()
            ));
            e.printStackTrace();
        }
    }

    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return "";
    }
}