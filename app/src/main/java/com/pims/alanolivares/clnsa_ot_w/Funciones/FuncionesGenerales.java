package com.pims.alanolivares.clnsa_ot_w.Funciones;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLLocal;
import com.pims.alanolivares.clnsa_ot_w.R;
import com.pims.alanolivares.clnsa_ot_w.Vistas.FormarPaleta;
import com.pims.alanolivares.clnsa_ot_w.Vistas.Inicio;
import com.pims.alanolivares.clnsa_ot_w.Vistas.LoginActivity;
import com.pims.alanolivares.clnsa_ot_w.Vistas.MenuLateral;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import kotlin.jvm.Throws;

public class FuncionesGenerales {
    static Context context;
    public FuncionesGenerales(Context context){
        this.context=context;
    }
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
    public String Login(String usuario,String contrasena){
        try {
            String result="",nombre="",idUsuario="";
            JSONArray jsonArray=consultaJson("exec sp_getAcceso '"+usuario+"','"+contrasena+"'",SQLConnection.db_AAB);
            JSONObject jsonObject=jsonArray.getJSONObject(0);
            result=jsonObject.getString("msg");
            if(result.equals("1")){
                jsonArray=consultaJson("select Nombre,IdUsuario from CM_Usuario where Clave='"+usuario+"'",SQLConnection.db_AAB);
                jsonObject=jsonArray.getJSONObject(0);
                nombre=jsonObject.getString("nombre");
                idUsuario=jsonObject.getString("idusuario");
                SharedPreferences.Editor editor = context.getSharedPreferences("Usuarios", Context.MODE_PRIVATE).edit();
                editor.putString("nombre", nombre);
                editor.putString("idUsuario", idUsuario);
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

    public void insertaData(String consulta,String DB) throws Exception {
        if(!isOnlineNet())
            throw new Exception("La pistola no está conectada a la red");
        if(!servidorActivo())
            throw new Exception("El servidor no respondió");
        Statement statement = null;
        Connection con=null;
        try {
            con = SQLConnection.connect(DB);
            statement = con.createStatement();
            statement.executeUpdate(consulta);
        } catch (SQLException throwables) {
            throw new Exception("Se gerneró un problema al registrar la información, revisa los datos e intenta de nuevo");
        }finally {
            if(!con.isClosed())
                con.close();
            if(!statement.isClosed())
                statement.close();
        }

    }

    public JSONArray consultaJson(String consulta,String DB) throws Exception {
        if(!isOnlineNet())
            throw new Exception("La pistola no está conectada a la red");
        if(!servidorActivo())
            throw new Exception("El servidor no respondió");
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
            throw new Exception("Se gerneró un problema al consultar la información");
        } catch (JSONException e) {
            throw new Exception("Se gerneró un problema al convertir la información en JSON");
        }finally {
            if(!con.isClosed())
                con.close();
            if(!statement.isClosed())
                statement.close();
            if(!resultSet.isClosed())
                resultSet.close();
        }

    }
    public String[][] consultaTabla(String consulta,String DB,int llaves) throws Exception {
        try{
            JSONArray jsonArray= consultaJson(consulta,DB);
            return consultaTabla(jsonArray,llaves);
        } catch (JSONException e) {
            throw new Exception("Se gerneró un problema al convertir la información e JSON");
        } catch (Exception e) {
            throw e;
        }
    }
    public String[][] consultaTabla(JSONArray jsonArray,int llaves) throws Exception {
        try{
            String matriz[][] = new String[jsonArray.length()][llaves];
            if(jsonArray.length()>=0){
                for (int x=0;x<jsonArray.length();x++){
                    int i=0;
                    Iterator<String> keyItr = jsonArray.getJSONObject(x).keys();
                    JSONObject jsonObject=jsonArray.getJSONObject(x);
                    while(keyItr.hasNext()) {
                        String key = keyItr.next();
                        matriz[x][i]=jsonObject.getString(key);
                        i++;
                    }

                }
            }
            return matriz;
        } catch (JSONException e) {
            throw new Exception("Se gerneró un problema al convertir la información e JSON");
        } catch (Exception e) {
            throw e;
        }
    }
    public void cerrarSesion(){
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setMessage("¿Quieres cerrar sesión?");
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

    public Boolean isOnlineNet() {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifi.isConnected();
    }
    public String formatNumber(String numero){
        double amount = Double.parseDouble(numero);
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        return formatter.format(amount);
    }
    public boolean valEtiBarr(String etiqueta){
        return etiqueta.length()==10 && etiqueta.startsWith("0101");
    }

    public void mostrarMensaje(String mensaje){
        Toast.makeText(context,mensaje,Toast.LENGTH_LONG).show();
    }
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
            throw new Exception("Se gerneró un problema al activar los flujometros");
        } catch (Exception e) {
            throw new Exception("Se gerneró un problema al activar los flujometros");
        }

    }
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
    public String getCurrDate(String formato){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(formato);
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }
    public void resetProceso() throws Exception{
        try {
            insertaData("update cm_lanza set edollenada=estado, seleccion=0", SQLConnection.db_AAB);
            insertaData("delete pr_opllenado", SQLConnection.db_AAB);
        } catch (Exception e) {
            throw new Exception("Se gerneró un problema al recetear el flujometro: "+e.getMessage());
        }
    }
    public String getIdUsuario(){
        SharedPreferences preferences = context.getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        return preferences.getString("idUsuario","-1");
    }
    public String getPistola(){
        SharedPreferences preferences = context.getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        return preferences.getString("pistola","1");
    }

    public void reintentadoRegistro(String consulta,String conexion){
        try {
            insertaData(consulta,conexion);
            mostrarMensaje("Operación realizada con exito");
        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            dialog.dismiss();
                            reintentadoRegistro(consulta,conexion);
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            dialog.dismiss();
                            break;
                    }
                }
            };
            builder.setMessage("Se generó un problema al registrar la información, por favor verifica tu conexión wifi. ¿Quieres intentar nuevamente?")
                    .setPositiveButton("Si", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
    }

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
            mostrarMensaje("Problema al cargar el sppiner");
        }
        return lista;
    }
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
            mostrarMensaje("Problema al cargar el sppiner");
        }
        return lista;
    }

    public JSONArray getJsonLocal(String consulta){
        JSONArray jsonArray=new JSONArray();
        SQLLocal myhelper=new SQLLocal(context);
        SQLiteDatabase db = myhelper.getWritableDatabase();
        Cursor cursor =db.rawQuery(consulta,null);
        while (cursor.moveToNext())
        {
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
    public void ejecutarComLocal(String consulta){
        SQLLocal myhelper=new SQLLocal(context);
        SQLiteDatabase db = myhelper.getWritableDatabase();
        db.execSQL(consulta);
        db.close();
        myhelper.close();
    }
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
    public void borrarRegistros(String tabla){
        SQLLocal myhelper=new SQLLocal(context);
        SQLiteDatabase db = myhelper.getWritableDatabase();
        db.delete(tabla,null,null);
        db.close();
        myhelper.close();
    }
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

    public boolean servidorActivo(){
        Runtime runtime = Runtime.getRuntime();
        try
        {
            Process  mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 "+SQLConnection.ip);
            int mExitValue = mIpAddrProcess.waitFor();
            return mExitValue==0;
        }
        catch (InterruptedException ignore)
        {
            return false;
        }
        catch (IOException e)
        {
            return false;
        }
    }

    public void activarNotificaciones(){
        //Se revisa si ya está corriendo el proceso
        Thread TBRE=getThread("TBRE_Notifi");
        if(TBRE!=null){
            if(TBRE.isInterrupted())
                TBRE.start();
            return;
        }
        Timer timer = new Timer("TBRE_Notifi",true);
        timer.schedule(new revisarNotificaciones(), 5000, 5000);
    }
    public void desactivarNotificaciones(){
        //Se revisa si ya está corriendo el proceso
        Thread TBRE=getThread("TBRE_Notifi");
        if(TBRE!=null)
            TBRE.interrupt();
    }
    public Thread getThread(String nombre){
        Set<Thread> lista= Thread.getAllStackTraces().keySet();
        for(Thread hilo:lista){
            if(hilo.getName().equals(nombre))
                return hilo;
        }
        return null;
    }
    public boolean isThread(String nombre){
        Set<Thread> lista= Thread.getAllStackTraces().keySet();
        for(Thread hilo:lista){
            if(hilo.getName().equals(nombre))
                return true;
        }
        return false;
    }
    public boolean horaCorrecta(){
        try {
            JSONArray hora=consultaJson("select FORMAT (getdate(), 'yyyy-MM-dd HH:mm:ss') as fecha",SQLConnection.db_AAB);
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
                    Log.i("Nueva notificación:", String.valueOf(json));
                }
            }catch (Exception e){
                Log.i("Error de notificación",e.getMessage());
            }
        }
    }

}
