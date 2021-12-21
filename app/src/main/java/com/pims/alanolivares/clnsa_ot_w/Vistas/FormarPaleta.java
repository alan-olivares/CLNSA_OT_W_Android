package com.pims.alanolivares.clnsa_ot_w.Vistas;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadre;
import com.pims.alanolivares.clnsa_ot_w.Funciones.FuncionesGenerales;
import com.pims.alanolivares.clnsa_ot_w.Funciones.NoScrollViewTable;
import com.pims.alanolivares.clnsa_ot_w.R;

import org.json.JSONArray;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.model.TableColumnDpWidthModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class FormarPaleta extends ClasePadre {
    Button agregar,formar,camara;
    EditText etiqueta;
    NoScrollViewTable dataTable;
    TextView total;
    String pallet;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formar_paleta);
        inicializar();
        setEtiqueta(etiqueta);
        setCamara(camara);
        agregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validaBarril();
            }
        });
        formar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validaFormar();
            }
        });
        cargarBarriles();
    }
    private void inicializar(){
        agregar=findViewById(R.id.agregarFP);
        formar=findViewById(R.id.formarFP);
        camara=findViewById(R.id.camaraFP);
        etiqueta=findViewById(R.id.etiquetaFP);
        dataTable=findViewById(R.id.tablaFP);
        total=findViewById(R.id.totalFP);
        progressBar=findViewById(R.id.progressFP);
        String[] spaceProbeHeaders={"Etiqueta","Año","Alcohol","Uso"};
        dataTable.setHeaderAdapter(new SimpleTableHeaderAdapter(this,spaceProbeHeaders));
        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(this, 4, 80);
        columnModel.setColumnWidth(0,150);
        columnModel.setColumnWidth(2,120);
        dataTable.setColumnModel(columnModel);
        setCopyEtiqueta(dataTable);
    }
    String tabla[][];
    private void cargarBarriles(){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray jsonArray = getFunciones().consultaJson("exec sp_Pallet '"+getFunciones().getPistola()+"'", SQLConnection.db_AAB);
                    if(jsonArray.length()>0){
                        pallet= jsonArray.getJSONObject(0).getString("pallet");
                        tabla=getFunciones().consultaTabla("exec sp_PalletList '"+pallet+"'",SQLConnection.db_AAB,4);
                        agregar.setEnabled(tabla.length<9);
                        etiqueta.setEnabled(tabla.length<9);
                        camara.setEnabled(tabla.length<9);
                        total.setText("Total: "+tabla.length);
                        dataTable.setDataAdapter(new SimpleTableDataAdapter(FormarPaleta.this, tabla));
                        dataTable.setAutoHeight(tabla.length);
                    }else{
                        getFunciones().mostrarMensaje("No se pudo obtener el número de pallet");
                        onBackPressed();
                    }
                } catch (Exception e) {
                    getFunciones().mostrarMensaje(e.getMessage());
                    onBackPressed();
                }finally {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

    }
    private void validaBarril(){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                try {
                    String eti=etiqueta.getText().toString();
                    if(tabla.length==0){
                        agregarBarril(eti);
                    }else if(tabla.length>9){
                        getFunciones().mostrarMensaje("Esta Paleta ya se completó seleccione la función de formar otra");
                    }else if(!getFunciones().valEtiBarr(eti)){
                        getFunciones().mostrarMensaje("La etiqueta es incorrecta");
                    }else{
                        JSONArray jsonArray= getFunciones().consultaJson("exec sp_PalletValida_v2 '"+pallet+"','"+eti+"'", SQLConnection.db_AAB);
                        String msg=jsonArray.getJSONObject(0).getString("msg");
                        if(msg.equals("")){
                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which){
                                        case DialogInterface.BUTTON_POSITIVE:
                                            try {
                                                agregarBarril(eti);
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
                            AlertDialog.Builder builder = new AlertDialog.Builder(FormarPaleta.this);
                            builder.setMessage("¿Desea agregar el barril?").setPositiveButton("Si", dialogClickListener)
                                    .setNegativeButton("No", dialogClickListener).show();
                        }else{
                            getFunciones().mostrarMensaje(msg);
                        }
                    }
                    etiqueta.setText("");
                } catch (Exception e) {
                    getFunciones().mostrarMensaje(e.getMessage());
                }
                finally {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

    }
    private void agregarBarril(String eti) throws Exception{
        if(getFunciones().valEtiBarr(eti)){
            getFunciones().escribirLog(getFunciones().getCurrDate("dd/MM/yy hh:mm:ss")+"|"+pallet+"|"+eti+"|0");
            getFunciones().insertaData("Update WM_Barrica Set IdPallet = '"+pallet+"' Where Consecutivo = SUBSTRING('"+eti+"',5,6)",SQLConnection.db_AAB);
            cargarBarriles();
        }else{
            getFunciones().mostrarMensaje("Etiqueta invalida");
        }

    }
    private void validaFormar(){
        if(tabla.length<9){
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            formarPaleta();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            dialog.dismiss();
                            break;
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Esta paleta no está completa. Presione Si para cerrarla o No para cancelar el proceso").setPositiveButton("Si", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }else{
            formarPaleta();
        }
    }
    private void formarPaleta(){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                try {
                    getFunciones().insertaData("exec sp_PalletEstatus '"+pallet+"'",SQLConnection.db_AAB);
                } catch (Exception e) {
                    getFunciones().mostrarMensaje("Problema al crear paleta, razón: "+e.getMessage());
                }
                try {
                    cargarBarriles();
                } catch (Exception e) {
                    getFunciones().mostrarMensaje("Se generó un porblema al generar un nuevo pallet, razón: "+e.getMessage());
                    onBackPressed();
                }
                progressBar.setVisibility(View.INVISIBLE);

            }
        });

    }

}