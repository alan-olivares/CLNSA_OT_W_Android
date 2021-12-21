package com.pims.alanolivares.clnsa_ot_w.Vistas;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadre;
import com.pims.alanolivares.clnsa_ot_w.R;

public class RevReparacion extends ClasePadre {
    String idAlcohol,fecha;
    EditText compas,bisel,duelas,juntura,tapas,poro,orificio;
    Button aceptar;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rev_reparacion);
        idAlcohol=getIntent().getStringExtra("idAlcohol");
        fecha=getIntent().getStringExtra("fecha");
        inicializar();
        setTitulo("Reparaciones");
        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int cantCompas=(compas.getText().toString().isEmpty()?0:Integer.valueOf(compas.getText().toString()));
                            int cantDuelas=(duelas.getText().toString().isEmpty()?0:Integer.valueOf(duelas.getText().toString()));
                            int cantTapas=(tapas.getText().toString().isEmpty()?0:Integer.valueOf(tapas.getText().toString()));
                            int cantOrificio=(orificio.getText().toString().isEmpty()?0:Integer.valueOf(orificio.getText().toString()));
                            int cantBisel=(bisel.getText().toString().isEmpty()?0:Integer.valueOf(bisel.getText().toString()));
                            int cantJuntura=(juntura.getText().toString().isEmpty()?0:Integer.valueOf(juntura.getText().toString()));
                            int cantPoro=(poro.getText().toString().isEmpty()?0:Integer.valueOf(poro.getText().toString()));
                            String consulta="exec sp_RevReparaciones '"+
                                    fecha+"','"+idAlcohol+"','"+cantCompas+"','"+cantDuelas+"','"+cantTapas+"','"+cantOrificio+"','"+cantBisel+"','"+cantJuntura+"','"+cantPoro+"'";
                            getFunciones().insertaData(consulta, SQLConnection.db_AAB);
                            getFunciones().mostrarMensaje("Información guardada con exito");
                            onBackPressed();
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
    private void inicializar(){
        compas=findViewById(R.id.compasRR);
        bisel=findViewById(R.id.biselRR);
        duelas=findViewById(R.id.duelasRR);
        juntura=findViewById(R.id.junturaRR);
        tapas=findViewById(R.id.tapasRR);
        poro=findViewById(R.id.poroRR);
        orificio=findViewById(R.id.orificioRR);
        aceptar=findViewById(R.id.aceptarRR);
        progressBar=findViewById(R.id.progressRevRep);
    }
}