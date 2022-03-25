package com.pims.alanolivares.clnsa_ot_w.DataBase;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * <p>Clase que conecta el dispositivo hacía la base de datos del servidor
 * </p>
 *
 * @author Alan Israel Olivares Mora
 * @version v1.0
 *
 */
public class SQLConnection {
    private static final String LOG = "DEBUG";
    //IP del servidor al que se conectará
    public static String ip = "192.168.1.42";
    //Puerto en el que esta escuchando las peticiones, por default es 1433
    private static String port = "1433";
    //Driver web usado para la comunicación entre SQL Server y Java
    private static String classs = "net.sourceforge.jtds.jdbc.Driver";
    //Nombres de las base de datos
    public static String db_AAB = "AAB_CLNSA_2022";
    public static String db_Emba = "Embarrilado01";
    //Credenciales de acceso al SQL Server
    private static String user = "aiom";
    private static String password = "123456";
    /**
     * Método que hace la conexión entre dispositivo y servidor de SQL
     *
     * @param db - Nombre de la base de datos
     * @throws Exception - Lanza información del problema ocurrido
     * @return Connection - Conexión para ingresar al SQL
     */
    public static Connection connect(String db) throws Exception {
        Connection conn = null;
        String ConnURL = null;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            Class.forName(classs);
            ConnURL = "jdbc:jtds:sqlserver://"+ip+":"+port+"/"+db;
            conn = DriverManager.getConnection(ConnURL,user,password);
        } catch (SQLException e) {
            Log.e(LOG,e.getMessage());
            throw new Exception("Se generó un problema al conectar a la base de datos");
        } catch (ClassNotFoundException e) {
            Log.e(LOG,e.getMessage());
            throw new Exception("No se encontró la clase net.sourceforge.jtds.jdbc.Driver");
        }
        return conn;
    }
}