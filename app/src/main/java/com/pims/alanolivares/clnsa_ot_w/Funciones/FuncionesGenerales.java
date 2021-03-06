package com.pims.alanolivares.clnsa_ot_w.Funciones;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLLocal;
import com.pims.alanolivares.clnsa_ot_w.R;
import com.pims.alanolivares.clnsa_ot_w.Vistas.Inicio;
import com.pims.alanolivares.clnsa_ot_w.Vistas.LoginActivity;
import com.pims.alanolivares.clnsa_ot_w.Vistas.MenuRelleno;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * <p>Clase con la implementaci??n de los metodos principales para el uso de la aplicaci??n
 * </p>
 *
 * @author Alan Israel Olivares Mora
 * @version v1.0
 *
 */
public class FuncionesGenerales {
    /**
     * Conexto que nos permite trabajar en cualquier vista en la que es llamada la clase
     */
    private static Context context;
    /**
     * Constructor de la clase que inicializa el Context
     * @param context - Contexto de la vista en la que es llamada la clase
     */
    public FuncionesGenerales(Context context){
        this.context=context;
    }
    /**
     * M??todo que autentica al usuario y asigna una n??mero de pistola al dispositivo
     *
     * @param usuario - Campo de identificador del usuario
     * @param contrasena - Campo de la contrase??a del usuario
     * @param pistola - N??mero de pistola con la que trabajar?? este dispositivo
     * @return String - El resultado obtenido en la base de datos, 1 signifca que fue satisfactorio en inicio de sesi??n
     */
    public String Login(String usuario,String contrasena,String pistola){
        String result= Login(usuario,contrasena);
        if(result.equals("1")){
            SharedPreferences.Editor editor = context.getSharedPreferences("Usuarios", Context.MODE_PRIVATE).edit();
            editor.putString("pistola", pistola);
            editor.putString("usuario", usuario);
            editor.putString("contra", contrasena);
            editor.commit();
        }
        return result;
    }
    /**
     * M??todo que autentica al usuario (No necesita pistola)
     *
     * @param usuario - Campo de identificador del usuario
     * @param contrasena - Campo de la contrase??a del usuario
     * @return String - El resultado obtenido en la base de datos, 1 signifca que fue satisfactorio en inicio de sesi??n
     */
    public String Login(String usuario,String contrasena){
        try {
            String result="";
            JSONArray jsonArray=consultaJson("exec sp_getAcceso '"+usuario+"','"+contrasena+"'",SQLConnection.db_AAB);
            JSONObject jsonObject=jsonArray.getJSONObject(0);
            result=jsonObject.getString("msg");
            if(result.equals("1")){
                jsonArray=consultaJson("select Nombre,IdUsuario,IdGrupo from CM_Usuario_Web where Clave='"+usuario+"'",SQLConnection.db_AAB);
                jsonObject=jsonArray.getJSONObject(0);
                SharedPreferences.Editor editor = context.getSharedPreferences("Usuarios", Context.MODE_PRIVATE).edit();
                editor.putString("nombre", jsonObject.getString("nombre"));
                editor.putString("idUsuario", jsonObject.getString("idusuario"));
                editor.putString("idGrupo", jsonObject.getString("idgrupo"));
                editor.commit();
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
    /**
     * M??todo que inserta o actualiza informaci??n dentro de la base de datos
     *
     * @param consulta - Consulta que se realizar??
     * @param DB - Base de datos a la que acceder?? (Ver clase SQLConnection)
     * @throws Exception - Lanza informaci??n del problema ocurrido
     */
    public void insertaData(String consulta,String DB) throws Exception {
        if(!isOnlineNet())
            throw new Exception("La pistola no est?? conectada a la red");
        if(!servidorActivo())
            throw new Exception("El servidor no respondi??");
        Statement statement = null;
        Connection con=null;
        try {
            con = SQLConnection.connect(DB);
            statement = con.createStatement();
            statement.executeUpdate(consulta);
        } catch (SQLException throwables) {
            makeErrorSound();
            throw new Exception("Se gerner?? un problema al registrar la informaci??n: "+throwables.getMessage());
        }finally {
            if(con!=null && !con.isClosed())
                con.close();
            if(statement!=null && !statement.isClosed())
                statement.close();
        }

    }
    /**
     * M??todo que consulta informaci??n en la base de datos y convierte dicha informaci??n en Json
     *
     * @param consulta - Consulta que se realizar??
     * @param DB - Base de datos a la que acceder?? (Ver clase SQLConnection)
     * @throws Exception - Lanza informaci??n del problema ocurrido
     * @return JSONArray - Consulta convertida en Json
     */
    public JSONArray consultaJson(String consulta,String DB) throws Exception {
        if(!isOnlineNet())
            throw new Exception("La pistola no est?? conectada a la red");
        if(!servidorActivo())
            throw new Exception("El servidor no respondi??");
        Connection con=null;
        Statement statement=null;
        ResultSet resultSet=null;
        try {
            con = SQLConnection.connect(DB);
            statement = con.createStatement();
            resultSet = statement.executeQuery(consulta);
            JSONArray jsonArray = new JSONArray();
            while (resultSet.next()) {
                int columns = resultSet.getMetaData().getColumnCount();
                JSONObject obj = new JSONObject();
                for (int i = 0; i < columns; i++)
                    obj.put(resultSet.getMetaData().getColumnLabel(i + 1).toLowerCase(), resultSet.getObject(i + 1));
                jsonArray.put(obj);
            }
            return jsonArray;
        } catch (SQLException throwables) {
            makeErrorSound();
            throw new Exception(throwables.getMessage());
        } catch (JSONException e) {
            makeErrorSound();
            throw new Exception("Se gerner?? un problema al convertir la informaci??n en JSON: "+e.getMessage());
        }finally {
            if(con!=null && !con.isClosed())
                con.close();
            if(statement!=null && !statement.isClosed())
                statement.close();
            if(resultSet!=null && !resultSet.isClosed())
                resultSet.close();
        }

    }
    public String [][] busquedaTabla(String text,String tabla[][]){
        int ancho=(tabla.length>0)?tabla[0].length:0;
        String nueva[][]=new String[tabla.length][ancho];
        int pos=0;
        for(int x=0;x<tabla.length;x++)
            for(int i=0;i<ancho;i++){
                if(tabla[x][i].contains(text)){
                    nueva[pos++]=tabla[x];
                    break;
                }
            }
        return Arrays.copyOf(nueva,pos);
    }

    /**
     * M??todo que consulta informaci??n en la base de datos y convierte dicha informaci??n en un
     * array bi-dimencional
     *
     * @param consulta - Consulta que se realizar??
     * @param DB - Base de datos a la que acceder?? (Ver clase SQLConnection)
     * @param llaves - N??mero de columnas deseadas (Obtendr?? las columnas 0 hasta la variable llaves)
     * @throws Exception - Lanza informaci??n del problema ocurrido
     * @return String[][] - Consulta convertida en array bi-dimencional
     */
    public String[][] consultaTabla(String consulta,String DB,int llaves) throws Exception {
        try{
            JSONArray jsonArray= consultaJson(consulta,DB);
            return consultaTabla(jsonArray,llaves);
        } catch (JSONException e) {
            makeErrorSound();
            throw new Exception("Se gerner?? un problema al convertir la informaci??n e JSON: "+e.getMessage());
        } catch (Exception e) {
            throw e;
        }
    }
    /**
     * M??todo que convierte un JSONArray en un array bi-dimencional
     *
     * @param jsonArray - Datos de las tablas en forma de JSONArray
     * @param llaves - N??mero de columnas deseadas (Obtendr?? las columnas 0 hasta la variable llaves)
     * @throws Exception - Lanza informaci??n del problema ocurrido
     * @return String[][] - JSONArray convertido en array bi-dimencional
     */
    public String[][] consultaTabla(JSONArray jsonArray,int llaves) throws Exception {
        try{
            String matriz[][] = new String[jsonArray.length()][llaves];
            if(jsonArray.length()>=0){
                for (int x=0;x<jsonArray.length();x++){
                    int i=0;
                    Iterator<String> keyItr = jsonArray.getJSONObject(x).keys();
                    JSONObject jsonObject=jsonArray.getJSONObject(x);
                    while(keyItr.hasNext() && i<llaves) {
                        String key = keyItr.next();
                        matriz[x][i++]=jsonObject.getString(key).isEmpty()?"":jsonObject.getString(key);
                    }
                }
            }
            return matriz;
        } catch (JSONException e) {
            makeErrorSound();
            throw new Exception("Se gerner?? un problema al convertir la informaci??n en JSON: "+e.getMessage());
        } catch (Exception e) {
            throw e;
        }
    }
    public boolean iniciaOrden(String orden,String tipo, Class<?> cls){
        try {
            JSONArray json=consultaJson("select isnull((select Estatus from PR_Orden where IdOrden='"+orden+"'),3) as estado",SQLConnection.db_AAB);
            if(json.getJSONObject(0).getString("estado").equals("1") || json.getJSONObject(0).getString("estado").equals("2")){
                insertaData("update PR_Orden set Estatus=2 where IdOrden="+orden,SQLConnection.db_AAB);
                Intent menu=new Intent(context, cls);
                menu.putExtra("IdOrden",orden);
                menu.putExtra("tipo",tipo);
                context.startActivity(menu);
                return true;
            }
            mostrarMensaje("..Error.. Esta orden ya se encuentra finalizada");
        }catch (Exception e){
            mostrarMensaje(e.getMessage());
        }
        makeErrorSound();
        return false;
    }
    public int etiToConse(String etiqueta){
        return Integer.valueOf(etiqueta.substring(4,10));
    }
    public void makeErrorSound(){
        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGen1.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT,250);
    }
    /**
     * M??todo que permite cerrar sesi??n del usuario el cual elimina la informaci??n alamacenada por este
     */
    public void cerrarSesion(){
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setMessage("??Quieres cerrar sesi??n?");
        alert.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = context.getSharedPreferences("Usuarios", Context.MODE_PRIVATE).edit();
                editor.putString("usuario", "No existe");
                editor.putString("contra", "No existe");
                editor.putString("pistola", "1");
                editor.commit();
                desactivarNotificaciones();
                ejecutarComLocal("delete from notifiOrden");
                Intent pass = new Intent(context, LoginActivity.class);
                context.startActivity(pass);
                ((Activity)context).finish();
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog aler = alert.create();
        aler.show();
    }
    /**
     * M??todo que verifica si el dispositivo est?? conectado al Wi-Fi
     *
     * @return Boolean
     */
    public Boolean isOnlineNet() {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifi.isConnected();
    }
    /**
     * M??todo que convierte un n??mero de tipo String a un formato requerido dentro de la aplicaci??n (#,###.00)
     *
     * @param numero - N??mero a convertir
     * @return String - N??mero con formato #,###.00
     */
    public String formatNumber(String numero){
        double amount = Double.parseDouble(numero);
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        return formatter.format(amount);
    }
    /**
     * M??todo que valida si la etiqueta ingresada por el usuario es correcta
     *
     * @param etiqueta - Etiqueta del barril
     * @return boolean
     */
    public boolean valEtiBarr(String etiqueta){
        return (etiqueta.length()==10 || (etiqueta.length()==11 && etiqueta.contains("\n"))) && etiqueta.startsWith("0101");
    }
    /**
     * M??todo que muestra al usuario un mensaje en forma de toast
     *
     * @param mensaje - Mensaje a mostrar
     */
    public void mostrarMensaje(String mensaje){
        /*boolean error=mensaje.contains("..Error..");
        Toast toast = Toast.makeText(context, mensaje.replace("..Error..",""), Toast.LENGTH_LONG);
        View toastView = toast.getView(); // This'll return the default View of the Toast.

         //And now you can get the TextView of the default View of the Toast.
        TextView toastMessage = toastView.findViewById(android.R.id.message);
        toastMessage.setTextSize(25);
        toastMessage.setTextColor(Color.BLACK);
        toastMessage.setCompoundDrawablesWithIntrinsicBounds(error?R.drawable.finalizar_tanque:R.drawable.consulta_barril, 0, 0, 0);
        toastMessage.setGravity(Gravity.CENTER);
        toastMessage.setCompoundDrawablePadding(16);
        toastView.setBackgroundColor(error?Color.RED:Color.WHITE);
        toast.show();*/
        Toast.makeText(context,mensaje,Toast.LENGTH_LONG).show();
    }
    /**
     * M??todo que permite activar los flujomentros para el llenado de barriles
     *
     * @throws Exception - Lanza informaci??n del problema ocurrido
     */
    public void avtivarFlujometos() throws Exception{
        try{
            JSONArray jsonArray=consultaJson("select idlanza from CM_Lanza where Seleccion=1 order by IdLanza",SQLConnection.db_AAB);
            for (int x=0;x<jsonArray.length();x++){
                JSONObject jsonObject=jsonArray.getJSONObject(x);
                String tag="-1";
                switch (jsonObject.getInt("idlanza")){
                    case 1:
                        tag="4";
                        break;
                    case 2:
                        tag="7";
                        break;
                    case 3:
                        tag="10";
                        break;
                }
                insertaData("UPDATE COM_TagsActual set valor=1 where IdTag="+tag,SQLConnection.db_Emba);
            }
            insertaData("UPDATE COM_TagsActual set Valor=0 where IdTag in (6,9,12)",SQLConnection.db_Emba);
        }catch (JSONException e) {
            makeErrorSound();
            throw new Exception("Se gerner?? un problema al activar los flujometros");
        } catch (Exception e) {
            makeErrorSound();
            throw new Exception("Se gerner?? un problema al activar los flujometros");
        }

    }
    /**
     * M??todo que registra los logs en archivos de texto dentro del dispositivo en la carpeta TBRE
     *
     * @param texto - Log a escribir
     * @throws IOException - Lanza problema ocurrido al registrar la informaci??n
     */
    public void escribirLog(String texto) throws IOException {
        String dia=getCurrDate("ddMMyyyy");
        File f = new File(Environment.getExternalStorageDirectory().getPath(), "TBRE/TBRElog_"+dia+".txt");
        if(!f.exists()){
            Files.createFile(Paths.get(f.getAbsolutePath()));
        }
        FileOutputStream stream = new FileOutputStream(f,true);
        try {
            stream.write((texto+"\n").getBytes());
        } finally {
            stream.close();
        }
    }
    /**
     * M??todo que obtiene la fecha actual del dispositivo en el formato solicitado
     *
     * @param formato - Formato en el que estar?? la fecha
     * @return String - Fecha actual con el formato solicitado
     */
    public String getCurrDate(String formato){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(formato);
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }
    /**
     * M??todo que resetea y elimina informaci??n de las procesos de llenado y relleno pasados
     *
     * @throws Exception - Lanza descripci??n del problema ocurrido
     */
    public void resetProceso() throws Exception{
        try {
            insertaData("update cm_lanza set edollenada=estado, seleccion=0", SQLConnection.db_AAB);
            insertaData("delete pr_opllenado", SQLConnection.db_AAB);
        } catch (Exception e) {
            makeErrorSound();
            throw new Exception("Se gerner?? un problema al recetear el flujometro: "+e.getMessage());
        }
    }
    /**
     * M??todo que obtiene el ID del usuario con el que inici?? sesi??n
     *
     * @return String - ID del usuario
     */
    public String getIdUsuario(){
        return getDatoCache("idUsuario");
    }
    /**
     * M??todo que obtiene el n??mero de pistola que se est?? usando en el dispositivo
     *
     * @return String - Pistola
     */
    public String getPistola(){
        return getDatoCache("pistola");
    }
    public String getDatoCache(String key){
        SharedPreferences preferences = context.getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        return preferences.getString(key,"-1");
    }
    public void saveDatoCache(String key,String valor){
        SharedPreferences.Editor editor = context.getSharedPreferences("Usuarios", Context.MODE_PRIVATE).edit();
        editor.putString(key, valor);
        editor.commit();
    }
    /**
     * M??todo que permite al mostrarle al usuario un mensaje al usuario cuando ocurri?? un
     * problema y avisarle si desea intentar de nuevo el proceso, funciona de forma recursiva,
     * el cual permite realizar un sin n??mero de intentos
     *
     * @param consulta - Insert o Update que se requiere hacer
     * @param conexion - Conexi??n de SQLConection
     */
    public void reintentadoRegistro(String consulta,String conexion,Runnable runAfter){
        try {
            consultaJson(consulta,conexion);
            runAfter.run();
        } catch (Exception e) {
            makeErrorSound();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            dialog.dismiss();
                            reintentadoRegistro(consulta,conexion,runAfter);
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            dialog.dismiss();
                            break;
                    }
                }
            };
            builder.setMessage(e.getMessage()+". ??Quieres intentar nuevamente?")
                    .setPositiveButton("Si", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
    }
    /**
     * M??todo que llena un spinner de que es recibido como parametro con informaci??n de la
     * base de datos, la consulta debe incluir un campo id y un campo valor, por ejemplo:
     * select campo1 as id, campo2 as valor from tabla
     *
     * @param consulta - La consulta de datos que se requieren
     * @param spinner - Spinner al cual se le agregar?? la informaci??n
     * @return ArrayList<SpinnerObjeto> - Lista de objetos id-valor que se obtuvieron de la consulta
     */
    public ArrayList llenarSpinner(String consulta, Spinner spinner){
        ArrayList<SpinnerObjeto> lista=new ArrayList<>();
        try {
            JSONArray jsonArray=consultaJson(consulta, SQLConnection.db_AAB);
            for(int x=0;x<jsonArray.length();x++){
                JSONObject jsonObject=jsonArray.getJSONObject(x);
                lista.add(new SpinnerObjeto(jsonObject.getInt("id"),jsonObject.getString("valor")));
            }
            ArrayAdapter<SpinnerObjeto> adapter = new ArrayAdapter<SpinnerObjeto>(context, android.R.layout.simple_spinner_dropdown_item, lista);
            spinner.setAdapter(adapter);
        } catch (Exception e) {
            mostrarMensaje("Problema al cargar el sppiner: "+e.getMessage());
        }
        return lista;
    }
    /**
     * M??todo que llena un spinner de que es recibido como parametro con informaci??n de los
     * datos internos del dispositivo, la consulta debe incluir un campo id y un campo valor,
     * por ejemplo: select campo1 as id, campo2 as valor from tabla
     *
     * @param consulta - La consulta de datos que se requieren
     * @param spinner - Spinner al cual se le agregar?? la informaci??n
     * @return ArrayList<SpinnerObjeto> - Lista de objetos id-valor que se obtuvieron de la consulta
     */
    public ArrayList llenarSpinnerLocal(String consulta, Spinner spinner){
        ArrayList<SpinnerObjeto> lista=new ArrayList<>();
        try {
            JSONArray jsonArray=getJsonLocal(consulta);
            for(int x=0;x<jsonArray.length();x++){
                JSONObject jsonObject=jsonArray.getJSONObject(x);
                lista.add(new SpinnerObjeto(jsonObject.getInt("id"),jsonObject.getString("valor")));
            }
            ArrayAdapter<SpinnerObjeto> adapter = new ArrayAdapter<SpinnerObjeto>(context, android.R.layout.simple_spinner_dropdown_item, lista);
            spinner.setAdapter(adapter);
        } catch (Exception e) {
            mostrarMensaje("Problema al cargar el sppiner: "+e.getMessage());
        }
        return lista;
    }
    /**
     * M??todo que obtiene informaci??n de la base de datos interna y es convertida a JSONArray
     *
     * @param consulta - La consulta de datos que se requieren
     * @return JSONArray - Datos de la consulta solicitada
     */
    public JSONArray getJsonLocal(String consulta){
        JSONArray jsonArray=new JSONArray();
        SQLLocal myhelper=new SQLLocal(context);
        SQLiteDatabase db = myhelper.getWritableDatabase();
        Cursor cursor =db.rawQuery(consulta,null);
        while (cursor.moveToNext()){
            JSONObject jsonObject=new JSONObject();
            for(int x=0;x<cursor.getColumnCount();x++){
                try {
                    jsonObject.put(cursor.getColumnName(x),cursor.getString(x));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            jsonArray.put(jsonObject);
        }
        db.close();
        myhelper.close();
        cursor.close();
        return jsonArray;
    }
    /**
     * M??todo que ejecuta Updates o Inserts dentro de la base de datos interna
     *
     * @param consulta - La consulta requerida por el usuario
     */
    public void ejecutarComLocal(String consulta){
        SQLLocal myhelper=new SQLLocal(context);
        SQLiteDatabase db = myhelper.getWritableDatabase();
        db.execSQL(consulta);
        db.close();
        myhelper.close();
    }
    /**
     * M??todo que registra nuevos datos en las tablas internas del dispositivo
     *
     * @param tabla - El nombre de la tabla a la cual le surgiran los efectos
     * @param datos - JSONArray con los nuevos datos
     * @param borrar - True si se requiere que los viejos datos de toda la tabla sean borrados
     *               (La tabla wm_barril se recomienda borrar por id de bodega)
     */
    public void actualizarSQLLocal(String tabla,JSONArray datos,boolean borrar){
        SQLLocal myhelper=new SQLLocal(context);
        SQLiteDatabase db = myhelper.getWritableDatabase();
        if(borrar)
            borrarRegistros(tabla);
        for(int x=0;x<datos.length();x++){
            try {
                JSONObject json = datos.getJSONObject(x);
                Iterator<String> columnas=json.keys();
                ContentValues contentValues = new ContentValues();
                while (columnas.hasNext()){
                    String columna=columnas.next();
                    contentValues.put(columna, json.getString(columna));
                }
                db.insert(tabla, null, contentValues);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        db.close();
        myhelper.close();
    }
    /**
     * M??todo que borra todos los registros de alguna tabla requerida
     *
     * @param tabla - El nombre de la tabla que se vaciar??
     */
    public void borrarRegistros(String tabla){
        SQLLocal myhelper=new SQLLocal(context);
        SQLiteDatabase db = myhelper.getWritableDatabase();
        db.delete(tabla,null,null);
        db.close();
        myhelper.close();
    }
    /**
     * M??todo que crea el canal requerido para las notificaciones
     *
     * @param canalID - El nombre del canal
     */
    public void createNotificationChannel(String canalID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name);
            String description = context.getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(canalID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    /**
     * M??todo que verifica si el servidor responde a un ping
     *
     * @return boolean
     */
    public boolean servidorActivo(){
        Runtime runtime = Runtime.getRuntime();
        try{
            Process  mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 "+SQLConnection.ip);
            int mExitValue = mIpAddrProcess.waitFor();
            return mExitValue==0;
        }
        catch (Exception ignore){
            return false;
        }
    }
    /**
     * M??todo que perimite activar el tracking de notificaciones entrantes
     */
    public void activarNotificaciones(){
        //Se revisa si ya est?? corriendo el proceso
        Thread TBRE=getThread("TBRE_Notifi");
        if(TBRE!=null){
            if(TBRE.isInterrupted())
                TBRE.start();
            return;
        }
        Timer timer = new Timer("TBRE_Notifi",true);
        timer.schedule(new revisarNotificaciones(), 5000, 5000);
    }
    /**
     * M??todo que perimite desactivar el tracking de notificaciones entrantes
     */
    public void desactivarNotificaciones(){
        //Se revisa si ya est?? corriendo el proceso
        Thread TBRE=getThread("TBRE_Notifi");
        if(TBRE!=null)
            TBRE.interrupt();
    }
    /**
     * M??todo que perimite buscar alg??n hilo que se encuentre corriendo dentro del dispositivo
     *
     * @param nombre - Nombre del hilo solicitado
     * @return Thread - Hilo que se requiere o null sino fue encontrado
     */
    public Thread getThread(String nombre){
        Set<Thread> lista= Thread.getAllStackTraces().keySet();
        for(Thread hilo:lista){
            if(hilo.getName().equals(nombre))
                return hilo;
        }
        return null;
    }
    /**
     * M??todo que perimite buscar si el hilo existe
     *
     * @param nombre - Nombre del hilo solicitado
     * @return boolean
     */
    public boolean isThread(String nombre){
        Set<Thread> lista= Thread.getAllStackTraces().keySet();
        for(Thread hilo:lista){
            if(hilo.getName().equals(nombre))
                return true;
        }
        return false;
    }
    /**
     * M??todo que verifica si la hora del dispositivo es la misma a la hora del servidor,
     * sino lo fuera, aparecer?? un mensaje mencionando cual es la hora correcta y cerrar?? la aplicaci??n
     * as?? como retornar false para que el hilo principal no continue con su proceso habitual
     *
     * @return boolean
     */
    public boolean horaCorrecta(){
        try {
            JSONArray hora=consultaJson("select convert(varchar,GETDATE(),120) as fecha",SQLConnection.db_AAB);
            String horaCel=getCurrDate("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date1 = sdf.parse(horaCel);
            Date date2 = sdf.parse(hora.getJSONObject(0).getString("fecha"));
            long diff = date2.getTime() - date1.getTime();//as given
            if(TimeUnit.MILLISECONDS.toMinutes(diff)<-60 || TimeUnit.MILLISECONDS.toMinutes(diff)>60){
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                ((AppCompatActivity)context).finishAndRemoveTask();
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("El dispositivo no tiene la hora correcta del servidor ("+hora.getJSONObject(0).getString("fecha")+"), configuralo he intenta de nuevo")
                        .setPositiveButton("Ok", dialogClickListener)
                        .setCancelable(false).show();
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    /**
     * M??todo que verifica la versi??n del dispositivo para conocer si est?? instalada la ??ltima versi??n
     *
     * @return boolean
     */
    public boolean versionUpdate(){
        try {
            JSONArray versionUpdate=consultaJson("select val1 from CM_Config where idConfig=6",SQLConnection.db_AAB);
            float ver=0,verU=0;
            try {
                PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                String version = pInfo.versionName;
                ver = Float.parseFloat(version);
                verU=Float.parseFloat(versionUpdate.getJSONObject(0).getString("val1"));
                if (verU>ver){
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    try {
                                        JSONArray updateLink=consultaJson("select val3 from CM_Config where idConfig=6",SQLConnection.db_AAB);
                                        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(updateLink.getJSONObject(0).getString("val3")));
                                        context.startActivity(launchBrowser);
                                    } catch (Exception e) {
                                        mostrarMensaje("Ocurri?? un problema al obtener el link de descarga, intenta de nuevo m??s tarde, error:"+e.getMessage());
                                        ((AppCompatActivity)context).finishAndRemoveTask();
                                    }
                                    break;
                            }
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Actualizaci??n disponible").setMessage("Se encontr?? una nueva versi??n para descargar, por favor actualizala para poder continuar")
                            .setPositiveButton("Descargar", dialogClickListener)
                            .setCancelable(false).show();
                    return true;
                }
            } catch (PackageManager.NameNotFoundException e) {
                mostrarMensaje("Error en convertir la versi??n a n??meral, verifica la versi??n en el sistema");
            }
        } catch (Exception e) {
            mostrarMensaje("Error al obtener la ??ltima versi??n de la aplicaci??n, error: "+e.getMessage());
        }
        return false;
    }
    /**
     * <p>Clase privada que extiende de TimerTask la cual estar?? verificando si existen notificaciones
     * dentro del servidor
     * </p>
     * <p>
     * La clase contiene m??todos:
     * <ul>
     * <li>crearNotificacion</li>
     * <li>run</li>
     * </ul>
     * </p>
     *
     * @author Alan Istael Olivares Mora
     * @version v1.0
     *
     */
    private class revisarNotificaciones extends TimerTask {
        private String canalID = "my_channel_id_01";
        private void crearNotificacion(int tipo,String titulo, String mensaje,int id){
            ejecutarComLocal("insert into notifiOrden (idnotifiOrden,Fecha) values('"+id+"',datetime('now', 'localtime'))");
            Intent intent = new Intent(context, Inicio.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("caso",tipo);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            createNotificationChannel(canalID);
            NotificationManagerCompat notificationManager= NotificationManagerCompat.from(context);
            NotificationCompat.Builder builderNoti= new NotificationCompat.Builder(context, canalID);
            builderNoti.setContentTitle(titulo)
                    .setContentText(mensaje)
                    .setSmallIcon(R.drawable.tbre)
                    .setPriority(NotificationCompat.PRIORITY_MAX).
                    setContentIntent(pendingIntent).
                    setAutoCancel(true);
            notificationManager.notify(id, builderNoti.build());
        }

        @Override
        public void run() {
            try {
                String fecha=getCurrDate("yyyy-MM-dd");
                ejecutarComLocal("delete from notifiOrden where Fecha <='"+fecha+" 00:00'");
                JSONArray jsonLocal=getJsonLocal("select idnotifiOrden from notifiOrden where Fecha >'"+fecha+" 00:00'");
                JSONArray jsonServer=consultaJson("select idnotificacion,referencia,caso,titulo,mensaje from ADM_Notificaciones where Fecha='"+fecha+"' and (idReceptor=0 or idReceptor="+getIdUsuario()+") and IdEnviador <> "+getIdUsuario()+" order by idnotificacion",SQLConnection.db_AAB);
                for(int x=jsonLocal.length();x<jsonServer.length();x++){
                    JSONObject json=jsonServer.getJSONObject(x);
                    crearNotificacion(json.getInt("caso")-1,json.getString("titulo"),json.getString("mensaje"),json.getInt("idnotificacion"));
                    Log.i("Nueva notificaci??n:", String.valueOf(json));
                }
            }catch (Exception e){
                Log.i("Error de notificaci??n",e.getMessage());
            }
        }
    }

}
