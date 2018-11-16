
package Proxy;

import javax.servlet.http.HttpSession;


public class SaveAttr implements Save {

    @Override
    public void saveAttr(HttpSession mysession, String key, Object value) {
        mysession.setAttribute(key, value);
    }
 
}
