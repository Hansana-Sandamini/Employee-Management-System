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
import java.util.Map;

@WebServlet("/signin")
public class SignInServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String,String> user =
                mapper.readValue(request.getInputStream(), Map.class);

        ServletContext sc = request.getServletContext();
        BasicDataSource ds= (BasicDataSource) sc.getAttribute("ds");

        String email=user.get("uemail");
        String password=user.get("upassword");
        System.out.println("email:"+email+"password:"+password);

        try {
            Connection connection=ds.getConnection();
            PreparedStatement pstm=connection.prepareStatement(
                    "SELECT * FROM systemusers WHERE upassword=? AND uemail =?");
            pstm.setString(1, password);
            pstm.setString(2, email);

            ResultSet rs=pstm.executeQuery();
            response.setContentType("application/json");
            PrintWriter out=response.getWriter();
            if(rs.next()){
                response.setStatus(HttpServletResponse.SC_OK);
                mapper.writeValue(out,Map.of(
                        "code","200",
                        "status","Login Success",
                        "message","You have been logged in successfully"
                ));
            }else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                mapper.writeValue(out,Map.of(
                        "code","401",
                        "status","Unauthorized",
                        "message","Unauthorized Behaviour"
                ));
            }
            connection.close();
        } catch (SQLException e) {
            PrintWriter out=response.getWriter();
            mapper.writeValue(out,Map.of(
                    "code","500",
                    "status","Error",
                    "message",e.getMessage()
            ));
            throw new RuntimeException(e);
        }

    }
}
