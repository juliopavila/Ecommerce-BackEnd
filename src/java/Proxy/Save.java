
package Proxy;

import javax.servlet.http.HttpSession;

public interface Save {
    public void saveAttr(HttpSession mysession, String key, Object value);
}
