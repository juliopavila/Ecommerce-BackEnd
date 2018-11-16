/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javp.org;

import DataBase.DBConnection;
import DataBase.PasswordEncrypt;
import DataBase.PropsManager;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Julio Avila
 */
@WebServlet(name = "UDusers", urlPatterns = {"/UDusers"})
public class UDusers extends HttpServlet {
    protected JSONObject myjson;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            read(DBConnection.getConnection(), request, response);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SignUp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            delete(DBConnection.getConnection(), request, response);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SignUp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(UDusers.class.getName()).log(Level.SEVERE, null, ex);
        }   
    }
    
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

    private void delete(Connection connection, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        System.out.println("entrando a metodo delete user");
        response.setContentType("aplication/json");
        PropsManager props = PropsManager.getInstance();
        PrintWriter out = response.getWriter();
        PreparedStatement stmt = null;
        String deleteuser = props.getProps("deleteuser");
        JSONObject json = new JSONObject();
        HttpSession mySession;
        //Importamos los campos
        Integer user_id = Integer.parseInt(request.getParameter("user_id"));
        try{
            mySession = request.getSession();
            stmt = connection.prepareStatement(deleteuser);
            stmt.setInt(1, user_id);
            System.out.println("Este es el query del delete ---->"+stmt.toString());        
            stmt.executeUpdate();
            System.out.println("Eliminado con exito a la Base de datos");
            json.put("status", 200);
            //Datos para el userboard
        }
        catch(SQLException | JSONException e){
            System.out.println("Error al conectar..."+e.getMessage());
            json.put("status", 404);
        }//Final del catch
        out.print(json.toString());            
    }

    private void update(Connection connection, HttpServletRequest request, HttpServletResponse response) throws IOException, NoSuchAlgorithmException {
        PropsManager myprops = PropsManager.getInstance();
        PreparedStatement mySt = null;
        String encryptedpw = null;
        String updateuser = myprops.getProps("updateuser");
        PrintWriter out = response.getWriter();
        try {            
            mySt = connection.prepareStatement(updateuser);
            mySt.setString(1, myjson.getString("user_name"));
            mySt.setString(2, myjson.getString("user_lastname"));
            mySt.setString(3, myjson.getString("user_email"));
            mySt.setInt(4, myjson.getInt("user_id"));
            mySt.executeUpdate(); //use if no data will be returned... else use, executeQuery();
            System.out.println("Actualizado con exito");
            myjson.put("status", 200);
        } catch (SQLException | JSONException e) {
            System.out.println("Error al conectar... -> " + e.getMessage());
            myjson.put("success", false);
        }
        out.print(myjson.toString());
    }

    private void read(Connection connection, HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("Entrando al metodo leer productos");
        response.setContentType("aplication/json");
        PropsManager props = PropsManager.getInstance();
        PrintWriter out = response.getWriter();
        JSONArray myjsonarray = new JSONArray();
        PreparedStatement stmt = null;
        Integer user_id = Integer.parseInt(request.getParameter("user_id"));
        String getinfo = props.getProps("getuser");
        ResultSetMetaData rsmd = null;
        //Importamos el id del usuario para realizar la consulta
        try{
            stmt = connection.prepareStatement(getinfo);
            stmt.setInt(1, user_id);
            ResultSet rs = stmt.executeQuery();
            rsmd = rs.getMetaData();//Importamos la Meta Data
            while(rs.next()){
                JSONObject json = new JSONObject();
                for (int i = 1; i < rsmd.getColumnCount(); i++) {
                    json.put(rsmd.getColumnLabel(i), rs.getObject(i)).put("status", 200);
                    System.out.println("JSON->"+json);
                }
                myjsonarray.put(json);
                System.out.println("JSONArray ----->"+myjsonarray);
            }           
            out.print(myjsonarray);
	}//Final del try
        catch(SQLException | JSONException e){
            System.out.println("Error ... -> " + e.getMessage());
        }        
    }

}
