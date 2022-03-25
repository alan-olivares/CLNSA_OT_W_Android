package com.pims.alanolivares.clnsa_ot_w.Vistas;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadre;
import com.pims.alanolivares.clnsa_ot_w.Funciones.NoScrollViewTable;
import com.pims.alanolivares.clnsa_ot_w.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.listeners.TableDataLongClickListener;
import de.codecrafters.tableview.model.TableColumnDpWidthModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class ReparacionAvance extends ClasePadre {
    NoScrollViewTable dataTable;
    TextView cantidad;
    ProgressBar progressBar;
    private SearchView sear;
    String tabla[][];
    boolean montrando=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reparacion_avance);
        setTitulo("Avance reparación");
        inicializar();
        cargarBarriles();
    }
    private void inicializar(){
        dataTable=findViewById(R.id.tablaAO);
        //setCopyEtiqueta(dataTable,0);
        cantidad=findViewById(R.id.totalAO);
        progressBar=findViewById(R.id.progressAO);
        String[] spaceProbeHeaders={"Etiqueta","Uso","Reparador","Tipo"};
        dataTable.setHeaderAdapter(new SimpleTableHeaderAdapter(this,spaceProbeHeaders));
        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(this, 4, 200);
        columnModel.setColumnWidth(0,150);
        columnModel.setColumnWidth(1,80);
        dataTable.setColumnModel(columnModel);
        dataTable.addDataClickListener(new TableDataClickListener<String[]>() {
            @Override
            public void onDataClicked(int rowIndex, String[] clickedData) {
                final BottomSheetDialog bottomSheet = new BottomSheetDialog(ReparacionAvance.this);
                View vista = getLayoutInflater().inflate(R.layout.detalles_result, null);
                proceso(rowIndex,vista,bottomSheet);
            }
        });
        dataTable.addDataLongClickListener(new TableDataLongClickListener() {
            @Override
            public boolean onDataLongClicked(int rowIndex, Object clickedData) {

                return false;
            }
        });
    }
    public void proceso(int index, View vista,BottomSheetDialog bottomSheet){
        bottomSheet.setContentView(vista);
        bottomSheet.show();
        Button cerrar = vista.findViewById(R.id.cancelar);
        cerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheet.dismiss();
            }
        });
        NoScrollViewTable descripcion=vista.findViewById(R.id.detales_table);
        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(this, 6, 80);
        columnModel.setColumnWidth(3,120);
        columnModel.setColumnWidth(4,120);
        columnModel.setColumnWidth(5,120);
        descripcion.setColumnModel(columnModel);
        ProgressBar progress=vista.findViewById(R.id.progressDR);
        TextView titulo=vista.findViewById(R.id.descripcion_titulo);
        progress.post(new Runnable() {
            @Override
            public void run(){
                try {
                    String mant=tabla[index][4];
                    String consulta="SELECT CAro,CTapas,CDuela,Case CepDuela When 0 then 'No' When 1 then 'Si' end As CepDuela,Case RepCanal When 0 then 'No' When 1 then 'Si' end As RepCanal,Case CanalNvo When 0 then 'No' When 1 then 'Si' end As CanalNvo from PR_MantAcciones Where IdMantenimiento="+mant;
                    String datos[][]=getFunciones().consultaTabla(consulta, SQLConnection.db_AAB,6);
                    titulo.setText("Mantenimiento: "+mant);
                    Button continuar = vista.findViewById(R.id.continuar);
                    String[] head = {"Aros", "Tapas", "Duelas","Cep Duela","Rep Canal","Canal Nvo"};
                    llenarTabla(head,datos,descripcion);
                    continuar.setEnabled(true);
                    continuar.setText("Eliminar");
                    continuar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            eliminaRegistro(mant);
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

    private void cargarBarriles(){
        String consulta="SELECT isnull((('01' + right('00' + convert(varChar(2),1),2) + right('000000' + convert(varChar(6),B.Consecutivo),6))),'Sin Asignar') as Etiqueta, " +
                "C.Codigo as Uso,U.Nombre,Case M.IdTipoMant When 1 then 'Cambio de Aro' When 2 Then 'Reparacion Gral' end TipoMant,M.IdMantenimiento " +
                "from PR_Mantenimiento M inner join WM_Barrica B on B.IdBarrica = M.IdBarrica " +
                "inner Join CM_CodEdad CE on CE.IdCodEdad = B.IdCodificacion " +
                "inner join CM_Codificacion C on C.IdCodificacion = CE.IdCodificicacion inner join CM_Usuario_Web U on U.IdUsuario = M.IdUsuario " +
                "Where Convert(Date,M.Fecha)= Convert(Date,GETDATE()) order by M.IdTipoMant,C.Codigo,B.Consecutivo";
        progressBar.setVisibility(View.VISIBLE);
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                try {
                    tabla = getFunciones().consultaTabla(consulta, SQLConnection.db_AAB,5);
                    updateTable(dataTable,tabla,tabla.length );
                    cantidad.setText("Total: "+tabla.length);
                } catch (Exception e) {
                    getFunciones().mostrarMensaje(e.getMessage());
                }finally {
                    progressBar.setVisibility(View.GONE);
                }

            }
        });

    }
    private void eliminaRegistro(String mante){
        montrando=true;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog.dismiss();
                        try {
                            getFunciones().insertaData("delete from PR_Mantenimiento where IdMantenimiento="+mante,SQLConnection.db_AAB);
                            getFunciones().insertaData("delete from PR_MantAcciones where IdMantenimiento="+mante,SQLConnection.db_AAB);
                            cargarBarriles();
                            getFunciones().mostrarMensaje("Registro borrado con exito");
                        } catch (Exception e) {
                            getFunciones().mostrarMensaje(e.getMessage());
                        }

                        montrando=false;
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        montrando=false;
                        break;

                }
            }
        };
        builder.setMessage("¿Estás seguro de eliminar el registro del mantenimiento "+mante+"?")
                .setPositiveButton("Si", dialogClickListener).setCancelable(false)
                .setNegativeButton("No", dialogClickListener).show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflar el menú; Esto agrega elementos a la barra de acción si está presente.
        getMenuInflater().inflate(R.menu.menu_busqueda, menu);
        final MenuItem ordenar=menu.findItem(R.id.ordenar);
        ordenar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ArrayList<String> listaopc=new ArrayList<>();
                listaopc.add("Etiqueta");
                listaopc.add("Uso");
                listaopc.add("Reparador");
                ordenar(dataTable,tabla,listaopc);
                return false;
            }
        });
        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        sear=(SearchView) MenuItemCompat.getActionView(searchItem);
        sear.setQueryHint("Buscar algún dato");
        sear.setIconifiedByDefault(true);
        sear.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!newText.equals("")){
                    String aux[][]=getFunciones().busquedaTabla(newText,tabla);
                    updateTable(dataTable,aux,aux.length );
                }else
                    updateTable(dataTable,tabla,tabla.length );
                return false;
            }
        });
        //CastButtonFactory.setUpMediaRouteButton(getApplicationContext(),menu,R.id.media_route_menu_item);
        return true;
    }


}