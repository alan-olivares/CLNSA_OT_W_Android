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

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.model.TableColumnDpWidthModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class TrasiegoHoover extends ClasePadreFragment {
    TableView<String[]> dataTable;
    String barriles[][];
    ProgressBar progressBar;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_trasiego_hoover, container, false);
        dataTable=view.findViewById(R.id.trasiego_hoover_table);
        progressBar=view.findViewById(R.id.progressTrasiegoHoov);
        setTitle("Órdenes de Tra. Hoover");
        String[] spaceProbeHeaders={"Orden","Bodega","Fecha","Tanque","Cant"};
        setTabla(dataTable,spaceProbeHeaders);
        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(getContext(), 4, 100);
        columnModel.setColumnWidth(2,130);
        dataTable.setColumnModel(columnModel);
        return view;
    }

    @Override
    public void proceso(int index, View vista, BottomSheetDialog bottomSheet) {
        super.proceso(index, vista, bottomSheet);
        ProgressBar progress=vista.findViewById(R.id.progressDR);
        TextView titulo=vista.findViewById(R.id.descripcion_titulo);
        Button continuar = vista.findViewById(R.id.continuar);
        progress.post(new Runnable() {
            @Override
            public void run() {
                String datos[][]= new String[0][];
                try {
                    datos = getFunciones().consultaTabla("exec sp_OrdenTras_Pistola_Detalle '"+barriles[index][0]+"'", SQLConnection.db_AAB,6);
                    NoScrollViewTable descripcion=vista.findViewById(R.id.detales_table);
                    TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(getContext(), 6, 110);
                    columnModel.setColumnWidth(1,80);
                    columnModel.setColumnWidth(2,80);
                    columnModel.setColumnWidth(5,80);
                    descripcion.setColumnModel(columnModel);
                    String[] head = {"Alcohol", "Fecha", "Uso","Costado","Fila","B. disp"};
                    llenarTabla(head,datos,descripcion);
                    titulo.setText("Orden: "+barriles[index][0]);
                    final String IdOrden=barriles[index][0];
                    continuar.setEnabled(true);
                    continuar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent menu=new Intent(getContext(),IdentTrasiego.class);
                            menu.putExtra("IdOrden",IdOrden);
                            menu.putExtra("tipo","2");
                            startActivity(menu);
                            bottomSheet.dismiss();
                        }
                    });
                } catch (Exception e) {
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
                    barriles=getFunciones().consultaTabla("exec sp_OrdenTras_Pistola_v2 7", SQLConnection.db_AAB,5);
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

