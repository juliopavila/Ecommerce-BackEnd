
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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Julio Avila
 */
@WebServlet(name = "ChangePass", urlPatterns = {"/ChangePass"})
public class ChangePass extends HttpServlet {
    protected JSONObject myjson;
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        myjson = new JSONObject(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
            try {
                update(DBConnection.getConnection(), request, response);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(SignUp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(UDusers.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
        private void update(Connection connection, HttpServletRequest request, HttpServletResponse response) throws IOException, NoSuchAlgorithmException {
        PropsManager myprops = PropsManager.getInstance();
        PreparedStatement mySt = null;
        PasswordEncrypt myhash = new PasswordEncrypt();
        String encryptedpw = null;
        String changepassword = myprops.getProps("changepass");
        PrintWriter out = response.getWriter();
        try { 
            encryptedpw = PasswordEncrypt.getMD5(myjson.getString("password"));            
            mySt = connection.prepareStatement(changepassword);
            mySt.setString(1, encryptedpw);
            mySt.setInt(2, myjson.getInt("user_id"));
            mySt.executeUpdate(); //use if no data will be returned... else use, executeQuery();
            System.out.println("Actualizado con exito");
            myjson.put("status", 200);
        } catch (SQLException | JSONException e) {
            System.out.println("Error al conectar... -> " + e.getMessage());
            myjson.put("success", false);
        }
        out.print(myjson.toString());
    }
}
