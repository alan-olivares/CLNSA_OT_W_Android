package com.pims.alanolivares.clnsa_ot_w.Vistas;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadre;
import com.pims.alanolivares.clnsa_ot_w.Funciones.NoScrollViewTable;
import com.pims.alanolivares.clnsa_ot_w.R;

import de.codecrafters.tableview.model.TableColumnDpWidthModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class AvanceOrden extends ClasePadre {
    NoScrollViewTable dataTable;
    TextView cantidad;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avance_orden);
        setTitulo(getIntent().getStringExtra("titulo"));
        inicializar();
        if(getIntent().getStringExtra("caso").equals("1")){
            cargarBarriles("exec sp_ll_avance "+getIntent().getStringExtra("idLote"));
        }else{
            cargarBarriles("exec sp_rell_avance "+getIntent().getStringExtra("IdOrden"));
        }
    }
    private void inicializar(){
        dataTable=findViewById(R.id.tablaAO);
        setCopyEtiqueta(dataTable);
        cantidad=findViewById(R.id.totalAO);
        progressBar=findViewById(R.id.progressAO);
        String[] spaceProbeHeaders={"Etiqueta","Tapa","Uso","Litros"};
        dataTable.setHeaderAdapter(new SimpleTableHeaderAdapter(this,spaceProbeHeaders));
        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(this, 4, 80);
        columnModel.setColumnWidth(0,150);
        columnModel.setColumnWidth(3,110);
        dataTable.setColumnModel(columnModel);
    }
    private void cargarBarriles(String consulta){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                String tabla[][]= new String[0][];
                try {
                    tabla = getFunciones().consultaTabla(consulta, SQLConnection.db_AAB,4);
                    for(int x=0;x<tabla.length;x++)
                        tabla[x][3]=getFunciones().formatNumber(tabla[x][3]);
                    dataTable.setDataAdapter(new SimpleTableDataAdapter(AvanceOrden.this, tabla));
                    dataTable.setAutoHeight(tabla.length);
                    cantidad.setText("Total: "+tabla.length);
                } catch (Exception e) {
                    getFunciones().mostrarMensaje(e.getMessage());
                }finally {
                    progressBar.setVisibility(View.GONE);
                }

            }
        });

    }
}