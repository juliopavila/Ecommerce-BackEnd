package javp.org;

import DataBase.DBConnection;
import DataBase.PropsManager;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
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
@MultipartConfig
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
        response.setContentType("application/json");
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
        Integer user_id = Integer.parseInt(request.getParameter("user_id"));
        String getproduct = props.getProps("getproduct");
        ResultSetMetaData rsmd = null;
        //Importamos el id del usuario para realizar la consulta
        try {
            stmt = connection.prepareStatement(getproduct);
            stmt.setInt(1, user_id);
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

    private void add(Connection connection, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        System.out.println("Entrando al metodo para agregar un producto");
        Part file = request.getPart("file");
        //Importamos los campos
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        Integer user_id = Integer.parseInt(request.getParameter("user_id"));
        Integer stock = Integer.parseInt(request.getParameter("stock"));
        Float price = Float.parseFloat(request.getParameter("price"));
        JSONObject myjson = new JSONObject();
        PreparedStatement stmt = null;
        PropsManager props = PropsManager.getInstance();
        String addproduct = props.getProps("addproduct");
        PrintWriter out = response.getWriter();
        HttpSession mySession;
        java.util.Date date = new java.util.Date();//Para obtener la forma correcta de formato de fecha para postgres
        java.sql.Date finaldate = new java.sql.Date(date.getTime());
        try {
            mySession = request.getSession();
            stmt = connection.prepareStatement(addproduct);
            stmt.setInt(1, user_id);
            stmt.setString(2, title);
            stmt.setString(3, description);
            stmt.setInt(4, stock);
            stmt.setFloat(5, price);
            stmt.setDate(6, finaldate);
            stmt.setString(7, uploadProductImage(file));
            stmt.setString(8, this.getFileName(file));
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

    private void update(Connection connection, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        System.out.println("Entrando al metodo para modificar un producto");
        //Importamos los campos
        String title = myjson.getString("title");
        String description = myjson.getString("description");
        Integer user_id = myjson.getInt("user_id");
        Integer product_id = myjson.getInt("product_id");
        Integer stock = myjson.getInt("stock");
        Float price = Float.parseFloat(myjson.getString("price"));
        JSONObject json = new JSONObject();
        PreparedStatement stmt = null;
        PropsManager props = PropsManager.getInstance();
        String updateproduct = props.getProps("updateproduct");
        PrintWriter out = response.getWriter();
        HttpSession mySession;
        try {
            mySession = request.getSession();
            stmt = connection.prepareStatement(updateproduct);
            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setInt(3, stock);
            stmt.setFloat(4, price);
            stmt.setInt(5, product_id);
            stmt.setInt(6, user_id);
            System.out.println("Este es el query del add product ---->" + stmt.toString());
            stmt.executeUpdate();
            System.out.println("Agregado con exito a la Base de datos");
            json.put("status", 200);
            //Datos para el userboard
        } catch (SQLException | JSONException e) {
            System.out.println("Error al conectar..." + e.getMessage());
            json.put("status", 404);
        }//Final del catch
        out.print(json.toString());
    }

    private void delete(Connection connection, HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("Entrando al metodo eliminar producto");
        response.setContentType("aplication/json");
        PropsManager props = PropsManager.getInstance();
        PrintWriter out = response.getWriter();
        PreparedStatement stmt = null;
        String deleteproduct = props.getProps("deleteproduct");
        JSONObject json = new JSONObject();
        HttpSession mySession;
        //Importamos los campos
        Integer user_id = Integer.parseInt(request.getParameter("user_id"));
        Integer product_id = Integer.parseInt(request.getParameter("product_id"));
        try {
            mySession = request.getSession();
            stmt = connection.prepareStatement(deleteproduct);
            stmt.setInt(1, user_id);
            stmt.setInt(2, product_id);
            System.out.println("Este es el query del delete ---->" + stmt.toString());
            stmt.executeUpdate();
            System.out.println("Eliminado con exito a la Base de datos");
            json.put("status", 200);
        } catch (SQLException | JSONException e) {
            System.out.println("ERROR AL CONECTAR... -> " + e.getMessage());
            json.put("status", 404);
        }
        out.print(json.toString());
    }

    protected String uploadProductImage(Part file) throws IOException, ServletException {
        PropsManager props = PropsManager.getInstance();
        InputStream filecontent = file.getInputStream();
        OutputStream os = null;
        String fullurl = null;

        if (allowedFiles(this.getFileName(file))) {
            try {
                String baseDir = props.getProps("uploadimg");
                os = new FileOutputStream(baseDir + "/" + this.getFileName(file));
                fullurl = baseDir + "/" + this.getFileName(file);
                int read = 0;
                byte[] bytes = new byte[1024];

                while ((read = filecontent.read(bytes)) != -1) {
                    os.write(bytes, 0, read);
                }
            } catch (IOException e) {
            } finally {
                if (filecontent != null) {
                    filecontent.close();
                }
                if (os != null) {
                    os.close();
                }
            }
        } else {
            //NOT ALLOWED FILE
            return fullurl;
        }

        return fullurl;
    }

    // This function allows us to obtain the file name
    private String getFileName(Part part) {
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

    private boolean allowedFiles(String myfile) {
        String[] allowedfiles = {".png", ".jpg", ".jpeg"};
        boolean isallow;
        isallow = ((myfile.contains(allowedfiles[0])) || (myfile.contains(allowedfiles[1])));
        return isallow;
    }

    public void getImage(String myurl, HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("image/png;image/jpg");
        File f = new File(myurl);
        BufferedImage bi = ImageIO.read(f);
        OutputStream out = res.getOutputStream();
        ImageIO.write(bi, "jpg", out);
        out.close();
    }
}
