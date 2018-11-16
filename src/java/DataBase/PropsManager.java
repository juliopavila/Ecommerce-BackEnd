
package DataBase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropsManager {
    private static PropsManager manager = null;
    private Properties p;
    public PropsManager() {
        p = new Properties();
        try{
            p.load(new FileInputStream(new File("C:\\Users\\Julio Avila\\Documents\\NetBeansProjects\\ShoppingCart\\src\\java\\DataBase\\db.properties")));
        }
        catch (IOException e) {
            System.out.println("Error al cargar archivo de propiedades... -> "+e.getMessage());  
        }
    }
    
    public static PropsManager getInstance() {
	return manager = ((manager == null) ? manager = new PropsManager() : manager); 
    }
    
    public String getProps(String prop) {
        return p.getProperty(prop);
    }
}
