package com.pims.alanolivares.clnsa_ot_w.Vistas;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import org.json.JSONObject;

import de.codecrafters.tableview.model.TableColumnDpWidthModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class IdentTrasiego extends ClasePadre {
    private NoScrollViewTable dataTable;
    private TextView total,tanque,alcohol,annio,barrVac,uso;
    private Button finalizar,trasegar,camara,aceptar;
    private String idOrden,tanqueId,tipo;
    private EditText etiqueta;
    private int totalBarriles=0;
    private ProgressBar progressBar;
    private int llaves=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ident_trasiego);
        idOrden=getIntent().getStringExtra("IdOrden");
        tipo=getIntent().getStringExtra("tipo");
        setTitulo("Orden: "+idOrden);
        inicializar();
        setEtiqueta(etiqueta);
        setCamara(camara);
        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertaEtiqueta(etiqueta.getText().toString());
            }
        });
        finalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalizarOrden();
            }
        });
        trasegar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent menu=new Intent(IdentTrasiego.this,TrasLoteDest.class);
                menu.putExtra("IdOrden",idOrden);
                menu.putExtra("tanqueId",tanqueId);
                menu.putExtra("tipo",tipo);
                startActivity(menu);
            }
        });
    }
    private void inicializar(){
        llaves=tipo.equals("1")?4:5;
        total=findViewById(R.id.totalIT);
        tanque=findViewById(R.id.tanqueIT);
        alcohol=findViewById(R.id.alcoholIT);
        annio=findViewById(R.id.anioIT);
        barrVac=findViewById(R.id.barrilesVIT);
        uso=findViewById(R.id.usoIT);
        etiqueta=findViewById(R.id.etiquetaIT);
        dataTable=findViewById(R.id.tablaIT);
        finalizar=findViewById(R.id.finalizarIT);
        trasegar=findViewById(R.id.trasegarIT);
        camara=findViewById(R.id.camaraIT);
        aceptar=findViewById(R.id.agregarIT);
        progressBar=findViewById(R.id.progressIT);
        configurar(tipo);

    }
    private void configurar(String tipo){
        String[] spaceProbeHeaders=tipo.equals("1")?new String[]{"Etiqueta","Uso","Edad","Capacidad"}: new String[]{"Etiqueta", "Uso", "Alcohol", "Capacidad","Tipo"};
        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(this, llaves, 90);
        switch (tipo){
            case "1":
                columnModel.setColumnWidth(0,150);
                columnModel.setColumnWidth(3,140);
                break;
            case "2":
                columnModel.setColumnWidth(0,150);
                columnModel.setColumnWidth(2,140);
                columnModel.setColumnWidth(3,140);
                columnModel.setColumnWidth(4,120);
                break;
        }
        dataTable.setColumnModel(columnModel);
        dataTable.setHeaderAdapter(new SimpleTableHeaderAdapter(this,spaceProbeHeaders));
        setCopyEtiqueta(dataTable,0);
    }
    @Override
    public void validaEtiqueta(String eti){
        try {
            super.validaEtiqueta(eti);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        String consulta=tipo.equals("1")?"exec sp_ValidaEtiTras_v2 '"+eti+"','"+idOrden+"'":"exec sp_ValidaEtiTrasHoov '"+eti+"','"+idOrden+"','"+tanqueId+"',1";
                        JSONArray jsonArray=getFunciones().consultaJson(consulta,SQLConnection.db_AAB);
                        getFunciones().mostrarMensaje(jsonArray.getJSONObject(0).getString("msg"));
                        cargarBarriles();
                        etiqueta.setText("");
                    } catch (Exception e) {
                        getFunciones().mostrarMensaje("Se generó un problema al agregar el barril "+e.getMessage());
                    }finally {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
        } catch (Exception e) {
            getFunciones().mostrarMensaje(e.getMessage());
        }

    }
    private void finalizarOrden(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        AlertDialog aler = alert.create();
        alert.setMessage("¿Estás seguro de finalizar esta orden?");
        alert.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    getFunciones().insertaData("Update PR_Orden set Estatus=3 where IdOrden="+idOrden,SQLConnection.db_AAB);
                    onBackPressed();
                } catch (Exception e) {
                    getFunciones().mostrarMensaje("Se generó un problema al finalizar la orden "+e.getMessage());
                }

            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                aler.dismiss();
            }
        });
        aler.show();
    }
    private void cargarDatos(){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                String consulta=tipo.equals("1")?"select top 1 A.Descripcion as Alcohol,C.Codigo,Ta.Codigo as descripcion,convert(int,O.Cantidad) as cantidad," +
                        "Ta.IDTanque as tanque,O.Fecha from PR_Orden P inner join PR_Op O on O.IdOrden=P.IdOrden inner join CM_Tanque Ta on Ta.IDTanque=O.IdTanque " +
                        "inner join CM_Alcohol A on A.IdAlcohol=O.IdAlcohol inner join CM_Codificacion C on C.IdCodificacion=O.IdCodificacion " +
                        "left Join AA_Seccion Se on O.SeccionID=Se.SeccionID left Join AA_Area Ar on Se.AreaId = Ar.AreaId " +
                        "left Join AA_Almacen Am on Ar.AlmacenId=Am.AlmacenID " +
                        "where P.IdOrden= "+idOrden+" and P.Estatus in (1,2)"
                        :"select top 1 A.Descripcion as Alcohol,C.Codigo,Ta.NoSerie as descripcion,Ta.NoSerie as tanque,convert(int,O.Cantidad) as cantidad,O.Fecha " +
                        "from PR_Orden P inner join PR_Op O on O.IdOrden=P.IdOrden inner join WM_Tanques Ta on Ta.IdTanque=O.IdTanque  " +
                        "inner join CM_Alcohol A on A.IdAlcohol=O.IdAlcohol inner join CM_Codificacion C on C.IdCodificacion=O.IdCodificacion " +
                        "left Join AA_Seccion Se on O.SeccionID=Se.SeccionID left Join AA_Area Ar on Se.AreaId = Ar.AreaId " +
                        "left Join AA_Almacen Am on Ar.AlmacenId=Am.AlmacenID " +
                        "where P.IdOrden="+idOrden+" and P.Estatus in (1,2) and P.IdTipoOp=7";
                try {
                    JSONArray jsonArray=getFunciones().consultaJson(consulta,SQLConnection.db_AAB);
                    if(jsonArray.length()>0){
                        JSONObject jsonObject=jsonArray.getJSONObject(0);
                        tanque.setText("Tanque: "+jsonObject.getString("descripcion"));
                        alcohol.setText("Alcohol: "+jsonObject.getString("alcohol"));
                        annio.setText("Año: "+jsonObject.getString("fecha"));
                        barrVac.setText("Barriles a vacíar: "+jsonObject.getString("cantidad"));
                        uso.setText("Uso: "+jsonObject.getString("codigo"));
                        tanqueId=jsonObject.getString("tanque");
                        totalBarriles=Integer.valueOf(jsonObject.getString("cantidad"));
                        cargarBarriles();
                    }else{
                        onBackPressed();
                    }

                } catch (Exception e) {
                    getFunciones().mostrarMensaje(e.getMessage());
                }finally {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatos();
    }

    String tabla[][];
    private void cargarBarriles(){
        String consulta=tipo.equals("1")?"exec sp_Tras_Opera_Detail '"+idOrden+"'"
                :"SELECT isnull((('01' + right('00' + convert(varChar(2),1),2) + right('000000' + convert(varChar(6),OpHis.Consecutivo),6))),'Sin Asignar') as Etiqueta, " +
                "C.Codigo as Edad,Al.Descripcion as Alcohol,OpHis.Capacidad,case when OpHis.tipoLl=1 then 'Completo' else 'Parcial' end as Tipo from WM_OperacionTQH Op " +
                "left join WM_OperacionTQHDetalle OpDe on Op.IdOperacion = OpDe.IdOperacion left join WM_OperacionTQHBarrilHis OpHis on OpHis.IdOperacion=Op.IdOperacion " +
                "left Join WM_LoteBarrica LB on LB.IdLoteBarica = OpHis.IdLoteBarrica inner Join PR_Lote L on L.Idlote = LB.IdLote  " +
                "inner Join CM_CodEdad CE on CE.IdCodEdad = OpHis.IdCodificacion " +
                "inner Join CM_Codificacion C on C.IdCodificacion = CE.IdCodificicacion " +
                "inner Join CM_Edad E on E.IdEdad = CE.IdEdad inner Join CM_Alcohol Al on Al.IdAlcohol = L.IdAlcohol where OpHis.IdOrden='"+idOrden+"'";
        try {
            tabla=getFunciones().consultaTabla(consulta, SQLConnection.db_AAB,llaves);
            //trasegar.setEnabled(tabla.length==totalBarriles);
            toggleButtons(tabla.length<totalBarriles);
            trasegar.setEnabled(tipo.equals("2") && tabla.length>=(totalBarriles-1));
            total.setText("Total: "+tabla.length);
            dataTable.setDataAdapter(new SimpleTableDataAdapter(this, tabla));
            dataTable.setAutoHeight(tabla.length);
        } catch (Exception e) {
            getFunciones().mostrarMensaje(e.getMessage());
        }

    }
    private void toggleButtons(boolean opc){
        trasegar.setEnabled(!opc);
        aceptar.setEnabled(opc);
        etiqueta.setEnabled(opc);
        camara.setEnabled(opc);
    }
}