package com.pims.alanolivares.clnsa_ot_w.Vistas;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadreFragment;
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

public class Montacargas extends ClasePadreFragment {
    TableView<String[]> dataTable;
    ProgressBar progressBar;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_montacargas, container, false);
        dataTable=view.findViewById(R.id.monta_table);
        progressBar=view.findViewById(R.id.progressMonta);
        String[] spaceProbeHeaders={"Orden","Tipo","Fecha","Bodega"};
        setTitle("Órdenes de montacargas");
        setHasOptionsMenu(true);
        setTabla(dataTable,spaceProbeHeaders);
        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(getContext(), 4, 95);
        columnModel.setColumnWidth(2,130);
        columnModel.setColumnWidth(1,130);
        dataTable.setColumnModel(columnModel);
        cargarDatos();
        return view;
    }

    @Override
    public void proceso(int index, View vista, BottomSheetDialog bottomSheet) {
        super.proceso(index, vista, bottomSheet);
        ProgressBar progress=vista.findViewById(R.id.progressDR);
        try{
            JSONArray jsonA=getFunciones().getJsonLocal("select OP.Cantidad,OP.Fecha,C.Codigo as Uso,E.Codigo as Edad, B.Nombre as Bodega " +
                    ",substr(A.Nombre,8) as Costado,S.Nombre as Fila  from PR_Op OP " +
                    "inner Join PR_Orden O on O.IdOrden = OP.IdOrden inner Join CM_CodEdad CE on CE.IdCodEdad = OP.IdCodificacion " +
                    "inner Join CM_Edad E on E.IdEdad = CE.IdEdad inner Join CM_Codificacion C on C.IdCodificacion = CE.IdCodificicacion " +
                    "inner Join AA_Area A on A.AlmacenId = O.IDAlmacen and A.AreaId = OP.AreaID inner join aa_almacen B ON B.AlmacenID=O.IdAlmacen " +
                    "inner Join AA_Seccion S on S.AreaId = A.AreaId and S.SeccionID = OP.SeccionID Where OP.IdOrden ="+jsonArray.getJSONObject(index).getString("Orden"));
            TextView titulo=vista.findViewById(R.id.descripcion_titulo);
            titulo.setText("Orden: "+jsonArray.getJSONObject(index).getString("Orden"));
            Button continuar = vista.findViewById(R.id.continuar);
            Button otro = vista.findViewById(R.id.otro);
            NoScrollViewTable descripcion=vista.findViewById(R.id.detales_table);
            TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(getContext(), 7, 90);
            columnModel.setColumnWidth(0,100);
            descripcion.setColumnModel(columnModel);
            String[] head = {"Cantidad", "Fecha", "Uso","Edad","Bodega","Costado","Fila"};
            String [][] datos=getFunciones().consultaTabla(jsonA,7);
            llenarTabla(head,datos,descripcion);
            continuar.setText("Formar paleta");
            otro.setText("Finalizar Orden");
            otro.setVisibility(View.VISIBLE);
            continuar.setEnabled(true);
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
                        getFunciones().ejecutarComLocal("update PR_Orden set estatus=3 where IdOrden="+jsonArray.getJSONObject(index).getString("Orden"));
                        cargarDatos();
                        bottomSheet.dismiss();
                    }catch (Exception e) {
                        getFunciones().mostrarMensaje(e.getMessage());
                    }
                }
            });
        }catch (Exception e){
            getFunciones().mostrarMensaje(e.getMessage());
        }finally {
            progress.setVisibility(View.GONE);
        }
    }

    JSONArray jsonArray;
    @Override
    public void cargarDatos(){
        try{
            jsonArray=getFunciones().getJsonLocal("select Distinct O.IdOrden as Orden, TP.Descripcion as Tipo , strftime('%Y-%m-%d', O.Fecha) as Fecha ,AA.Nombre as Bodega " +
                    " from PR_Orden O inner Join PR_Op OP on OP.IdOrden = O.IdOrden Inner Join CM_Alcohol AL on Al.IdAlcohol = OP.IdAlcohol " +
                    " inner Join CM_Codificacion C on C.IdCodificacion = OP.IdCodificacion inner Join AA_Almacen AA On AA.AlmacenID = O.Idalmacen " +
                    " inner Join AA_Area A on A.AlmacenId = AA.AlmacenID and A.AreaId = OP.AreaID  inner Join AA_Seccion S on S.AreaId = A.AreaId and S.SeccionID = OP.SeccionID " +
                    " inner Join CM_TipoOp TP on TP.IdTipoOp = O.IdTipoOp where O.IdTipoOp in(3) and O.Estatus in (1,2)");
            String barriles[][]=getFunciones().consultaTabla(jsonArray,4);
            dataTable.setDataAdapter(new SimpleTableDataAdapter(getContext(), barriles));
        }catch (Exception e){
            getFunciones().mostrarMensaje(e.getMessage());
        }

    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_monta, menu);
        final MenuItem sincronizar=menu.findItem(R.id.sincronizar);
        final MenuItem reubicar=menu.findItem(R.id.reubicar);
        final MenuItem actualizarBod=menu.findItem(R.id.actualizarBod);
        final MenuItem actualizarEst=menu.findItem(R.id.actualizarEst);
        sincronizar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                sincronizar();
                return false;
            }
        });
        reubicar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent menu=new Intent(getContext(),ReubiTarima.class);
                startActivity(menu);
                return false;
            }
        });
        actualizarBod.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent menu=new Intent(getContext(),SincBodega.class);
                startActivity(menu);
                return false;
            }
        });
        actualizarEst.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                new Inventario().sincEstructura(getContext(),getFunciones());
                return false;
            }
        });
    }

    private void sincronizar(){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                try {
                    guardarProgreso();
                    JSONArray jsonArray=getFunciones().consultaJson("select IdOrden,IdTipoOp,IdLote,IdAlmacen,IdArea,IdSeccion,IdOperario,IdOperarioMon,IdSupervisor,Fecha,Estatus,IdUsuario from PR_Orden where Estatus in (1,2)",SQLConnection.db_AAB);
                    getFunciones().actualizarSQLLocal("PR_Orden",jsonArray,true);
                    jsonArray=getFunciones().consultaJson("select O.IdOperacion,O.IdOrden,O.Cantidad,O.Estatus,O.Fecha,O.IdAlcohol,O.IdCodificacion,O.IdTanque,O.IdLote,O.AreaID,O.SeccionID from PR_Op O inner join PR_Orden P on P.IdOrden=O.IdOrden where P.Estatus in (1,2)",SQLConnection.db_AAB);
                    getFunciones().actualizarSQLLocal("PR_Op",jsonArray,true);
                    jsonArray=getFunciones().consultaJson("select D.IdOperaDetail,D.IdOperacion,D.IdBarrica,D.Capacidad,D.Estatus from PR_OperaDetail D inner join PR_Op O on D.IdOperacion=O.IdOperacion inner join PR_Orden P on P.IdOrden=O.IdOrden where P.Estatus in (1,2)",SQLConnection.db_AAB);
                    getFunciones().actualizarSQLLocal("PR_OperaDetail",jsonArray,true);
                    getFunciones().mostrarMensaje("Los cambios se han cargado correctamente");
                    cargarDatos();
                } catch (Exception e) {
                    getFunciones().mostrarMensaje(e.getMessage());
                }finally {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void guardarProgreso() throws Exception{
        JSONArray jsonArray = getFunciones().getJsonLocal("Select Consecutivo,Racklocfin,Tipo from PR_NvUbicacion");
        //Guradar datos en PR_NvUbicacion
        for(int x=0;x<jsonArray.length();x++){
            JSONObject object=jsonArray.getJSONObject(x);
            getFunciones().insertaData("INSERT INTO PR_NvUbicacion (Consecutivo,RacklocFin,Tipo) Values(" +object.getString("Consecutivo")+ "," +object.getString("Racklocfin")+ "," +object.getString("tipo")+ ")",SQLConnection.db_AAB);
        }
        //Ejecutar procedure
        jsonArray=getFunciones().consultaJson("exec sp_AplicaNivelacion '"+getFunciones().getIdUsuario()+"','"+getFunciones().getPistola()+"'",SQLConnection.db_AAB);
        if(jsonArray.getJSONObject(0).getString("msg").equals("1")){
            getFunciones().borrarRegistros("PR_NvUbicacion");
        }else{
            getFunciones().mostrarMensaje("Error al aplicar las nivelaciones en el servidor, por favor inteta de nuevo");
        }
        //Guradar órdenes
        jsonArray = getFunciones().getJsonLocal("Select IdOrden from PR_Orden where estatus=3");
        for(int x=0;x<jsonArray.length();x++){
            JSONObject json=jsonArray.getJSONObject(x);
            getFunciones().insertaData("update PR_Orden set estatus=3 where IdOrden="+json.getString("IdOrden"),SQLConnection.db_AAB);
        }
    }
}
