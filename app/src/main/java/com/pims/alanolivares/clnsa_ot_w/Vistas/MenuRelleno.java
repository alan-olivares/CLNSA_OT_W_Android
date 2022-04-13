package com.pims.alanolivares.clnsa_ot_w.Vistas;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Dialogos.Lanzas;
import com.pims.alanolivares.clnsa_ot_w.Funciones.BotonMenuAdapter;
import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadre;
import com.pims.alanolivares.clnsa_ot_w.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class MenuRelleno extends ClasePadre {
    private TextView total,donador,receptor;
    private String IdOrden;
    private ProgressBar progressBar;
    private GridView menuGrid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_relleno);
        IdOrden =getIntent().getStringExtra("IdOrden");
        setTitulo("Orden: "+IdOrden);

        menuGrid = findViewById(R.id.rellenado_menu);
        total=findViewById(R.id.totalMR);
        donador=findViewById(R.id.donadorMR);
        receptor=findViewById(R.id.receptorMR);
        progressBar=findViewById(R.id.progressMR);

        Context context=this;
        menuGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent menu=null;
                switch (i){
                    case 0://Identificar donadores
                        menu=new Intent(context,IdentRelleno.class);
                        menu.putExtra("IdOrden",IdOrden);
                        menu.putExtra("tipo","1");
                        break;
                    case 1://Identificar receptores
                        menu=new Intent(context,IdentRelleno.class);
                        menu.putExtra("IdOrden",IdOrden);
                        menu.putExtra("tipo","2");
                        break;
                    case 2://Iniciar relleno
                        try {
                            menu=new Intent(context,IniciarRelleno.class);
                            menu.putExtra("IdOrden",IdOrden);
                            menu.putExtra("tipo","2");
                            getFunciones().resetProceso();
                        } catch (Exception e) {
                            getFunciones().mostrarMensaje(e.getMessage());
                            return;
                        }
                        break;
                    case 3://Asigna sobrante
                        try {
                            menu=new Intent(context,IniciarRelleno.class);
                            menu.putExtra("IdOrden",IdOrden);
                            menu.putExtra("tipo","3");
                            getFunciones().resetProceso();
                        } catch (Exception e) {
                            getFunciones().mostrarMensaje(e.getMessage());
                            return;
                        }
                        break;
                    case 4://Formar paleta
                        menu=new Intent(context,FormarPaleta.class);
                        break;
                    case 5://Config Lanzas
                        menu = new Intent(context, Lanzas.class);
                        break;
                    case 6://Avance relleno
                        menu=new Intent(context,AvanceOrden.class);
                        menu.putExtra("IdOrden",IdOrden);
                        menu.putExtra("titulo","Avance relleno");
                        menu.putExtra("caso","2");
                        break;
                    case 7://Finalizar orden
                        finalizarOrden(IdOrden);
                        return;
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
                    JSONArray jsonArray= getFunciones().consultaJson("exec sp_OrdenRell_Identificados "+IdOrden,SQLConnection.db_AAB);
                    JSONObject jsonObject=jsonArray.getJSONObject(0);
                    total.setText("Total barriles: "+jsonObject.getString("cantini"));
                    donador.setText("Donadores: "+jsonObject.getString("donador"));
                    receptor.setText("Receptores: "+jsonObject.getString("relleno"));
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
        String[] menuText = { "Identificar donadores", "Identificar receptores", "Iniciar relleno", "Asigna sobrante", "Formar paleta","Config Lanzas","Avance relleno","Finalizar orden" };
        int[] images = { R.drawable.donadores,R.drawable.receptores, R.drawable.iniciar_llenado,R.drawable.sobrante,
                R.drawable.formar_paleta, R.drawable.configurar_lanzas,
                R.drawable.avance_llenado, R.drawable.finalizar_tanque};
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

    private void finalizarOrden(String IdOrden){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        try {
                            getFunciones().insertaData("Update PR_Orden set Estatus=3 where IdOrden='"+IdOrden+"'", SQLConnection.db_AAB);
                            getFunciones().mostrarMensaje("Orden finalizada con exito");
                            onBackPressed();
                        } catch (Exception e) {
                            getFunciones().mostrarMensaje(e.getMessage());
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Estás seguro de finalizar la orden "+IdOrden+"?").setPositiveButton("Si", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }
}