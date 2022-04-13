package com.pims.alanolivares.clnsa_ot_w.Vistas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Funciones.BotonMenuAdapter;
import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadre;
import com.pims.alanolivares.clnsa_ot_w.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class TraspasoHooverOpc extends ClasePadre {
    private TextView total,donador,receptor;
    private String IdOrden;
    private ProgressBar progressBar;
    private GridView menuGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traspaso_hoover_opc);
        IdOrden =getIntent().getStringExtra("IdOrden");
        setTitulo("Orden: "+IdOrden);
        menuGrid = findViewById(R.id.rellenado_menu);
        total=findViewById(R.id.totalMR);
        donador=findViewById(R.id.donadorMR);
        receptor=findViewById(R.id.receptorMR);
        progressBar=findViewById(R.id.progressMR);
        menuGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent menu=null;
                switch (i){
                    case 0://Identificar completos
                        menu=new Intent(getApplicationContext(),IdentTrasiego.class);
                        menu.putExtra("IdOrden",IdOrden);
                        menu.putExtra("tipo","2");
                        break;
                    case 1://Identificar parciales
                        menu=new Intent(getApplicationContext(),IdentTrasiegoParcial.class);
                        menu.putExtra("IdOrden",IdOrden);
                        break;
                }
                startActivity(menu);
            }
        });
    }
    private void obtenerDatos(){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray jsonArray= getFunciones().consultaJson("select (select count(*) from WM_OperacionTQHBarrilHis where tipoLl=1 and IdOrden='"+IdOrden+"') as Completos," +
                            " (select count(*) from WM_OperacionTQHBarrilHis where tipoLl=2 and IdOrden='"+IdOrden+"') as Parciales", SQLConnection.db_AAB);
                    JSONObject jsonObject=jsonArray.getJSONObject(0);
                    int completo=jsonObject.getInt("completos");
                    int parcial=jsonObject.getInt("parciales");
                    total.setText("Total barriles: "+(completo+parcial));
                    donador.setText("Completos: "+completo);
                    receptor.setText("Parciales: "+parcial);
                } catch (Exception e) {
                    getFunciones().mostrarMensaje(e.getMessage());
                }finally {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
    @Override
    protected void onResume() {
        String[] menuText = { "Barriles completos", "Barriles parciales"};
        int[] images = { R.drawable.sobrante,R.drawable.sobrante};
        BotonMenuAdapter adapter = new BotonMenuAdapter(this, menuText,images);
        menuGrid.post(new Runnable() {
            @Override
            public void run() {
                menuGrid.setAdapter(adapter);
            }
        });
        super.onResume();
        obtenerDatos();
    }
}