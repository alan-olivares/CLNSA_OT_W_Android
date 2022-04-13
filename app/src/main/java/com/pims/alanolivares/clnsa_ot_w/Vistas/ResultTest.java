package com.pims.alanolivares.clnsa_ot_w.Vistas;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadre;
import com.pims.alanolivares.clnsa_ot_w.Funciones.FuncionesGenerales;
import com.pims.alanolivares.clnsa_ot_w.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class ResultTest extends ClasePadre {
    private Button aceptar,camara,guardar;
    private EditText etiqueta,uso,edad;
    private TextView aprovados;
    private RadioButton aprovado,descartado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_test);
        aceptar=findViewById(R.id.aceptarRT);
        etiqueta=findViewById(R.id.etiquetaRT);
        uso=findViewById(R.id.usoRT);
        edad=findViewById(R.id.edadRT);
        guardar=findViewById(R.id.guardarRT);
        aprovado=findViewById(R.id.aprovadoRT);
        descartado=findViewById(R.id.descartadoRT);
        aprovados=findViewById(R.id.aprovadosTextRT);
        camara=findViewById(R.id.camaraRT);
        setEtiqueta(etiqueta);
        setCamara(camara);
        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertaEtiqueta(etiqueta.getText().toString());
            }
        });
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertaEtiqueta(etiqueta.getText().toString());
            }
        });
        getAprovados();
    }
    @Override
    public void validaEtiqueta(String eti){
        try {
            super.validaEtiqueta(eti);
            JSONArray jsonArray=getFunciones().consultaJson("exec sp_BarrilIdentidad_v2 '"+etiqueta.getText()+"'", SQLConnection.db_AAB);
            if(jsonArray.length()>0){
                JSONObject jsonObject=jsonArray.getJSONObject(0);
                uso.setText(jsonObject.getString("uso"));
                edad.setText(jsonObject.getString("edad"));
                getFunciones().mostrarMensaje(jsonObject.getString("mensaje"));
            }
        } catch (Exception e) {
            getFunciones().mostrarMensaje(e.getMessage());
        }
    }
    private void getAprovados(){
        try {
            JSONArray jsonArray=getFunciones().consultaJson("exec sp_MantAprov", SQLConnection.db_AAB);
            if(jsonArray.length()>0){
                JSONObject jsonObject=jsonArray.getJSONObject(0);
                aprovados.setText("Total aprovados: "+jsonObject.getString("total"));
            }
        } catch (Exception e) {
            getFunciones().mostrarMensaje(e.getMessage());
        }
    }

}