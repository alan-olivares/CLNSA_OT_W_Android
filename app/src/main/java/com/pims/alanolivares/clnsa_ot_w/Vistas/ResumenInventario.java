package com.pims.alanolivares.clnsa_ot_w.Vistas;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadre;
import com.pims.alanolivares.clnsa_ot_w.Funciones.NoScrollViewTable;
import com.pims.alanolivares.clnsa_ot_w.Funciones.SpinnerObjeto;
import com.pims.alanolivares.clnsa_ot_w.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.codecrafters.tableview.model.TableColumnDpWidthModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class ResumenInventario extends ClasePadre {
    Spinner planta,bodega,costado,fila,torre,nivel;
    NoScrollViewTable tabla;
    TextView barriles,litros;
    Button buscar;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumen_inventario);
        inicializar();
    }
    private void inicializar(){
        bodega=findViewById(R.id.bodegaRI);
        costado=findViewById(R.id.costadoRI);
        fila=findViewById(R.id.filaRI);
        torre=findViewById(R.id.torreRI);
        nivel=findViewById(R.id.nivelRI);
        planta=findViewById(R.id.plantaRI);
        tabla=findViewById(R.id.tablaRI);
        //setCopyEtiqueta(tabla);
        tabla.setHeaderAdapter(new SimpleTableHeaderAdapter(this,new String[]{"Etiqueta","Año","Alcohol","Tipo","Litros"}));
        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(this, 4, 90);
        columnModel.setColumnWidth(0,150);
        columnModel.setColumnWidth(2,130);
        tabla.setColumnModel(columnModel);
        barriles=findViewById(R.id.barrilesRI);
        litros=findViewById(R.id.litrosRI);
        buscar=findViewById(R.id.buscarRI);
        progressBar=findViewById(R.id.progressBarRI);
        getFunciones().llenarSpinnerLocal("select -1 as id,'' as valor union select PlantaID,Nombre from AA_Plantas",planta);
        planta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                vaciarValor(1);
                SpinnerObjeto planta=(SpinnerObjeto)adapterView.getSelectedItem();
                buscar.setEnabled(planta.getId()!=-1);
                if(planta.getId()!=-1){
                    getFunciones().llenarSpinnerLocal("select -1 as id,'' as valor union select AlmacenID,Nombre from AA_Almacen where PlantaID="+planta.getId(),bodega);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        bodega.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                vaciarValor(2);
                SpinnerObjeto bod=(SpinnerObjeto)adapterView.getSelectedItem();
                if(bod.getId()!=-1){
                    getFunciones().llenarSpinnerLocal("select -1 as id,'' as valor,-1 as conse union select AreaId, Nombre,Consecutivo from AA_Area A WHERE AlmacenId="+bod.getId()+" order by Consecutivo",costado);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        costado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                vaciarValor(3);
                SpinnerObjeto cos=(SpinnerObjeto)adapterView.getSelectedItem();
                if(cos.getId()!=-1){
                    getFunciones().llenarSpinnerLocal("select -1 as id,'' as valor,-1 as conse union SELECT SeccionID,Nombre,Consecutivo FROM AA_Seccion WHERE AreaId="+cos.getId()+" ORDER BY  Consecutivo",fila);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        fila.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                vaciarValor(4);
                SpinnerObjeto fil=(SpinnerObjeto)adapterView.getSelectedItem();
                if(fil.getId()!=-1){
                    getFunciones().llenarSpinnerLocal("select -1 as id,'' as valor,-1 as conse union SELECT PosicionId,Nombre,Consecutivo FROM AA_Posicion WHERE SeccionID="+fil.getId()+" ORDER BY  Consecutivo",torre);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        torre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                vaciarValor(5);
                SpinnerObjeto torr=(SpinnerObjeto)adapterView.getSelectedItem();
                if(torr.getId()!=-1){
                    getFunciones().llenarSpinnerLocal("select -1 as id,'' as valor,-1 as conse union SELECT NivelId,Nombre,Consecutivo FROM AA_Nivel WHERE PosicionId="+torr.getId()+" ORDER BY  Consecutivo ",nivel);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        nivel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                vaciarValor(6);
                SpinnerObjeto niv=(SpinnerObjeto)adapterView.getSelectedItem();
                if(niv.getId()!=-1){
                    try {
                        cargarTabla();
                    } catch (Exception e) {
                        getFunciones().mostrarMensaje(e.getMessage());
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        buscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            cargarTabla();
                        } catch (Exception e) {
                            getFunciones().mostrarMensaje(e.getMessage());
                        }finally {
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });

            }
        });
    }
    private void cargarTabla() throws Exception {
        SpinnerObjeto plan=(SpinnerObjeto)planta.getSelectedItem();
        if(plan.getId()!=-1){
            SpinnerObjeto bod=(SpinnerObjeto)bodega.getSelectedItem();
            SpinnerObjeto fil=(SpinnerObjeto)fila.getSelectedItem();
            SpinnerObjeto torr=(SpinnerObjeto)torre.getSelectedItem();
            SpinnerObjeto cos=(SpinnerObjeto)costado.getSelectedItem();
            SpinnerObjeto niv=(SpinnerObjeto)nivel.getSelectedItem();
            String condicion=((bod!=null && bod.getId()!=-1)?" where B.Bodega="+bod.getId():"")+
                    ((cos!=null && cos.getId()!=-1)?" and costado="+cos.getId():"")+((fil!=null && fil.getId()!=-1)?" and fila="+fil.getId():"")+
                    ((torr!=null && torr.getId()!=-1)?" and torre="+torr.getId():"")+((niv!=null && niv.getId()!=-1)?" and nivel="+niv.getId():"");
            String consultaTotal="select count(B.consecutivo) as Barriles,IFNULL(sum(B.litros),0)  as Litros " +
                    " from wm_barril B join cm_alcohol A ON B.alcohol=A.idalcohol " +
                    "join CM_codificacion C ON B.tipo=C.idcodificacion "+condicion;
            String consulta="select B.Anio as annio, A.Descripcion as Alcohol, C.Codigo as Tipo,'0101' || substr('000000'|| B.consecutivo,-6) as Etiqueta , IFNULL(B.litros,0) as Litros " +
                    "from wm_barril B join cm_alcohol A ON B.alcohol=A.idalcohol join CM_codificacion C ON B.Tipo=C.idcodificacion  " +
                            "join AA_nivel N ON B.nivel=N.nivelid "+condicion;
            JSONArray jsonArray=getFunciones().getJsonLocal(consulta);
            String datos[][]=new String[jsonArray.length()][5];
            if(jsonArray.length()>0){
                for(int x=0;x<jsonArray.length();x++){
                    JSONObject json=jsonArray.getJSONObject(x);
                    datos[x][0]=json.getString("Etiqueta");
                    datos[x][1]=json.getString("annio");
                    datos[x][2]=json.getString("Alcohol");
                    datos[x][3]=json.getString("Tipo");
                    datos[x][4]=getFunciones().formatNumber(json.getString("Litros"));
                }
                jsonArray=getFunciones().getJsonLocal(consultaTotal);
                barriles.setText("Barriles: "+jsonArray.getJSONObject(0).getString("Barriles"));
                litros.setText("Litros: "+getFunciones().formatNumber(jsonArray.getJSONObject(0).getString("Litros") ));
                tabla.setDataAdapter(new SimpleTableDataAdapter(this, datos));
                tabla.setAutoHeight(datos.length);
                tabla.setVisibility(View.VISIBLE);
            }else{
                getFunciones().mostrarMensaje("No se encontraron barriles en está ubicación, verifica los datos o intenta sincronizar la bodega primero ");
            }

        }
    }
    private void vaciarValor(int caso){
        switch (caso){
            case 1://Plantas
                bodega.setAdapter(null);
            case 2://Bodegas
                costado.setAdapter(null);
            case 3://Costados
                fila.setAdapter(null);
            case 4://Filas
                torre.setAdapter(null);
            case 5://Torres
                nivel.setAdapter(null);
            case 6://Nivel
                tabla.setVisibility(View.GONE);
                barriles.setText("");
                litros.setText("");
                //tabla.setDataAdapter(new SimpleTableDataAdapter(this, new String[][]{}));

        }
    }
}