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
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import static java.lang.System.out;

@WebServlet("/signup")
public class SignUpServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> user =  mapper.readValue(request.getInputStream(), Map.class);
        ServletContext sc = request.getServletContext();
        BasicDataSource ds = (BasicDataSource) sc.getAttribute("ds");
        try {
            Connection connection = ds.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO systemusers (uid, uname, upassword, uemail) VALUES (?,?,?,?)");
            preparedStatement.setString(1, UUID.randomUUID().toString());
            preparedStatement.setString(2, user.get("uname"));
            preparedStatement.setString(3, user.get("upassword"));
            preparedStatement.setString(4, user.get("uemail"));

            int executed = preparedStatement.executeUpdate();

            PrintWriter out = response.getWriter();
            response.setContentType("application/json");

            if (executed > 0) {
                response.setStatus(HttpServletResponse.SC_OK);
                mapper.writeValue(out, Map.of(
                        "code", "200",
                        "status", "success",
                        "message", "User successfully created!"
                ));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                mapper.writeValue(out, Map.of(
                        "code", "400",
                        "status", "error",
                        "message", "Username already exists!"
                ));
            }
            connection.close();
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(out, Map.of(
                    "code", "500",
                    "status", "error",
                    "message", "Internal Server exists!"
            ));
            e.printStackTrace();
        }
    }
}