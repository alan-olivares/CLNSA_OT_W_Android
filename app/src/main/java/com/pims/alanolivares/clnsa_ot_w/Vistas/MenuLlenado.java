package com.pims.alanolivares.clnsa_ot_w.Vistas;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuPopupHelper;

import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Dialogos.Lanzas;
import com.pims.alanolivares.clnsa_ot_w.Funciones.BotonMenuAdapter;
import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadre;
import com.pims.alanolivares.clnsa_ot_w.Funciones.FuncionesGenerales;
import com.pims.alanolivares.clnsa_ot_w.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

public class MenuLlenado extends ClasePadre {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_llenado);
        String IdLote =getIntent().getStringExtra("IdLote");
        String tanque =getIntent().getStringExtra("tanque");
        setTitulo("Tanque: "+tanque);
        String[] menuText = { "Resulta Pruebas", "Iniciar llenado", "Formar Paletas", "Registrar fallas", "Finalizar Tanque","Config Lanzas","Avance llenado" };
        int[] images = { R.drawable.resultado_pruebas, R.drawable.iniciar_llenado,
                R.drawable.formar_paleta, R.drawable.registrar_fallas,
                R.drawable.finalizar_tanque, R.drawable.configurar_lanzas, R.drawable.avance_llenado };
        GridView menuGrid;
        menuGrid = findViewById(R.id.llenado_menu);
        BotonMenuAdapter adapter = new BotonMenuAdapter(getApplicationContext(), menuText,images);
        menuGrid.setAdapter(adapter);
        Context context=this;
        menuGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent menu=null;
                switch (i){
                    case 0://Pruebas
                        menu=new Intent(context,ResultTest.class);
                        break;
                    case 1://Inciar llenado
                        try {
                            menu=new Intent(context,IniciarLlenado.class);
                            menu.putExtra("idLote",IdLote);
                            menu.putExtra("tanque",tanque);
                            getFunciones().resetProceso();
                        } catch (Exception e) {
                            getFunciones().mostrarMensaje(e.getMessage());
                            return;
                        }
                        break;
                    case 2://Formar paletas
                        menu=new Intent(context,FormarPaleta.class);
                        break;
                    case 3://Registra fallas
                        menu=new Intent(context,RegistraFallas.class);
                        menu.putExtra("tanque",tanque);
                        break;
                    case 4://Finalizar tanque
                        finalizarTanque(IdLote,tanque);
                        return;
                    case 5://Configurar lanzas
                        menu = new Intent(context, Lanzas.class);
                        break;
                    case 6://Avance órden
                        menu=new Intent(context,AvanceOrden.class);
                        menu.putExtra("tanque",tanque);
                        menu.putExtra("idLote",IdLote);
                        menu.putExtra("titulo","Avance llenado");
                        menu.putExtra("caso","1");
                        break;
                }
                startActivity(menu);
            }
        });
    }

    private void finalizarTanque(String idLote,String tanque){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        try {
                            getFunciones().insertaData("exec sp_RecepFin '"+idLote+"'",SQLConnection.db_AAB);
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
        builder.setMessage("¿Estás seguro de de dar de baja la recepción actual del tanque "+tanque+"?").setPositiveButton("Si", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }
}