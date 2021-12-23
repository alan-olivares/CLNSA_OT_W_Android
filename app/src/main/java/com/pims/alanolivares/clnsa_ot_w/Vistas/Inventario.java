package com.pims.alanolivares.clnsa_ot_w.Vistas;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadreFragment;
import com.pims.alanolivares.clnsa_ot_w.Funciones.FuncionesGenerales;
import com.pims.alanolivares.clnsa_ot_w.Funciones.SpinnerObjeto;
import com.pims.alanolivares.clnsa_ot_w.R;

import org.json.JSONArray;
import org.json.JSONObject;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class Inventario extends ClasePadreFragment {
    ImageButton estructura,bodega,resumen,consulta;
    String consultas[][]={{"select AlmacenId,Nombre,[Descripcion],[Consecutivo],[PlantaID] from AA_Almacen",
            "AA_Almacen"},{"select [AreaId],[AlmacenId] ,[Nombre],[Consecutivo] from AA_Area","AA_Area"},
            {"SELECT [NivelID],[PosicionId],[Nombre],[Consecutivo] FROM AA_Nivel","AA_Nivel"},
            {"SELECT [PlantaID],[Nombre],[Descripcion],[Consecutivo] FROM AA_Plantas","AA_Plantas"},
            {"SELECT [PosicionID],[SeccionID],[Nombre],[Consecutivo] FROM AA_Posicion","AA_Posicion"},
            {"SELECT [SeccionID],[AreaId],[Nombre],[Consecutivo] FROM AA_Seccion","AA_Seccion"},
            {"SELECT [RackLocID],[NivelID],[ClasifiID] FROM WM_RackLoc","WM_RackLoc"},
            {"SELECT [RackLocID],[NivelID],[ClasifiID] FROM WM_RackLoc","WM_RackLoc"},
            {"SELECT [IdAlcohol],[Codigo],[Descripcion],[Grado],[Observaciones] FROM CM_Alcohol","CM_Alcohol"},
            {"SELECT [IdCodEdad],[IdCodificicacion],[IdEdad] FROM CM_CodEdad","CM_CodEdad"},
            {"SELECT [IdCodificacion],[Codigo],[Descripcion] FROM CM_Codificacion","CM_Codificacion"},
            {"SELECT [IdEstadoLote],[Descripcion] FROM CM_EstadoLote","CM_EstadoLote"},
            {"SELECT [IdEdad],[Codigo],[Descripcion] FROM [dbo].[CM_Edad]","CM_Edad"},
            {"select IdTipoOP,Descripcion, [index] as Indexy from CM_TipoOp","CM_TipoOp"}};
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_inventario, container, false);
        setTitle("Inventario");
        inicializar(view);
        return view;
    }

    private void inicializar(View view){
        estructura=view.findViewById(R.id.estructuraI);
        bodega=view.findViewById(R.id.bodegasI);
        resumen=view.findViewById(R.id.resumenI);
        consulta=view.findViewById(R.id.consultaI);
        estructura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sincEstructura(getContext(),getFunciones());
            }
        });
        bodega.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bod=new Intent(getContext(),SincBodega.class);
                startActivity(bod);
            }
        });
        resumen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent barril=new Intent(getContext(),ResumenInventario.class);
                startActivity(barril);
            }
        });
        consulta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent barril=new Intent(getContext(),ConsultaBarril.class);
                startActivity(barril);
            }
        });
    }
    String canalID = "my_channel_id_01";
    int notificationID = 100;
    public void sincEstructura(Context context, FuncionesGenerales func){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        int PROGRESS_MAX=consultas.length;
                        if(!func.isThread("TBRE_Sinc")){
                            iniciarNotifi(PROGRESS_MAX,context,func);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        for(int x=0;x<consultas.length;x++){
                                            JSONArray jsonArray=func.consultaJson(consultas[x][0], SQLConnection.db_AAB);
                                            func.actualizarSQLLocal(consultas[x][1],jsonArray,true);
                                            builderNoti.setProgress(PROGRESS_MAX, x, false);
                                            notificationManager.notify(notificationID, builderNoti.build());
                                        }
                                        builderNoti.setContentText("Descarga completada")
                                                .setProgress(0,0,false);
                                        notificationManager.notify(notificationID, builderNoti.build());
                                    } catch (Exception e) {
                                        builderNoti.setProgress(0, 0, false).
                                                setContentTitle("Error en la descarga de datos").
                                                setContentText(e.getMessage());
                                        notificationManager.notify(notificationID, builderNoti.build());
                                    }
                                }
                            }).start();
                        }else{
                            func.mostrarMensaje("Hay una descarga en proceso, espera a que se finalice");
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Se iniciará la carga del diseño de bodegas, ¿deseas continuar?").setPositiveButton("Si", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder builderNoti;
    private void iniciarNotifi(int max,Context context,FuncionesGenerales func){
        notificationID++;
        func.createNotificationChannel(canalID);
        notificationManager = NotificationManagerCompat.from(context);
        builderNoti = new NotificationCompat.Builder(context, canalID);
        builderNoti.setContentTitle("Bodegas")
                .setContentText("Descarga en progreso...")
                .setSmallIcon(R.drawable.tbre)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setProgress(max, 0, false);
        func.mostrarMensaje("Descarga de estructura en bodegas iniciada");
        notificationManager.notify(notificationID, builderNoti.build());
    }


}