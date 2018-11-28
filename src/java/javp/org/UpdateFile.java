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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Julio Avila
 */
@MultipartConfig
@WebServlet(name = "UpdateFile", urlPatterns = {"/UpdateFile"})
public class UpdateFile extends HttpServlet {

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            update(DBConnection.getConnection(), request, response);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SignUp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void update(Connection connection, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        System.out.println("Entrando al metodo para modificar una imagen producto");
        Part file = request.getPart("file");
        System.out.println(""+file);
        //Importamos los campos
        Integer user_id = Integer.parseInt(request.getParameter("user_id"));
        System.out.println(""+user_id);
        Integer product_id = Integer.parseInt(request.getParameter("product_id"));
        System.out.println(""+product_id);
        JSONObject myjson = new JSONObject();
        PreparedStatement stmt = null;
        PropsManager props = PropsManager.getInstance();
        String updateproduct = props.getProps("updatefile");
        PrintWriter out = response.getWriter();
        HttpSession mySession;
        try {
            mySession = request.getSession();
            stmt = connection.prepareStatement(updateproduct);
            stmt.setString(1, uploadProductImage(file));
            stmt.setString(2, this.getFileName(file));
            stmt.setInt(3, product_id);
            stmt.setInt(4, user_id);
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
