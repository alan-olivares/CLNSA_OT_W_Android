package com.pims.alanolivares.clnsa_ot_w.Vistas;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadre;
import com.pims.alanolivares.clnsa_ot_w.Funciones.FuncionesGenerales;
import com.pims.alanolivares.clnsa_ot_w.R;

public class RegistraFallas extends ClasePadre {
    Button camara,aceptar;
    EditText etiqueta;
    CheckBox orificio,desajuste;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registra_fallas);
        inicializar();
        setTitulo("Tanque: "+getIntent().getStringExtra("tanque"));
        setEtiqueta(etiqueta);
        setCamara(camara);
        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String eti=etiqueta.getText().toString();
                insertaEtiqueta(eti);
            }
        });
    }
    private void inicializar(){
        camara=findViewById(R.id.camaraRF);
        aceptar=findViewById(R.id.aceptarRF);
        etiqueta=findViewById(R.id.etiquetaRF);
        orificio=findViewById(R.id.orificioRF);
        desajuste=findViewById(R.id.desajusteRF);
    }
    @Override
    public void validaEtiqueta(String eti){
        try {
            super.validaEtiqueta(eti);
            getFunciones().insertaData("exec sp_FallasAsig '"+eti+"','"+(orificio.isChecked()?"1":"0")+"','"+(desajuste.isChecked()?"1":"0")+"'", SQLConnection.db_AAB);
            etiqueta.setText("");
            getFunciones().mostrarMensaje("Barril registrado con exito");
            orificio.setChecked(false);
            desajuste.setChecked(false);
        } catch (Exception e) {
            getFunciones().mostrarMensaje(e.getMessage());
        }
    }


}