package com.pims.alanolivares.clnsa_ot_w.Vistas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Dialogos.Lanzas;
import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadre;
import com.pims.alanolivares.clnsa_ot_w.Funciones.FuncionesGenerales;
import com.pims.alanolivares.clnsa_ot_w.Funciones.NoScrollViewTable;
import com.pims.alanolivares.clnsa_ot_w.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import de.codecrafters.tableview.model.TableColumnDpWidthModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class IniciarRelleno extends ClasePadre {
    NoScrollViewTable dataTable;
    EditText etiqueta,lanza1,lanza2,lanza3;
    Button camara,aceptar,desactivarLan,iniciarBom;
    Timer timer;
    String IdOrden="",tipo;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciar_relleno);
        inicializar();
        IdOrden=getIntent().getStringExtra("IdOrden");
        tipo=getIntent().getStringExtra("tipo");
        setEtiqueta(etiqueta);
        setCamara(camara);
        seleccionarTipo();
        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                agregarBarril();
            }
        });
        desactivarLan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pas = new Intent(IniciarRelleno.this, Lanzas.class);
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
    private void seleccionarTipo(){
        if(tipo.equals("2")){
            setTitulo("Relleno");
        }else{
            setTitulo("Resto");
        }
    }
    private void inicializar(){
        etiqueta=findViewById(R.id.etiquetaINR);
        camara=findViewById(R.id.camaraINR);
        aceptar=findViewById(R.id.aceptarINR);
        desactivarLan=findViewById(R.id.desactivaLanINR);
        iniciarBom=findViewById(R.id.iniciarBomINR);
        lanza1=findViewById(R.id.l1INR);
        lanza2=findViewById(R.id.l2INR);
        lanza3=findViewById(R.id.l3INR);
        dataTable=findViewById(R.id.llenado_tableINR);
        setCopyEtiqueta(dataTable);
        progressBar=findViewById(R.id.progressIR);
        String[] spaceProbeHeaders={"Etiqueta","Tapa","Uso","Lanza"};
        dataTable.setHeaderAdapter(new SimpleTableHeaderAdapter(this,spaceProbeHeaders));
        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(this, 4, 100);
        columnModel.setColumnWidth(0,150);
        dataTable.setColumnModel(columnModel);
    }
    private void agregarBarril(){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                try {
                    String eti=etiqueta.getText().toString();
                    if(getFunciones().valEtiBarr(eti)){
                        getFunciones().insertaData("insert into ADM_LogOperation(Objeto,Descripcion,Accion,IdUsuario,Scan) values('BTN','Aceptar Codigo','Click',"+getFunciones().getIdUsuario()+"," +getFunciones().getPistola()+ ")",SQLConnection.db_AAB);
                        String consulta=!tipo.equals("3")?"EXEC sp_rell_Existe '"+IdOrden+"','"+eti+"'":"EXEC sp_rell_DonExiste_v2 '"+IdOrden+"','"+eti+"'";
                        JSONArray jsonArray=getFunciones().consultaJson(consulta, SQLConnection.db_AAB);
                        JSONObject jsonObject=jsonArray.getJSONObject(0);
                        if(jsonObject.getString("msg").equals("")){
                            //Agregamos el barril
                            jsonArray=getFunciones().consultaJson("EXEC sp_ll_agregaBarril '"+eti+"','0'", SQLConnection.db_AAB);
                            jsonObject=jsonArray.getJSONObject(0);
                            toggleButtons(jsonObject.getString("total").equals("0"));
                            cargarBarriles();
                        }else{
                            getFunciones().mostrarMensaje(jsonObject.getString("msg"));
                        }
                    }else{
                        getFunciones().mostrarMensaje("Etiqueta invalida");
                    }
                }catch (Exception e){
                    getFunciones().mostrarMensaje(e.getMessage());
                }finally {
                    progressBar.setVisibility(View.GONE);
                    etiqueta.setText("");
                }
            }
        });

    }
    private void iniciarBombeo() throws Exception{
        if(iniciarBom.getText().toString().toLowerCase().equals("iniciar bombeo")){
            iniciarBom.setText("Detener bombeo");
            iniciarBom.setBackgroundColor(ContextCompat.getColor(this,R.color.rojo_danger) );
            getFunciones().avtivarFlujometos();
            for (int x=0;x<jsonBarriles.length();x++){
                JSONObject jsonObject=jsonBarriles.getJSONObject(x);
                getFunciones().escribirLog(getFunciones().getCurrDate("dd/MM/yy hh:mm:ss") + "|" + jsonObject.getString("etiqueta") + "|" + jsonObject.getString("tapa") + "|" + jsonObject.getString("lanza") + "|1");
            }
            timer = new Timer();
            timer.schedule(new revisarFlujometros(), 2000, 1000);
        }else{
            terminarBombeo("2");
            getFunciones().insertaData("update COM_TagsActual set Valor=0 where idtag in (6,9,12)",SQLConnection.db_Emba);
        }
    }
    public void terminarBombeo(String tipo) throws Exception{
        timer.cancel();
        iniciarBom.post(new Runnable() {
            @Override
            public void run() {
                iniciarBom.setText("Iniciar bombeo");
                iniciarBom.setBackgroundColor(ContextCompat.getColor(IniciarRelleno.this,R.color.verde_claro) );
                try {
                    cargarBarriles();
                    cargarLanzas();
                }catch (Exception e){
                    getFunciones().mostrarMensaje("Problema al cargar lanzas, error: "+e.getMessage());
                }
            }
        });
        getFunciones().escribirLog(getFunciones().getCurrDate("dd/MM/yy hh:mm:ss") + "|" + lanza1.getText().toString() + "|" + lanza2.getText().toString()  + "|" + lanza3.getText().toString()  + "|"+tipo);
        getFunciones().reintentadoRegistro("Exec sp_Rell_Registra '"+IdOrden+"','"+tipo+"'",SQLConnection.db_AAB);

        getFunciones().insertaData("update cm_lanza set edollenada=estado, seleccion=0",SQLConnection.db_AAB);
        getFunciones().insertaData("delete pr_opllenado",SQLConnection.db_AAB);
        getFunciones().insertaData("update COM_TagsActual set Valor=0 where idtag in (4,7,10)",SQLConnection.db_Emba);
    }
    private boolean obtenerLanDisp(){
        try {
            JSONArray jsonArray=getFunciones().consultaJson("select count(idlanza) as total from CM_Lanza where EdoLlenada=1 and Seleccion=0", SQLConnection.db_AAB);
            return jsonArray.getJSONObject(0).getString("total").equals("0");
        } catch (Exception e) {
            return false;
        }
    }

    JSONArray jsonBarriles;
    private void cargarBarriles(){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                try {
                    jsonBarriles=getFunciones().consultaJson("exec sp_rell_cargabarriles", SQLConnection.db_AAB);
                    String[][] spaceProbes= new String[jsonBarriles.length()][4];
                    for (int x=0;x<jsonBarriles.length();x++){
                        JSONObject jsonObject=jsonBarriles.getJSONObject(x);
                        spaceProbes[x][0]=jsonObject.getString("etiqueta");
                        spaceProbes[x][1]=jsonObject.getString("tapa");
                        spaceProbes[x][2]=jsonObject.getString("uso");
                        spaceProbes[x][3]=jsonObject.getString("lanza");
                    }
                    iniciarBom.setBackgroundColor(ContextCompat.getColor(IniciarRelleno.this,R.color.verde_desactivado) );
                    //toggleButtons(!obtenerLanDisp());
                    dataTable.setDataAdapter(new SimpleTableDataAdapter(IniciarRelleno.this, spaceProbes));
                    dataTable.setAutoHeight(jsonBarriles.length());
                    cargarLanzas();
                } catch (Exception e) {
                    getFunciones().mostrarMensaje(e.getMessage());
                }finally {
                    progressBar.setVisibility(View.GONE);
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
            int color=activo? Color.RED:Color.GREEN;
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
                getFunciones().mostrarMensaje(e.getMessage());
            }
        }

        private void revisarAvance() throws Exception{
            JSONArray jsonArray=getFunciones().consultaJson("select Valor from com_tagsactual where Idtag=13",SQLConnection.db_Emba);
            if(jsonArray.length()>0){
                JSONObject jsonObject=jsonArray.getJSONObject(0);
                if(jsonObject.getInt("valor")!=0){
                    terminarBombeo("3");
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
            //revisarAvance();
        }
    }
}