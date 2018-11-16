
package javp.org;

import DataBase.DBConnection;
import DataBase.PasswordEncrypt;
import DataBase.PropsManager;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;

@WebServlet(name = "SignUp", urlPatterns = {"/SignUp"})
@MultipartConfig
public class SignUp extends HttpServlet {	
    protected PropsManager props = PropsManager.getInstance();
    private static final long serialVersionUID = 1L;
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
          System.out.println("Recibi un header");
            try {
                dbSingUp(DBConnection.getConnection(), request, response);
            } catch (ClassNotFoundException | NoSuchAlgorithmException ex) {
                Logger.getLogger(SignUp.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    private void dbSingUp(Connection connection, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException, NoSuchAlgorithmException 
    {
        //Instanciamos el PropsManager
        System.out.println("Estoy ingresando a dbSignUp del shopping cart");
        JSONObject myjson;
        myjson = new JSONObject(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
        JSONObject json = new JSONObject();
        PropsManager props = PropsManager.getInstance();
        java.util.Date date = new java.util.Date();//Para obtener la forma correcta de formato de fecha para postgres
        java.sql.Date finaldate = new java.sql.Date(date.getTime());
        String name = myjson.getString("name");
        String lastname = myjson.getString("lastname");
        String email = myjson.getString("email");
        String username = myjson.getString("username");
        String password = myjson.getString("password");
        PreparedStatement pstmt = null;
        String encryptedpw = null;
        String signupQuery = props.getProps("signup");
        System.out.println("Query -> "+signupQuery);
        PrintWriter out = response.getWriter();	
        try {
            encryptedpw = PasswordEncrypt.getMD5(password); // Encriptamiento de la clave
            System.out.println("Password Encryptado ---->"+encryptedpw);
            pstmt = connection.prepareStatement(signupQuery);
            pstmt.setString(1, name);
            pstmt.setString(2, lastname);
            pstmt.setString(3, username);
            pstmt.setString(4, email);
            pstmt.setString(5, encryptedpw);
            pstmt.setDate(6, finaldate);
            System.out.println("Este es el query ---->"+pstmt.toString());        
            pstmt.executeUpdate();
            System.out.println("Agregado con exito a la Base de datos");
            json.put("status", 200);
            } 
        catch (SQLException | JSONException e) {
            System.out.println("Error al conectar..."+e.getMessage());
            json.put("status", 404);
        }
        out.print(json.toString());	
    }
}
