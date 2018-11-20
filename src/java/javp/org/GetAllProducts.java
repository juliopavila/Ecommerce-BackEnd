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
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Julio Avila
 */
@WebServlet(name = "GetAllProducts", urlPatterns = {"/GetAllProducts"})
public class GetAllProducts extends HttpServlet {

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

    private void read(Connection connection, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        System.out.println("Entrando al metodo leer todos productos");
        response.setContentType("aplication/json");
        PropsManager props = PropsManager.getInstance();
        PrintWriter out = response.getWriter();
        JSONArray myjsonarray = new JSONArray();
        PreparedStatement stmt = null;
        String getproduct = props.getProps("getallproduct");
        ResultSetMetaData rsmd = null;
        //Importamos el id del usuario para realizar la consulta
        try {
            stmt = connection.prepareStatement(getproduct);
            ResultSet rs = stmt.executeQuery();
            rsmd = rs.getMetaData();//Importamos la Meta Data
            while (rs.next()) {
                JSONObject json = new JSONObject();
                String imgName = rs.getString("product_img_name");
                for (int i = 1; i < rsmd.getColumnCount(); i++) {
                    json.put(rsmd.getColumnLabel(i), rs.getObject(i)).put("status", 200).put("img_name", imgName);
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
}
