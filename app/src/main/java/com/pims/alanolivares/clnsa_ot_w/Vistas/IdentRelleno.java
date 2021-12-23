package com.pims.alanolivares.clnsa_ot_w.Vistas;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadre;

import com.pims.alanolivares.clnsa_ot_w.Funciones.NoScrollViewTable;
import com.pims.alanolivares.clnsa_ot_w.R;

import org.json.JSONArray;

import de.codecrafters.tableview.model.TableColumnDpWidthModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class IdentRelleno extends ClasePadre {
    Button agregar,camara;
    TextView total,orden;
    EditText etiqueta;
    NoScrollViewTable dataTable;
    String idOrden,tipo;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ident_relleno);
        inicializar();
        setEtiqueta(etiqueta);
        setCamara(camara);
        idOrden=getIntent().getStringExtra("IdOrden");
        tipo=getIntent().getStringExtra("tipo");
        orden.setText("Orden: "+idOrden);
        seleccionarTipo();
        cargarBarriles();
        agregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    validaBarril();
                } catch (Exception e) {
                    getFunciones().mostrarMensaje(e.getMessage());
                }
            }
        });
    }
    private void seleccionarTipo(){
        if(tipo.equals("1")){
            setTitulo("Ident. donadores");
        }else{
            setTitulo("Ident. receptores");
        }
    }
    private void validaBarril(){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                try {
                    String eti=etiqueta.getText().toString();
                    if(getFunciones().valEtiBarr(eti)){
                        JSONArray datos=getFunciones().consultaJson("exec sp_ValidaReg  '"+eti+"','"+idOrden+"'", SQLConnection.db_AAB);
                        switch(datos.getJSONObject(0).getInt("valor")){
                            case 1:
                                if(tipo.equals("1")){
                                    getFunciones().mostrarMensaje("Este barril ya fue identificado como donador");
                                    getFunciones().insertaData("insert into ADM_LogOperation(Objeto,Descripcion,Accion,IdUsuario,Scan) values('FNC','ValidaEtiqueta 1-1c','" +eti+ "'," +getFunciones().getIdUsuario()+ "," +getFunciones().getPistola()+ ")",SQLConnection.db_AAB);
                                }else if(tipo.equals("2")){
                                    getFunciones().mostrarMensaje("Este Barril está marcado como Donador; para rellenarlo es necesario marcarlo como Resto");
                                    getFunciones().insertaData("insert into ADM_LogOperation(Objeto,Descripcion,Accion,IdUsuario,Scan) values('FNC','ValidaEtiqueta 1-2a','" +eti+ "'," +getFunciones().getIdUsuario()+ "," +getFunciones().getPistola()+ ")",SQLConnection.db_AAB);
                                }
                                break;
                            case 2:
                                if(tipo.equals("1")){
                                    cambiarEstado();
                                }else if(tipo.equals("2")){
                                    getFunciones().mostrarMensaje("Este barril ya fue identificado como relleno");
                                    getFunciones().insertaData("insert into ADM_LogOperation(Objeto,Descripcion,Accion,IdUsuario,Scan) values('FNC','ValidaEtiqueta 2-1c','" +eti+ "'," +getFunciones().getIdUsuario()+ "," +getFunciones().getPistola()+ ")",SQLConnection.db_AAB);
                                }
                                break;
                            case 0:
                                datos=getFunciones().consultaJson("exec sp_validaOrdenBarril_v2  '"+idOrden+"','"+eti+"','"+tipo+"'", SQLConnection.db_AAB);
                                getFunciones().mostrarMensaje(datos.getJSONObject(0).getString("msg"));
                                cargarBarriles();
                                break;
                        }
                    }else{
                        getFunciones().mostrarMensaje("Etiqueta invalida");
                    }
                    etiqueta.setText("");
                }catch(Exception e){
                    getFunciones().mostrarMensaje(e.getMessage());
                }finally {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

    }
    private void cargarBarriles(){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                try {
                    String consulta="exec sp_Rell_Opera_Detail '"+idOrden+"','"+tipo+"'";
                    String tabla[][]=getFunciones().consultaTabla(consulta, SQLConnection.db_AAB,5);
                    total.setText("Total: "+tabla.length);
                    dataTable.setDataAdapter(new SimpleTableDataAdapter(IdentRelleno.this, tabla));
                    dataTable.setAutoHeight(tabla.length);
                }catch(Exception e){
                    getFunciones().mostrarMensaje(e.getMessage());
                }finally {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });


    }
    private void inicializar(){
        agregar=findViewById(R.id.agregarIR);
        camara=findViewById(R.id.camaraIR);
        total=findViewById(R.id.totalIR);
        etiqueta=findViewById(R.id.etiquetaIR);
        dataTable=findViewById(R.id.tablaIR);
        orden=findViewById(R.id.ordenIR);
        progressBar=findViewById(R.id.progressIDR);
        String[] spaceProbeHeaders={"Etiqueta","Uso","Edad","Capacidad","Estatus"};
        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(this, 5, 80);
        columnModel.setColumnWidth(0,150);
        columnModel.setColumnWidth(3,130);
        columnModel.setColumnWidth(4,130);
        dataTable.setColumnModel(columnModel);
        setCopyEtiqueta(dataTable,0);
        dataTable.setHeaderAdapter(new SimpleTableHeaderAdapter(this,spaceProbeHeaders));
    }
    private void cambiarEstado(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        try {
                            getFunciones().insertaData("exec sp_ValidaEtiRell '"+etiqueta.getText().toString()+"', "+idOrden+",1", SQLConnection.db_AAB);
                            getFunciones().insertaData("insert into ADM_LogOperation(Objeto,Descripcion,Accion,IdUsuario,Scan) values('FNC','ValidaEtiqueta 2-1a','" +etiqueta.getText().toString()+ "'," +getFunciones().getIdUsuario()+ "," +getFunciones().getPistola()+ ")",SQLConnection.db_AAB);
                            cargarBarriles();
                        } catch (Exception e) {
                            getFunciones().mostrarMensaje(e.getMessage());
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Este Barril está marcado como Relleno; desea cambiar su estado a Donador..?").setPositiveButton("Si", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }


}