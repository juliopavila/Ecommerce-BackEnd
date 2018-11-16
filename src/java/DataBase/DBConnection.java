
package DataBase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static Connection conn = null;
    private static PropsManager props = PropsManager.getInstance();
    public static Connection getConnection() throws ClassNotFoundException {
    try {
        System.out.println("Estoy intentando agarrando el driver");
        Class.forName("org.postgresql.Driver");
        System.out.println("Ya agarre el driver");
        conn = DriverManager.getConnection(props.getProps("dbhost"),props.getProps("dbuser"),props.getProps("dbpassword"));
        System.out.println("Ya estableci conexion con la Base De Datos");
	} 
        catch(SQLException | ClassNotFoundException e) {
            System.out.println("Error al crear conexión a la BD (DBConnection) -> " + e.getMessage());
	}
	return conn;   
    }   
}
