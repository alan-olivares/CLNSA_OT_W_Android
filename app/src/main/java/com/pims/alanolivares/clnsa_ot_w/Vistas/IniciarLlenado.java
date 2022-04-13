package com.pims.alanolivares.clnsa_ot_w.Vistas;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Dialogos.Lanzas;
import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadre;
import com.pims.alanolivares.clnsa_ot_w.Funciones.FuncionesGenerales;
import com.pims.alanolivares.clnsa_ot_w.Funciones.NoScrollViewTable;
import com.pims.alanolivares.clnsa_ot_w.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.model.TableColumnDpWidthModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class IniciarLlenado extends ClasePadre {
    private NoScrollViewTable dataTable;
    private EditText etiqueta,tapa,lanza1,lanza2,lanza3;
    private Button camara,aceptar,desactivarLan,iniciarBom;
    private Timer timer;
    private String usuario="",IdLote="";
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciar_llenado);
        inicializar();
        IdLote=getIntent().getStringExtra("idLote");
        String tanque=getIntent().getStringExtra("tanque");
        setEtiqueta(etiqueta);
        setCamara(camara);
        setTitulo("Tanque: "+tanque);
        SharedPreferences preferences = getSharedPreferences("Usuarios",MODE_PRIVATE);
        usuario = preferences.getString("idUsuario","No existe");
        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertaEtiqueta(etiqueta.getText().toString());
            }
        });
        desactivarLan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pas = new Intent(IniciarLlenado.this, Lanzas.class);
                startActivity(pas);
            }
        });
        iniciarBom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    iniciarBombeo();
                } catch (Exception e) {
                    getFunciones().mostrarMensaje(e.getMessage());
                }
            }
        });

    }

    private void inicializar(){
        etiqueta=findViewById(R.id.etiquetaILL);
        camara=findViewById(R.id.camaraILL);
        aceptar=findViewById(R.id.aceptarILL);
        tapa=findViewById(R.id.tapaILL);
        desactivarLan=findViewById(R.id.desactivaLanILL);
        iniciarBom=findViewById(R.id.iniciarBomILL);
        lanza1=findViewById(R.id.l1ILL);
        lanza2=findViewById(R.id.l2ILL);
        lanza3=findViewById(R.id.l3ILL);
        dataTable=findViewById(R.id.llenado_tableILL);
        setCopyEtiqueta(dataTable,0);
        progressBar=findViewById(R.id.progressILL);
        String[] spaceProbeHeaders={"Etiqueta","Tapa","Uso","Lanza"};
        dataTable.setHeaderAdapter(new SimpleTableHeaderAdapter(this,spaceProbeHeaders));
        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(this, 4, 100);
        columnModel.setColumnWidth(0,150);
        dataTable.setColumnModel(columnModel);
        //cargarBarriles();
    }
    @Override
    public void validaEtiqueta(String eti) {
        try {
            super.validaEtiqueta(eti);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONArray jsonArray=getFunciones().consultaJson("EXEC sp_ll_Existe_v2 '"+IdLote+"','"+eti+"'", SQLConnection.db_AAB);
                        if(jsonArray.length()>0){
                            JSONObject jsonObject=jsonArray.getJSONObject(0);
                            if(jsonObject.getString("mensaje").equals("")){
                                //Creamos número de tapa
                                jsonArray=getFunciones().consultaJson("EXEC sp_NoTapav2", SQLConnection.db_AAB);
                                jsonObject=jsonArray.getJSONObject(0);
                                int numtapa=jsonObject.getInt("tapa");
                                if(numtapa!=0){
                                    //Agregamos el barril
                                    getFunciones().escribirLog(getFunciones().getCurrDate("dd/MM/yy hh:mm:ss") + "| Scan:" + eti + "|" + (numtapa+1) + "|10");
                                    jsonArray=getFunciones().consultaJson("EXEC sp_ll_agregaBarril '"+eti+"','"+(numtapa+1)+"'", SQLConnection.db_AAB);
                                    jsonObject=jsonArray.getJSONObject(0);
                                    tapa.setText((numtapa+1)+"");
                                    toggleButtons(jsonObject.getString("total").equals("0"));
                                    cargarBarriles();
                                }else{
                                    getFunciones().makeErrorSound();
                                    getFunciones().mostrarMensaje("Error al crear número de tapa");
                                }
                            }else{
                                getFunciones().makeErrorSound();
                                getFunciones().mostrarMensaje(jsonObject.getString("mensaje"));
                            }
                        }
                    }catch (Exception e){
                        getFunciones().mostrarMensaje(e.getMessage());
                    }finally {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
        } catch (Exception e) {
            getFunciones().mostrarMensaje(e.getMessage());
        }



    }
    private void iniciarBombeo() throws Exception{
        tapa.setText("");
        if(iniciarBom.getText().toString().toLowerCase().equals("iniciar bombeo")){
            iniciarBom.setText("Detener bombeo");
            iniciarBom.setBackgroundColor(ContextCompat.getColor(this,R.color.rojo_danger) );
            getFunciones().avtivarFlujometos();
            for (int x=0;x<jsonBarriles.length();x++){
                JSONObject jsonObject=jsonBarriles.getJSONObject(x);
                System.out.println(jsonObject.toString());
                getFunciones().escribirLog(getFunciones().getCurrDate("dd/MM/yy hh:mm:ss") + "|" + jsonObject.getString("etiqueta") + "|" + jsonObject.getString("tapa") + "|" + jsonObject.getString("lanza") + "|11");
            }
            timer = new Timer();
            timer.schedule(new revisarFlujometros(), 2000, 1000);
        }else{
            terminarBombeo();
        }
    }
    private boolean obtenerLanDisp(){
        try {
            JSONArray jsonArray=getFunciones().consultaJson("select count(idlanza) as total from CM_Lanza where EdoLlenada=1 and Seleccion=0", SQLConnection.db_AAB);
            return jsonArray.getJSONObject(0).getString("total").equals("0");
        } catch (Exception e) {
            return false;
        }
    }
    public void terminarBombeo(){
        if(timer!=null)
            timer.cancel();
        iniciarBom.post(new Runnable() {
            @Override
            public void run() {
                try {
                    getFunciones().escribirLog(getFunciones().getCurrDate("dd/MM/yy hh:mm:ss") + "|" + lanza1.getText().toString() + "|" + lanza2.getText().toString()  + "|" + lanza3.getText().toString()  + "|11");
                }catch (Exception e){
                    getFunciones().mostrarMensaje(e.getMessage());
                }
                getFunciones().reintentadoRegistro("Exec sp_llen_Registra '" + IdLote + "','" + usuario + "'", SQLConnection.db_AAB, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getFunciones().insertaData("update cm_lanza set edollenada=estado, seleccion=0",SQLConnection.db_AAB);
                            //getFunciones().insertaData("delete pr_opllenado",SQLConnection.db_AAB);
                            getFunciones().insertaData("update COM_TagsActual set Valor=0 where idtag in (4,7,10,6,9,12)",SQLConnection.db_Emba);
                            iniciarBom.setText("Iniciar bombeo");
                            iniciarBom.setBackgroundColor(ContextCompat.getColor(IniciarLlenado.this,R.color.verde_claro) );
                            cargarBarriles();
                            cargarLanzas();
                            getFunciones().mostrarMensaje("Bombeo terminado correctamente");
                        }catch (Exception e){
                            getFunciones().mostrarMensaje(e.getMessage());
                        }
                    }
                });
            }
        });
    }
    JSONArray jsonBarriles;
    private void cargarBarriles(){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                try {
                    jsonBarriles=getFunciones().consultaJson("exec sp_ll_cargabarriles", SQLConnection.db_AAB);
                    String[][] spaceProbes= new String[jsonBarriles.length()][4];
                    for (int x=0;x<jsonBarriles.length();x++){
                        JSONObject jsonObject=jsonBarriles.getJSONObject(x);
                        spaceProbes[x][0]=jsonObject.getString("etiqueta");
                        spaceProbes[x][1]=jsonObject.getString("tapa");
                        spaceProbes[x][2]=jsonObject.getString("uso");
                        spaceProbes[x][3]=jsonObject.getString("lanza");
                    }
                    iniciarBom.setBackgroundColor(ContextCompat.getColor(IniciarLlenado.this,R.color.verde_desactivado) );
                    //toggleButtons(jsonBarriles.length()!=3);
                    dataTable.setDataAdapter(new SimpleTableDataAdapter(IniciarLlenado.this, spaceProbes));
                    dataTable.setAutoHeight(jsonBarriles.length());
                    cargarLanzas();
                    if(obtenerLanDisp())
                        iniciarBombeo();
                } catch (Exception e) {
                    getFunciones().mostrarMensaje(e.getMessage());
                }finally {
                    progressBar.setVisibility(View.GONE);
                    etiqueta.requestFocus();
                }

            }
        });

    }
    private void toggleButtons(boolean opc){
        iniciarBom.setEnabled(!opc);
        aceptar.setEnabled(opc);
        etiqueta.setEnabled(opc);
        camara.setEnabled(opc);
        if(!opc){
            iniciarBom.setBackgroundColor(ContextCompat.getColor(this,R.color.verde_claro) );
        }
    }
    private void cargarLanzas() throws Exception{
        JSONArray jsonArray=getFunciones().consultaJson("select idlanza, edoLlenada from cm_lanza", SQLConnection.db_AAB);
        for (int x=0;x<jsonArray.length();x++){
            JSONObject jsonObject=jsonArray.getJSONObject(x);
            boolean activo=jsonObject.getString("edollenada").equals("0");
            int color=activo?Color.RED:Color.GREEN;
            switch (jsonObject.getString("idlanza")){
                case "1":
                    lanza1.setBackgroundColor(color);
                    //lanza1.setEnabled(activo);
                    break;
                case "2":
                    lanza2.setBackgroundColor(color);
                    //lanza2.setEnabled(activo);
                    break;
                case "3":
                    lanza3.setBackgroundColor(color);
                    //lanza3.setEnabled(activo);
                    break;
            }
        }
        toggleButtons(!obtenerLanDisp());
    }

    @Override
    public void onBackPressed() {
        if(timer!=null)
            timer.cancel();
        super.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarBarriles();
    }

    private class revisarFlujometros extends TimerTask {


        @Override
        public void run() {
            try {
                obtenerDatosLanzas();
            }catch (Exception e){
                //getFunciones().mostrarMensaje(e.getMessage());
            }
        }

        private void revisarAvance() throws Exception{
            JSONArray jsonArray=getFunciones().consultaJson("select Valor from com_tagsactual where Idtag=13",SQLConnection.db_Emba);
            if(jsonArray.length()>0){
                JSONObject jsonObject=jsonArray.getJSONObject(0);
                if(jsonObject.getInt("valor")!=0){
                    IniciarLlenado.this.runOnUiThread(new Runnable() {
                        public void run() {
                            terminarBombeo();
                        }
                    });
                }
            }
        }
        private void obtenerDatosLanzas() throws Exception{
            JSONArray jsonArray=getFunciones().consultaJson("select idtag,Valor from COM_TagsActual where idtag in(6,9,12)",SQLConnection.db_Emba);
            for (int x=0;x<jsonArray.length();x++){
                JSONObject jsonObject=jsonArray.getJSONObject(x);
                switch (jsonObject.getString("idtag")){
                    case "6":
                        lanza1.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    lanza1.setText(jsonObject.getString("valor"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        getFunciones().insertaData("update cm_lanza set flujototal=" + jsonObject.getString("valor") + " Where idlanza=1",SQLConnection.db_AAB);
                        break;
                    case "9":
                        lanza2.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    lanza2.setText(jsonObject.getString("valor"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        //lanza2.setText(jsonObject.getString("valor"));
                        getFunciones().insertaData("update cm_lanza set flujototal=" + jsonObject.getString("valor") + " Where idlanza=2",SQLConnection.db_AAB);
                        break;
                    case "12":
                        lanza3.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    lanza3.setText(jsonObject.getString("valor"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        //lanza3.setText(jsonObject.getString("valor"));
                        getFunciones().insertaData("update cm_lanza set flujototal=" + jsonObject.getString("valor") + " Where idlanza=3",SQLConnection.db_AAB);
                        break;
                }
            }
            revisarAvance();
        }
    }
}