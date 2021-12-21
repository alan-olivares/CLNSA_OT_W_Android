package com.pims.alanolivares.clnsa_ot_w.DataBase;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class SQLConnection {
    private static final String LOG = "DEBUG";
    public static String ip = "192.168.1.14";
    private static String port = "1433";
    private static String classs = "net.sourceforge.jtds.jdbc.Driver";
    public static String db_AAB = "AAB_CLNSA";
    public static String db_Emba = "Embarrilado01";
    private static String user = "aiom";
    private static String password = "123456";
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
            throw new Exception("Se generó un problema al conectar a la base de datos");
        } catch (ClassNotFoundException e) {
            throw new Exception("No se encontró la clase net.sourceforge.jtds.jdbc.Driver");
        }
        return conn;
    }
}