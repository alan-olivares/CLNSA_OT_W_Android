package com.pims.alanolivares.clnsa_ot_w.Vistas;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadre;
import com.pims.alanolivares.clnsa_ot_w.Funciones.NoScrollViewTable;
import com.pims.alanolivares.clnsa_ot_w.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.codecrafters.tableview.model.TableColumnDpWidthModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class ConsultaBarril extends ClasePadre {
    TextView annio,alcohol,tipo,tapa,litros,llenada,relleno,ubicacion,ultima;
    NoScrollViewTable tabla;
    Button camara,aceptar;
    EditText etiqueta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulta_barril);
        inicializar();
        setTitulo("Inventario de barriles");
    }
    private void inicializar(){
        annio=findViewById(R.id.annioCB);
        alcohol=findViewById(R.id.alcoholCB);
        tipo=findViewById(R.id.tipoCB);
        tapa=findViewById(R.id.tapaCB);
        litros=findViewById(R.id.litrosCB);
        llenada=findViewById(R.id.llenadaCB);
        relleno=findViewById(R.id.rellenoCB);
        ubicacion=findViewById(R.id.ubicacionCB);
        tabla=findViewById(R.id.tablaCB);
        setCopyEtiqueta(tabla);
        camara=findViewById(R.id.camaraCB);
        etiqueta=findViewById(R.id.etiquetaCB);
        aceptar=findViewById(R.id.aceptarCB);
        ultima=findViewById(R.id.ultimaCB);
        setCamara(camara);
        setEtiqueta(etiqueta);
        String[] head = {"Etiquetas en la paleta"};
        tabla.setHeaderAdapter(new SimpleTableHeaderAdapter(this,head));
        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(this, 1, 250);
        tabla.setColumnModel(columnModel);
        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String eti=etiqueta.getText().toString();
                if(getFunciones().valEtiBarr(eti)){
                    try {
                        cargaDetalles(eti);

                    } catch (Exception e) {
                        getFunciones().mostrarMensaje(e.getMessage());
                    }
                }else{
                    getFunciones().mostrarMensaje("Etiqueta invalida");
                }
            }
        });
    }
    private void toggleButtons(boolean act){
        camara.setEnabled(act);
        aceptar.setEnabled(act);
        etiqueta.setEnabled(act);
        if(act){
            tabla.setDataAdapter(new SimpleTableDataAdapter(this, new String[][]{}));
            annio.setText("Año: ");
            alcohol.setText("Alcohol: ");
            tipo.setText("Tipo: ");
            tapa.setText("Tapa: ");
            litros.setText("Litros: ");
            llenada.setText("Llenada: ");
            relleno.setText("Relleno: ");
            ubicacion.setText("Ubicación: ");
            etiqueta.setText("");
            ultima.setText("");
            tabla.setAutoHeight(0);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rep, menu);
        final MenuItem actualizar=menu.findItem(R.id.actualizar);
        actualizar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                toggleButtons(true);
                return false;
            }
        });
        return true;
    }
    private void cargaDetalles(String eti) throws Exception {
        JSONArray json=getFunciones().getJsonLocal("select B.Anio, Al.Descripcion as Alcohol, C.Codigo as Tipo,B.Tapa," +
                "strftime('%Y-%m-%d',recepcion) as llenada, strftime('%Y-%m-%d',relleno) as relleno, " +
                "A.Nombre || ';Cos:' ||substr(AR.Nombre,8) ||';F:'||substr(S.Nombre,5) || ';T:' ||  substr(P.Nombre,6) ||';N:' || substr(N.Nombre,6) as ubicacion," +
                "B.Litros, B.idpallet, B.bodega from wm_barril B join cm_alcohol Al ON B.alcohol=Al.idalcohol " +
                "join CM_Codificacion C ON B.Tipo=C.IdCodificacion join aa_almacen A ON B.bodega=A.almacenid " +
                "join aa_area AR ON B.costado=AR.areaid join aa_seccion S ON B.fila=S.seccionid join aa_posicion P ON B.Torre=P.posicionId join aa_nivel N on B.nivel=N.nivelId " +
                "where B.consecutivo = "+Integer.valueOf(eti.substring(4)));
        if(json.length()>0){
            toggleButtons(false);
            String idPallet="",bodega="";
            JSONObject jsonO=json.getJSONObject(0);
            annio.setText("Año: "+jsonO.getString("anio"));
            alcohol.setText("Alcohol: "+jsonO.getString("Alcohol"));
            tipo.setText("Tipo: "+jsonO.getString("Tipo"));
            tapa.setText("Tapa: "+jsonO.getString("tapa"));
            litros.setText("Litros: "+getFunciones().formatNumber(jsonO.getString("Litros")));
            llenada.setText("Llenada: "+jsonO.getString("llenada"));
            relleno.setText("Relleno: "+jsonO.getString("relleno"));
            ubicacion.setText("Ubicación: "+jsonO.getString("ubicacion"));
            idPallet=jsonO.getString("idpallet");
            bodega=jsonO.getString("bodega");
            json=getFunciones().getJsonLocal(" select '0101' || substr('000000'|| consecutivo,-6) as Consecutivo from wm_barril where idpallet="+idPallet);
            String datos[][]=getFunciones().consultaTabla(json,1);
            tabla.setDataAdapter(new SimpleTableDataAdapter(this, datos));
            json=getFunciones().getJsonLocal("select strftime('%Y-%m-%d %H:%M',fecha) as fecha from cm_SyncBodega where idbodega="+bodega);
            ultima.setText("Última sincronización: "+json.getJSONObject(0).getString("fecha"));
            tabla.setAutoHeight(datos.length);
        }else{
            getFunciones().mostrarMensaje("La etiqueta "+eti+" no arrojo resultados, intenta sincronizar las bodegas primero");
        }
    }
}