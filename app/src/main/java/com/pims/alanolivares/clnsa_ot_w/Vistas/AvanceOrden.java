package com.pims.alanolivares.clnsa_ot_w.Vistas;

import androidx.core.view.MenuItemCompat;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadre;
import com.pims.alanolivares.clnsa_ot_w.Funciones.NoScrollViewTable;
import com.pims.alanolivares.clnsa_ot_w.R;

import java.util.ArrayList;

import de.codecrafters.tableview.model.TableColumnDpWidthModel;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class AvanceOrden extends ClasePadre {
    private NoScrollViewTable dataTable;
    private TextView cantidad;
    private ProgressBar progressBar;
    private SearchView sear;
    private String tabla[][];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avance_orden);
        setTitulo(getIntent().getStringExtra("titulo"));
        inicializar();
        if(getIntent().getStringExtra("caso").equals("1")){
            cargarBarriles("exec sp_ll_avance "+getIntent().getStringExtra("idLote"));
        }else if(getIntent().getStringExtra("caso").equals("2")){
            cargarBarriles("exec sp_rell_avance "+getIntent().getStringExtra("IdOrden"));
        }
    }
    private void inicializar(){
        dataTable=findViewById(R.id.tablaAO);
        setCopyEtiqueta(dataTable,0);
        cantidad=findViewById(R.id.totalAO);
        progressBar=findViewById(R.id.progressAO);
        String[] spaceProbeHeaders={"Etiqueta","Tapa","Uso","Litros"};
        dataTable.setHeaderAdapter(new SimpleTableHeaderAdapter(this,spaceProbeHeaders));
        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(this, 4, 80);
        columnModel.setColumnWidth(0,150);
        columnModel.setColumnWidth(3,110);
        dataTable.setColumnModel(columnModel);
    }
    protected void cargarBarriles(String consulta){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                try {
                    tabla = getFunciones().consultaTabla(consulta, SQLConnection.db_AAB,4);
                    for(int x=0;x<tabla.length;x++)
                        tabla[x][3]=getFunciones().formatNumber(tabla[x][3]);
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
                listaopc.add("Tapa");
                listaopc.add("Uso");
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