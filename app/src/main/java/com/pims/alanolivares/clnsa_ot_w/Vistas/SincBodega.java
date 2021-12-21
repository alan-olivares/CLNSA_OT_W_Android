package com.pims.alanolivares.clnsa_ot_w.Vistas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadre;
import com.pims.alanolivares.clnsa_ot_w.Funciones.SpinnerObjeto;
import com.pims.alanolivares.clnsa_ot_w.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class SincBodega extends ClasePadre {
    TextView ultima;
    Button sincronizar,todas;
    Spinner bodegas;
    String canalID = "my_channel_id_01";
    int notificationID = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sinc_bodega);
        inicializar();
        setTitulo("Sincronización de bodegas");
        ArrayList<SpinnerObjeto> lista= getFunciones().llenarSpinner("SELECT AlmacenID as id, Nombre as valor FROM AA_almacen ORDER BY ID",bodegas);
        bodegas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SpinnerObjeto bod=(SpinnerObjeto)adapterView.getSelectedItem();
                ponerFechaAct(bod);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        sincronizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SpinnerObjeto bodega=(SpinnerObjeto) bodegas.getSelectedItem();
                if(!getFunciones().isThread("TBRE_Sinc")){
                    iniciarNotifi();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                cargarBodega(bodega);
                                if(SincBodega.this.getWindow().getDecorView().getRootView().isShown()){
                                    ponerFechaAct(bodega);
                                }
                                builderNoti.setContentText("La bodega "+bodega+" ha sido actualizada").setContentTitle("Descarga completada")
                                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                                        .setProgress(0,0,false);
                                notificationManager.notify(notificationID, builderNoti.build());
                            } catch (Exception e) {
                                builderNoti.setPriority(NotificationCompat.PRIORITY_HIGH)
                                        .setProgress(0, 0, false)
                                        .setContentTitle("Error en la descarga de datos")
                                        .setContentText("La bodega "+bodega+" no pudo ser cargada. "+e.getMessage());
                                notificationManager.notify(notificationID, builderNoti.build());
                            }
                        }
                    },"TBRE_Sinc").start();
                }else{
                    getFunciones().mostrarMensaje("Hay una descarga en proceso, espera a que se finalice");
                }
                Thread.getAllStackTraces().keySet().forEach((t) -> System.out.println(t.getName() + "\nIs Daemon " + t.isDaemon() + "\nIs Alive " + t.isAlive()));

            }
        });

        todas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                if(!getFunciones().isThread("TBRE_Sinc")){
                                    iniciarNotifi();
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            int x=0;
                                            try {
                                                for(SpinnerObjeto bodega: lista){
                                                    builderNoti.setProgress(lista.size(), x++, false);
                                                    notificationManager.notify(notificationID, builderNoti.build());
                                                    cargarBodega(bodega);
                                                }
                                                builderNoti.setPriority(NotificationCompat.PRIORITY_HIGH)
                                                        .setContentText("Todas bodegas han sido actualizadas")
                                                        .setProgress(0,0,false).setContentTitle("Descarga completada");
                                                notificationManager.notify(notificationID, builderNoti.build());
                                                if(SincBodega.this.getWindow().getDecorView().getRootView().isShown()){
                                                    ponerFechaAct((SpinnerObjeto) bodegas.getSelectedItem());
                                                }
                                            }catch (Exception e){
                                                builderNoti.setPriority(NotificationCompat.PRIORITY_HIGH).
                                                        setProgress(0, 0, false)
                                                        .setContentTitle("Error en la descarga de datos")
                                                        .setContentText("Las bodegas no pudieron ser cargadas. "+e.getMessage());
                                                notificationManager.notify(notificationID, builderNoti.build());
                                            }
                                        }
                                    },"TBRE_Sinc").start();
                                }else{
                                    getFunciones().mostrarMensaje("Hay una descarga en proceso, espera a que se finalice");
                                }

                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                dialog.dismiss();
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(SincBodega.this);
                builder.setMessage("Actualizar todas las bodegas puede tardar hasta varios minutos en realizarse, ¿deseas continuar?").setPositiveButton("Si", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });
        sincronizar.setEnabled(bodegas.getSelectedItemPosition()!=-1);
        todas.setEnabled(bodegas.getSelectedItemPosition()!=-1);
    }
    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder builderNoti;
    private void iniciarNotifi(){
        notificationID++;
        getFunciones().createNotificationChannel(canalID);
        notificationManager = NotificationManagerCompat.from(SincBodega.this);
        builderNoti = new NotificationCompat.Builder(SincBodega.this, canalID);
        builderNoti.setContentTitle("Bodegas")
                .setContentText("Descarga en progreso...")
                .setSmallIcon(R.drawable.tbre)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setProgress(100, 0, false);
        getFunciones().mostrarMensaje("Descarga de bodegas iniciada");
        notificationManager.notify(notificationID, builderNoti.build());
    }

    private void inicializar(){
        ultima=findViewById(R.id.ultimaSB);
        sincronizar=findViewById(R.id.sincronizarSB);
        bodegas=findViewById(R.id.bodegasSB);
        todas=findViewById(R.id.sincronizarToSB);
    }
    private void cargarBodega(SpinnerObjeto bodega) throws Exception {
        JSONArray jsonArray=getFunciones().consultaJson("EXEC sp_AA_SincBodega "+bodega.getId(), SQLConnection.db_AAB);
        getFunciones().ejecutarComLocal("delete from wm_barril where bodega="+bodega.getId());
        getFunciones().actualizarSQLLocal("wm_barril",jsonArray,false);
        getFunciones().ejecutarComLocal("INSERT OR REPLACE INTO cm_SyncBodega (idbodega, usuario ,fecha) VALUES('"+bodega.getId()+"','"+getFunciones().getIdUsuario()+"',datetime('now', 'localtime'));");
    }
    private void ponerFechaAct(SpinnerObjeto bodega){
        ultima.post(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray json= getFunciones().getJsonLocal("select strftime('%d/%m/%Y %H:%M', fecha) as fecha  from cm_SyncBodega where idbodega="+bodega.getId());
                    if(json.length()>0){
                        ultima.setText("Última actualización: "+json.getJSONObject(0).getString("fecha"));
                        ultima.setTextColor(ContextCompat.getColor(SincBodega.this,R.color.black));
                    }else{
                        ultima.setText("Bodega no sincronizada");
                        ultima.setTextColor(ContextCompat.getColor(SincBodega.this,R.color.rojo_danger));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}