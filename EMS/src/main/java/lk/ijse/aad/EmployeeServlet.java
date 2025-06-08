package lk.ijse.aad;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.dbcp2.BasicDataSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebServlet("/employee")
public class EmployeeServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String,String> user =
                mapper.readValue(request.getInputStream(), Map.class);

        ServletContext sc = request.getServletContext();
        BasicDataSource ds= (BasicDataSource) sc.getAttribute("ds");
        try {
            Connection connection=ds.getConnection();
            PreparedStatement pstm=connection.prepareStatement(
                    "INSERT INTO employee (eid, ename, enumber, eaddress, edepartment, estatus) VALUES (?, ?, ?, ?, ?, ?)"
            );
            pstm.setString(1, UUID.randomUUID().toString());
            pstm.setString(2,user.get("ename"));
            pstm.setString(3,user.get("enumber"));
            pstm.setString(4,user.get("eaddress"));
            pstm.setString(5,user.get("edepartment"));
            pstm.setString(6,user.get("estatus"));
            int executed = pstm.executeUpdate();

            PrintWriter out = response.getWriter();
            response.setContentType("application/json");
            if (executed > 0) {
                response.setStatus(HttpServletResponse.SC_OK);
                mapper.writeValue(out, Map.of(
                        "code","200",
                        "status","success",
                        "message","Employee successfully created!"
                ));
            }else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                mapper.writeValue(out, Map.of(
                        "code","400",
                        "status","error",
                        "message","Username already exists!"
                ));
            }
            connection.close();
        } catch (SQLException e) {
            PrintWriter out = response.getWriter();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(out, Map.of(
                    "code","500",
                    "status","error",
                    "message","Internal server error!"
            ));
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        response.setContentType("application/json");

        ServletContext sc = request.getServletContext();
        BasicDataSource ds = (BasicDataSource) sc.getAttribute("ds");

        try (Connection connection = ds.getConnection()) {
            PreparedStatement pstm = connection.prepareStatement(
                    "SELECT eid, ename, enumber, eaddress, edepartment, estatus FROM employee"
            );
            ResultSet rs = pstm.executeQuery();

            List<Map<String, String>> employees = new ArrayList<>();

            while (rs.next()) {
                Map<String, String> emp = Map.of(
                        "eid", rs.getString("eid"),
                        "ename", rs.getString("ename"),
                        "enumber", rs.getString("enumber"),
                        "eaddress", rs.getString("eaddress"),
                        "edepartment", rs.getString("edepartment"),
                        "estatus", rs.getString("estatus")
                );
                employees.add(emp);
            }

            PrintWriter out = response.getWriter();
            response.setStatus(HttpServletResponse.SC_OK);
            mapper.writeValue(out, Map.of(
                    "code", "200",
                    "status", "success",
                    "data", employees
            ));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(response.getWriter(), Map.of(
                    "code", "500",
                    "status", "error",
                    "message", "Internal server error!"
            ));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> user = mapper.readValue(request.getInputStream(), Map.class);

        ServletContext sc = request.getServletContext();
        BasicDataSource ds = (BasicDataSource) sc.getAttribute("ds");

        try (Connection connection = ds.getConnection()) {
            PreparedStatement pstm = connection.prepareStatement(
                    "UPDATE employee SET ename = ?, enumber = ?, eaddress = ?, edepartment = ?, estatus = ? WHERE eid = ?"
            );

            pstm.setString(1, user.get("ename"));
            pstm.setString(2, user.get("enumber"));
            pstm.setString(3, user.get("eaddress"));
            pstm.setString(4, user.get("edepartment"));
            pstm.setString(5, user.get("estatus"));
            pstm.setString(6, user.get("eid"));

            int executed = pstm.executeUpdate();

            PrintWriter out = response.getWriter();
            response.setContentType("application/json");

            if (executed > 0) {
                response.setStatus(HttpServletResponse.SC_OK);
                mapper.writeValue(out, Map.of(
                        "code", "200",
                        "status", "success",
                        "message", "Employee successfully updated!"
                ));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(out, Map.of(
                        "code", "404",
                        "status", "error",
                        "message", "Employee not found!"
                ));
            }
        } catch (SQLException e) {
            PrintWriter out = response.getWriter();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(out, Map.of(
                    "code", "500",
                    "status", "error",
                    "message", "Internal server error!"
            ));
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        String eid = request.getParameter("eid");

        if (eid == null || eid.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(), Map.of(
                    "code", "400",
                    "status", "error",
                    "message", "Employee ID is required!"
            ));
            return;
        }

        ServletContext sc = request.getServletContext();
        BasicDataSource ds = (BasicDataSource) sc.getAttribute("ds");

        try (Connection connection = ds.getConnection()) {
            PreparedStatement pstm = connection.prepareStatement(
                    "DELETE FROM employee WHERE eid = ?"
            );
            pstm.setString(1, eid);

            int executed = pstm.executeUpdate();

            PrintWriter out = response.getWriter();
            response.setContentType("application/json");

            if (executed > 0) {
                response.setStatus(HttpServletResponse.SC_OK);
                mapper.writeValue(out, Map.of(
                        "code", "200",
                        "status", "success",
                        "message", "Employee successfully deleted!"
                ));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(out, Map.of(
                        "code", "404",
                        "status", "error",
                        "message", "Employee not found!"
                ));
            }
        } catch (SQLException e) {
            PrintWriter out = response.getWriter();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(out, Map.of(
                    "code", "500",
                    "status", "error",
                    "message", "Internal server error!"
            ));
            throw new RuntimeException(e);
        }
    }

}
