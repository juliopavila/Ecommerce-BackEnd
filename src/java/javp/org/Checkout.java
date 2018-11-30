package javp.org;

import DataBase.DBConnection;
import DataBase.PropsManager;
import java.io.IOException;
import java.io.PrintWriter;
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
import javax.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Julio Avila
 */
@WebServlet(name = "Checkout", urlPatterns = {"/Checkout"})
public class Checkout extends HttpServlet {

    protected JSONObject json;
    protected JSONArray myjsonarray;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        myjsonarray = new JSONArray(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
        //json = new JSONObject(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
        try {
            update(DBConnection.getConnection(), request, response);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SignUp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Checkout.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void update(Connection connection, HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        JSONObject myjson = new JSONObject();
        JSONArray cart = new JSONArray();
        PreparedStatement stmt = null;
        PropsManager props = PropsManager.getInstance();
        PrintWriter out = response.getWriter();
        HttpSession mySession;
        String checkout = props.getProps("checkout");
        try {
            mySession = request.getSession();
            connection.setAutoCommit(false);
            for (int i = 0; i < myjsonarray.length(); i++) {
                myjson = myjsonarray.getJSONObject(i);
                System.out.println("" + myjson.get("product_id"));
                stmt = connection.prepareStatement(checkout);
                stmt.setInt(1, myjson.getInt("product_quantity"));
                stmt.setInt(2, myjson.getInt("product_id"));
                System.out.println("query-> " + stmt.toString());
                stmt.executeUpdate();
            }
            connection.commit();
            System.out.println("Exito");
            myjson.put("status", 200);
        } catch (Exception e) {
            // Any error is grounds for rollback
            try {
                connection.rollback();
            } catch (SQLException ignored) {
            }
            out.println("Order failed. Please contact technical support.");
        } finally {
            // Clean up.
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            }
        }
        out.print(myjson.toString());
    }
}
