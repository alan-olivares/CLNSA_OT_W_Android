package com.pims.alanolivares.clnsa_ot_w.Vistas;

import android.os.Bundle;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadreFragment;
import com.pims.alanolivares.clnsa_ot_w.Funciones.NoScrollViewTable;
import com.pims.alanolivares.clnsa_ot_w.R;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.model.TableColumnDpWidthModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;

public class Relleno extends ClasePadreFragment {
    private TableView<String[]> dataTable;
    private String barriles[][];
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_relleno, container, false);
        dataTable=view.findViewById(R.id.relleno_table);
        progressBar=view.findViewById(R.id.progressRelleno);
        setTitle("Órdenes de Relleno");
        String[] spaceProbeHeaders={"Orden","Fecha","Cantidad","Bodega"};
        setTabla(dataTable,spaceProbeHeaders);
        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(getContext(), 4, 95);
        columnModel.setColumnWidth(1,130);
        columnModel.setColumnWidth(2,120);
        columnModel.setColumnWidth(3,120);
        dataTable.setColumnModel(columnModel);
        return view;
    }

    @Override
    public void proceso(int index, View vista, BottomSheetDialog bottomSheet) {
        super.proceso(index, vista, bottomSheet);
        NoScrollViewTable descripcion=vista.findViewById(R.id.detales_table);
        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(getContext(), 6, 110);
        descripcion.setColumnModel(columnModel);
        TextView titulo=vista.findViewById(R.id.descripcion_titulo);
        titulo.setText("Orden: "+barriles[index][0]);
        ProgressBar progress=vista.findViewById(R.id.progressDR);
        progress.post(new Runnable() {
            @Override
            public void run() {
                try{
                    String datos[][]=getFunciones().consultaTabla("exec sp_OrdenRell_Pistola_Detalle_v2 '"+barriles[index][0]+"'", SQLConnection.db_AAB,6);
                    Button continuar = vista.findViewById(R.id.continuar);
                    String[] head = {"Fecha", "Alcohol", "Uso","Costado","Fila","B. disp"};
                    llenarTabla(head,datos,descripcion);
                    final String IdOrden=barriles[index][0];
                    continuar.setEnabled(true);
                    continuar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!getFunciones().iniciaOrden(IdOrden,"0",MenuRelleno.class)){
                                cargarDatos();
                            }
                            bottomSheet.dismiss();
                        }
                    });

                }catch (Exception e){
                    getFunciones().mostrarMensaje(e.getMessage());
                    bottomSheet.dismiss();
                }finally {
                    progress.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onResume() {
        cargarDatos();
        super.onResume();
    }

    @Override
    public void cargarDatos(){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    barriles=getFunciones().consultaTabla("exec sp_OrdenRell_Pistola", SQLConnection.db_AAB,4);
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
