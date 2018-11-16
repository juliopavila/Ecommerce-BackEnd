/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javp.org;

import DataBase.DBConnection;
import DataBase.PasswordEncrypt;
import DataBase.PropsManager;
import Proxy.SaveAttr;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Julio Avila
 */
@WebServlet(name = "Login", urlPatterns = {"/Login"})
public class Login extends HttpServlet {
    protected JSONObject myjson;
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            myjson = new JSONObject(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
            try {
                validLogin(DBConnection.getConnection(), myjson, request, response);
            } 
            catch (ClassNotFoundException | NoSuchAlgorithmException | SQLException ex) {
                Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
            }
      
    }
    private void validLogin(Connection connection, JSONObject myjson, HttpServletRequest request, HttpServletResponse response) throws IOException, NoSuchAlgorithmException, SQLException {
        System.out.println("Entrando a valid login");
        response.setContentType("aplication/json");
        PropsManager props = PropsManager.getInstance();
        PrintWriter out = response.getWriter();
        PreparedStatement stmt = null;
        JSONObject myjsonr = new JSONObject();
        PasswordEncrypt myhash = new PasswordEncrypt();
        String encryptedpw = null;
        String loginQuery = props.getProps("login");
        HttpSession mySession;
        try{
            encryptedpw = PasswordEncrypt.getMD5(myjson.getString("password"));
            stmt = connection.prepareStatement(loginQuery);
            stmt.setString(1, (String) myjson.get("username"));
            stmt.setString(2,encryptedpw);
            System.out.println("Este es el query ---->"+stmt.toString());   
            ResultSet rs = stmt.executeQuery();
            SaveAttr saveAttr = new SaveAttr();
            if(rs.next()){
                String user = myjson.getString("username");
                Integer user_id = rs.getInt("user_id");
                mySession = request.getSession();
                // se supone que aqui guardo proxy
                saveAttr.saveAttr(mySession, "user_username", user);
                saveAttr.saveAttr(mySession, "user_id", user_id);
                // se supone que aqui guardo proxy
                System.out.println("El value de Attr es: ->"+mySession.getAttribute("user_username"));
                System.out.println("El value de Attr es: ->"+mySession.getAttribute("user_id"));
                myjsonr.put("success", true).put("user_id",user_id).put("status", 200).put("user_username",user).put("message", "Login successful");                    
                System.out.println("Nombre de la sesion --->"+mySession.getAttribute("user_username"));
            }//Final del rs.next
            else{
                myjsonr.put("success", false).put("status",404).put("message", "No se ha registrado");
            }//Final del else del rs.next
            out.print(myjsonr.toString());
        }//Final del try
        catch(SQLException | NoSuchAlgorithmException | JSONException e){
            System.out.println("Error ... -> " + e.getMessage());
        }//Final del catch  
    }
}