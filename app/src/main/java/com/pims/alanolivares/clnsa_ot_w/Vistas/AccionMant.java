package com.pims.alanolivares.clnsa_ot_w.Vistas;

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
    private Button aceptar;
    private EditText aros,tapas,duela;
    private CheckBox cepillado,reparacion,canal;
    private TextView etiqueta;
    private String eti,barrica;
    private ProgressBar progressBar;

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
                    JSONArray jsonArray=getFunciones().consultaJson("select M.IdMantenimiento,M.IdBarrica,M.IdUsuario,U.Nombre as NomUsu " +
                            "from PR_Mantenimiento M inner Join WM_Barrica B on B.IdBarrica = M.IdBarrica " +
                            " inner Join CM_Usuario U on U.IdUsuario = M.IdUsuario " +
                            "Where B.Consecutivo = SUBSTRING('"+eti+"',5,6) and convert(varchar(10),M.Fecha,120)='"+getFunciones().getCurrDate("yyyy-MM-dd")+"'", SQLConnection.db_AAB);
                    if(jsonArray.length()>0){
                        JSONObject jsonObject=jsonArray.getJSONObject(0);
                        int cantAros=(aros.getText().toString().isEmpty()?0:Integer.valueOf(aros.getText().toString()));
                        int cantTapas=(tapas.getText().toString().isEmpty()?0:Integer.valueOf(tapas.getText().toString()));
                        int cantDuelas=(duela.getText().toString().isEmpty()?0:Integer.valueOf(duela.getText().toString()));
                        getFunciones().insertaData("exec sp_MantAcciones " +
                                "'"+jsonObject.getString("idmantenimiento")+"','"+cantAros+"','"+cantTapas+"','"+cantDuelas+"','"+
                                (cepillado.isChecked()?"1":"0")+"','"+(reparacion.isChecked()?"1":"0")+"','"+
                                (canal.isChecked()?"1":"0")+"'",SQLConnection.db_AAB);
                        getFunciones().mostrarMensaje("Reparaci??n registrada correctamente");
                        onBackPressed();
                    }else{
                        getFunciones().makeErrorSound();
                        getFunciones().mostrarMensaje("Ocurri?? un problema al obtener el ID del mantenimiento");
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