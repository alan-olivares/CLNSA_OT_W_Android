package com.pims.alanolivares.clnsa_ot_w.Vistas;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadre;
import com.pims.alanolivares.clnsa_ot_w.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class AccionMant extends ClasePadre {
    Button aceptar;
    EditText aros,tapas,duela;
    CheckBox cepillado,reparacion,canal;
    TextView etiqueta;
    String eti,barrica;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accion_mant);
        inicializar();
        eti=getIntent().getStringExtra("etiqueta");
        barrica=getIntent().getStringExtra("idBarrica");
        etiqueta.setText("Barril: "+eti);
        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                agregarMantenimiento();
            }
        });
    }
    private void inicializar(){
        aceptar=findViewById(R.id.aceptarAM);
        aros=findViewById(R.id.arosAM);
        tapas=findViewById(R.id.tapasAM);
        duela=findViewById(R.id.duelaAM);
        cepillado=findViewById(R.id.cepilladoAM);
        reparacion=findViewById(R.id.reparacionAM);
        canal=findViewById(R.id.canalAM);
        etiqueta=findViewById(R.id.etiquetaAM);
        progressBar=findViewById(R.id.progressAM);
    }
    private void agregarMantenimiento(){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray jsonArray=getFunciones().consultaJson("exec sp_Uso_Mantenimiento_Barril '"+getFunciones().getCurrDate("yyyy-MM-dd")+"','"+eti+"'", SQLConnection.db_AAB);
                    if(jsonArray.length()>0){
                        JSONObject jsonObject=jsonArray.getJSONObject(0);
                        int cantAros=(aros.getText().toString().isEmpty()?0:Integer.valueOf(aros.getText().toString()));
                        int cantTapas=(tapas.getText().toString().isEmpty()?0:Integer.valueOf(tapas.getText().toString()));
                        int cantDuelas=(duela.getText().toString().isEmpty()?0:Integer.valueOf(duela.getText().toString()));
                        getFunciones().insertaData("exec sp_MantAcciones " +
                                "'"+jsonObject.getString("idmantenimiento")+"','"+cantAros+"','"+cantTapas+"','"+cantDuelas+"','"+
                                (cepillado.isChecked()?"1":"0")+"','"+(reparacion.isChecked()?"1":"0")+"','"+
                                (canal.isChecked()?"1":"0")+"'",SQLConnection.db_AAB);
                        getFunciones().mostrarMensaje("Reparación registrada correctamente");
                        onBackPressed();
                    }else{
                        getFunciones().mostrarMensaje("Ocurrió un problema al obtener el ID del mantenimiento");
                    }
                } catch (Exception e) {
                    getFunciones().mostrarMensaje(e.getMessage());
                }finally {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

    }
}