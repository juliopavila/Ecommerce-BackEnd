
package javp.org;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONObject;

/**
 *
 * @author Julio Avila
 */
@WebServlet(name = "Logout", urlPatterns = {"/Logout"})
public class Logout extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	PrintWriter out = response.getWriter();
	HttpSession session = request.getSession();
	JSONObject json = new JSONObject();		
	if(!session.isNew()) {
            json.put("status", "500").put("response", "Sesion no iniciada");
            session.invalidate();
	}
        else{
            json.put("status", "200").put("response", "Sesion terminada");
            session.invalidate();
	}
	out.print(json.toString());
        System.out.println("Json ----> "+json.toString());
    }
}
