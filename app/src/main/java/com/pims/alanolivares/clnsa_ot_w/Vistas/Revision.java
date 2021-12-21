package com.pims.alanolivares.clnsa_ot_w.Vistas;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadreFragment;
import com.pims.alanolivares.clnsa_ot_w.Funciones.FuncionesGenerales;
import com.pims.alanolivares.clnsa_ot_w.Funciones.NoScrollViewTable;
import com.pims.alanolivares.clnsa_ot_w.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.model.TableColumnDpWidthModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;


public class Revision extends ClasePadreFragment {
    TableView<String[]> dataTable;
    ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_revision, container, false);
        dataTable=view.findViewById(R.id.revision_table);
        progressBar=view.findViewById(R.id.progressRevision);
        String[] spaceProbeHeaders={"Bodega","Fecha","Alcohol"};
        setTitle("Revisión de órdenes de trabajo");
        cargarDatos();
        setTabla(dataTable,spaceProbeHeaders);
        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(getContext(), 3, 90);
        columnModel.setColumnWidth(0,180);
        columnModel.setColumnWidth(2,130);
        dataTable.setColumnModel(columnModel);
        return view;
    }

    @Override
    public void proceso(int index, View vista, BottomSheetDialog bottomSheet) {
        super.proceso(index, vista, bottomSheet);
        ProgressBar progress=vista.findViewById(R.id.progressDR);
        progress.setVisibility(View.INVISIBLE);
        try {
            JSONObject json=jsonArray.getJSONObject(index);
            TextView titulo=vista.findViewById(R.id.descripcion_titulo);
            titulo.setText("Bodega: "+jsonArray.getJSONObject(index).getString("bod"));
            Button continuar = vista.findViewById(R.id.continuar);
            Button otro = vista.findViewById(R.id.otro);
            continuar.setEnabled(true);
            NoScrollViewTable descripcion=vista.findViewById(R.id.detales_table);
            TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(getContext(), 3, 110);
            columnModel.setColumnWidth(2,180);
            descripcion.setColumnModel(columnModel);
            String[] head = {"Barril", "Cantidad", "Litros"};
            String [][] datos={{json.getString("barril"),json.getString("cantidad"),getFunciones().formatNumber(json.getString("litros"))}};
            llenarTabla(head,datos,descripcion);
            continuar.setText("Formar Paleta");
            otro.setText("Reparación");
            otro.setVisibility(View.VISIBLE);
            continuar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent menu=new Intent(getContext(),FormarPaleta.class);
                    startActivity(menu);
                }
            });
            otro.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Intent menu=new Intent(getContext(),RevReparacion.class);
                        menu.putExtra("fecha",json.getString("fecha_li"));
                        menu.putExtra("idAlcohol",json.getString("idalcohol"));
                        startActivity(menu);
                    } catch (JSONException e) {
                        getFunciones().mostrarMensaje(e.getMessage());
                    }
                }
            });
            bottomSheet.setContentView(vista);
            bottomSheet.show();
        }catch (Exception e){
            getFunciones().mostrarMensaje(e.getMessage());
        }

    }

    JSONArray jsonArray;
    @Override
    public void cargarDatos(){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    jsonArray=getFunciones().consultaJson("exec sp_LoteRevision_v2", SQLConnection.db_AAB);
                    String barriles[][]=new String[jsonArray.length()][3];
                    for(int x=0;x<jsonArray.length();x++){
                        JSONObject jsonObject=jsonArray.getJSONObject(x);
                        barriles[x][0]=jsonObject.getString("bod");
                        barriles[x][1]=jsonObject.getString("fecha_li");
                        barriles[x][2]=jsonObject.getString("alcohol");
                    }
                    dataTable.setDataAdapter(new SimpleTableDataAdapter(getContext(), barriles));
                } catch (Exception e) {
                    getFunciones().mostrarMensaje(e.getMessage());
                }finally {
                    progressBar.setVisibility(View.GONE);
                }

            }
        },1000);
    }
}
