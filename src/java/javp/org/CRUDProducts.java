
package javp.org;

import DataBase.DBConnection;
import DataBase.PropsManager;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
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
@WebServlet(name = "CRUDProducts", urlPatterns = {"/CRUDProducts"})
public class CRUDProducts extends HttpServlet {

    protected JSONObject myjson;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            read(DBConnection.getConnection(), request, response);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SignUp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(CRUDProducts.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        myjson = new JSONObject(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
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
        myjson = new JSONObject(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
        try {
            update(DBConnection.getConnection(), request, response);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SignUp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void read(Connection connection, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        System.out.println("Entrando al metodo leer productos");
        response.setContentType("aplication/json");
        PropsManager props = PropsManager.getInstance();
        PrintWriter out = response.getWriter();
        JSONArray myjsonarray = new JSONArray();
        PreparedStatement stmt = null;
        String getproduct = props.getProps("getproduct");
        ResultSetMetaData rsmd = null;
        //Importamos el id del usuario para realizar la consulta
        try{
            stmt = connection.prepareStatement(getproduct);
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

    private void add(Connection connection, HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("Entrando al metodo para agregar un producto");
        response.setContentType("aplication/json");
        PropsManager props = PropsManager.getInstance();
        PrintWriter out = response.getWriter();
        PreparedStatement stmt = null;
        String addproduct = props.getProps("addproduct");
        HttpSession mySession;
        java.util.Date date = new java.util.Date();//Para obtener la forma correcta de formato de fecha para postgres
        java.sql.Date finaldate = new java.sql.Date(date.getTime());
        //Importamos los campos
        String title = myjson.getString("product_title");
        String description = myjson.getString("product_description");
        Integer user_id = myjson.getInt("user_id");
        Integer stock = myjson.getInt("product_stock");
        BigDecimal price = myjson.getBigDecimal("product_price");
        try{
            mySession = request.getSession();
            stmt = connection.prepareStatement(addproduct);
            //Datos para la tabla Board
            stmt.setInt(1, user_id);
            stmt.setString(2, title);
            stmt.setString(3, description);
            stmt.setInt(4, stock);
            stmt.setBigDecimal(5, price);
            stmt.setDate(6, finaldate);
            System.out.println("Este es el query del createnote ---->"+stmt.toString());        
            stmt.executeUpdate();
            System.out.println("Agregado con exito a la Base de datos");
            myjson.put("status", 200);
            //Datos para el userboard
        }
        catch(SQLException | JSONException e){
            System.out.println("Error al conectar..."+e.getMessage());
            myjson.put("status", 404);
        }//Final del catch
        out.print(myjson.toString());
    }

    private void update(Connection connection, HttpServletRequest request, HttpServletResponse response) throws IOException {
        PropsManager myprops = PropsManager.getInstance();
        PreparedStatement mySt = null;
        String updateproduct = myprops.getProps("updateproduct");
        PrintWriter out = response.getWriter();
        try {  
            mySt = connection.prepareStatement(updateproduct);
            mySt.setString(1, myjson.getString("product_title"));
            mySt.setString(2, myjson.getString("product_description"));
            mySt.setInt(3, myjson.getInt("product_stock"));
            mySt.setBigDecimal(4, myjson.getBigDecimal("product_price"));
            mySt.setInt(5, myjson.getInt("product_id"));
            mySt.setInt(6, myjson.getInt("product_stock"));
            mySt.executeUpdate(); //use if no data will be returned... else use, executeQuery();
            System.out.println("Modificado con exito");
            myjson.put("status", 200);
        } catch (SQLException | JSONException e) {
            System.out.println("ERROR AL CONECTAR... -> " + e.getMessage());
            myjson.put("success", false);
        }
        out.print(myjson.toString());	        
    }

    private void delete(Connection connection, HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("Entrando al metodo eliminar producto");
        response.setContentType("aplication/json");
        PropsManager props = PropsManager.getInstance();
        PrintWriter out = response.getWriter();
        PreparedStatement stmt = null;
        String deleteproduct = props.getProps("deleteproduct");
        HttpSession mySession;
        //Importamos los campos
        Integer user_id = Integer.parseInt(request.getParameter("user_id"));
        Integer product_id = Integer.parseInt(request.getParameter("product_id"));
        try{
            mySession = request.getSession();
            stmt = connection.prepareStatement(deleteproduct);
            stmt.setInt(1, user_id);
            stmt.setInt(2, product_id);
            System.out.println("Este es el query del delete ---->"+stmt.toString());        
            stmt.executeUpdate();
            System.out.println("Eliminado con exito a la Base de datos");
            myjson.put("status", 200);
        } catch (SQLException | JSONException e) {
            System.out.println("ERROR AL CONECTAR... -> " + e.getMessage());
            myjson.put("status",404).put("success", false);
        }
        out.print(myjson.toString()); 
    }
}
