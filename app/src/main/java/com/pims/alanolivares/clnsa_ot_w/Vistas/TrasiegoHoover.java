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

public class TrasiegoHoover extends ClasePadreFragment {
    private TableView<String[]> dataTable;
    private String barriles[][];
    private ProgressBar progressBar;

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
        setTitle("Órdenes de trasiego Hoover");
        String[] spaceProbeHeaders={"Orden","Tanque","Fecha","Litros"};
        setTabla(dataTable,spaceProbeHeaders);
        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(getContext(), 4, 100);
        columnModel.setColumnWidth(1,160);
        columnModel.setColumnWidth(2,120);
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
                    datos = getFunciones().consultaTabla("SELECT CASE WHEN A.Nombre is null THEN 'Tanque sin ubicación' ELSE A.Nombre + ', ' + REPLACE(AA.Nombre, 'COSTADO', 'Cos: ') + ', ' + " +
                            "REPLACE(S.Nombre, 'FILA', 'F: ') + ',' + REPLACE(P.Nombre, 'TORRE', 'T: ') + ',' + REPLACE(N.Nombre, 'NIVEL', 'N: ') END AS Ubicación," +
                            "CT.Codigo as 'T. destino',convert(varchar(10),T.FechaLLenado,105) as Llenado,T.Litros as 'L. Disp.' " +
                            " from AA_Almacen A inner Join AA_Area AA on AA.AlmacenId = A.AlmacenID inner Join AA_Seccion S on S.AreaId = AA.AreaId " +
                            " inner join AA_Posicion P on P.SeccionID = S.SeccionID inner Join AA_Nivel N on N.PosicionId = P.PosicionID " +
                            " inner Join WM_RackLoc RL on RL.NivelID = n.NivelID inner Join WM_Pallet Pa on Pa.RackLocId = RL.RackLocID " +
                            " inner Join WM_Tanques T on T.IdPallet = Pa.IdPallet inner join PR_Orden O on T.IdTanque=O.IdLote " +
                            " inner join PR_Op OD on O.IdOrden=OD.IdOrden inner join CM_Tanque CT on OD.IdTanque=CT.IDTanque " +
                            "  where O.IdOrden='"+barriles[index][0]+"'", SQLConnection.db_AAB,4);
                    NoScrollViewTable descripcion=vista.findViewById(R.id.detales_table);
                    TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(getContext(), 4, 120);
                    columnModel.setColumnWidth(0,450);
                    columnModel.setColumnWidth(3,160);
                    descripcion.setColumnModel(columnModel);
                    String[] head = {"Ubicación", "T. destino", "Llenado","L. Disponibles"};
                    llenarTabla(head,datos,descripcion);
                    titulo.setText("Orden: "+barriles[index][0]);
                    final String IdOrden=barriles[index][0];
                    continuar.setEnabled(true);
                    continuar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!getFunciones().iniciaOrden(IdOrden,"2",TrasiegoHooverInicia.class)){
                                cargarDatos();
                            }
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
                    barriles=getFunciones().consultaTabla("select O.IdOrden,'01' + '02' +(right('000000' + convert(varChar(6),T.NoSerie ),6)) as Tanque,convert(varchar,O.Fecha,105) as Fecha,OP.Cantidad as Litros " +
                            "from PR_Orden O inner join WM_Tanques T on O.IdLote=T.IdTanque right Join PR_Op OP on OP.IdOrden = O.IdOrden " +
                            "where O.IdTipoOp=8 and O.Estatus in(1,2)", SQLConnection.db_AAB,4);
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

