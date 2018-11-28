package javp.org;

import DataBase.DBConnection;
import DataBase.PropsManager;
import java.io.IOException;
import java.io.PrintWriter;
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
import javax.servlet.http.Part;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Julio Avila
 */
@WebServlet(name = "CRUDComments", urlPatterns = {"/CRUDComments"})
public class CRUDComments extends HttpServlet {

    protected JSONObject json;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            read(DBConnection.getConnection(), request, response);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SignUp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        json = new JSONObject(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
        try {
            add(DBConnection.getConnection(), request, response);
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
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
        response.setContentType("application/json");
        json = new JSONObject(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
        try {
            update(DBConnection.getConnection(), request, response);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SignUp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    private void read(Connection connection, HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("Entrando al metodo leer comentarios");
        response.setContentType("aplication/json");
        PropsManager props = PropsManager.getInstance();
        PrintWriter out = response.getWriter();
        JSONArray myjsonarray = new JSONArray();
        PreparedStatement stmt = null;
        Integer product_id = Integer.parseInt(request.getParameter("product_id"));
        String getcomments = props.getProps("getcomments");
        ResultSetMetaData rsmd = null;
        //Importamos el id del usuario para realizar la consulta
        try {
            stmt = connection.prepareStatement(getcomments);
            stmt.setInt(1, product_id);
            ResultSet rs = stmt.executeQuery();
            rsmd = rs.getMetaData();//Importamos la Meta Data
            while (rs.next()) {
                JSONObject json = new JSONObject();
                for (int i = 1; i < rsmd.getColumnCount(); i++) {
                    json.put(rsmd.getColumnLabel(i), rs.getObject(i)).put("status", 200);
                    System.out.println("JSON->" + json);
                }
                myjsonarray.put(json);
                System.out.println("JSONArray ----->" + myjsonarray);
            }
            out.print(myjsonarray);
        }//Final del try
        catch (SQLException | JSONException e) {
            System.out.println("Error ... -> " + e.getMessage());
        }
    }

    private void add(Connection connection, HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("Entrando al metodo para agregar un comentario");
        //Importamos los campos
        String comment_content = json.getString("comment_content");
        Integer user_id = json.getInt("user_id");
        Integer product_id = json.getInt("product_id");
        JSONObject myjson = new JSONObject();
        PreparedStatement stmt = null;
        PropsManager props = PropsManager.getInstance();
        String addcomment = props.getProps("addcomment");
        PrintWriter out = response.getWriter();
        HttpSession mySession;
        java.util.Date date = new java.util.Date();//Para obtener la forma correcta de formato de fecha para postgres
        java.sql.Date finaldate = new java.sql.Date(date.getTime());
        try {
            mySession = request.getSession();
            stmt = connection.prepareStatement(addcomment);
            stmt.setInt(1, user_id);
            stmt.setInt(2, product_id);
            stmt.setString(3, comment_content);
            stmt.setDate(4, finaldate);
            System.out.println("Este es el query del add product ---->" + stmt.toString());
            stmt.executeUpdate();
            System.out.println("Agregado con exito a la Base de datos");
            myjson.put("status", 200);
            //Datos para el userboard
        } catch (SQLException | JSONException e) {
            System.out.println("Error al conectar..." + e.getMessage());
            myjson.put("status", 404);
        }//Final del catch
        out.print(myjson.toString());
    }

    private void delete(Connection connection, HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("Entrando al metodo eliminar comentario");
        response.setContentType("aplication/json");
        PropsManager props = PropsManager.getInstance();
        PrintWriter out = response.getWriter();
        PreparedStatement stmt = null;
        String deletecomment = props.getProps("deletecomment");
        JSONObject myjson = new JSONObject();
        HttpSession mySession;
        //Importamos los campos
        System.out.println("antes del id");
        Integer comment_id = Integer.parseInt(request.getParameter("comment_id"));
        try {
            mySession = request.getSession();
            stmt = connection.prepareStatement(deletecomment);
            stmt.setInt(1, comment_id);
            System.out.println("Este es el query del delete ---->" + stmt.toString());
            stmt.executeUpdate();
            System.out.println("Eliminado con exito a la Base de datos");
            myjson.put("status", 200);
        } catch (SQLException | JSONException e) {
            System.out.println("ERROR AL CONECTAR... -> " + e.getMessage());
            myjson.put("status", 404);
        }
        out.print(myjson.toString());   
    }

    private void update(Connection connection, HttpServletRequest request, HttpServletResponse response) throws IOException {
        PropsManager myprops = PropsManager.getInstance();
        PreparedStatement mySt = null;
        String updtecomment = myprops.getProps("updtecomment");
        PrintWriter out = response.getWriter();
        try {            
            mySt = connection.prepareStatement(updtecomment);
            mySt.setString(1, json.getString("comment_content"));
            mySt.setInt(2, json.getInt("comment_id"));
            mySt.executeUpdate(); //use if no data will be returned... else use, executeQuery();
            System.out.println("Actualizado con exito");
            json.put("status", 200);
        } catch (SQLException | JSONException e) {
            System.out.println("Error al conectar... -> " + e.getMessage());
            json.put("success", false);
        }
        out.print(json.toString());
    }

}
