package com.pims.alanolivares.clnsa_ot_w.Vistas;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadre;
import com.pims.alanolivares.clnsa_ot_w.Funciones.SpinnerObjeto;
import com.pims.alanolivares.clnsa_ot_w.R;

public class AsingRepara extends ClasePadre {
    private Button aceptar;
    private Spinner usuarios;
    private RadioButton cambio,reparacion;
    private TextView etiqueta;
    private String eti,barrica;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asing_repara);
        inizializar();
        eti=getIntent().getStringExtra("etiqueta");
        barrica=getIntent().getStringExtra("idBarrica");
        etiqueta.setText(eti);
        getFunciones().llenarSpinner("Select IdUsuario as id,Nombre as valor from CM_usuario_WEB Where IdGrupo = 5 and Estatus=1",usuarios);
        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String tipoMant=cambio.isChecked()?"1":"2";
                    SpinnerObjeto usoOb=(SpinnerObjeto) usuarios.getSelectedItem();
                    getFunciones().insertaData("exec sp_BarrilMantenimiento '"+barrica+"','"+tipoMant+"','"+usoOb.getId()+"','"+getFunciones().getCurrDate("yyyy-MM-dd")+"'", SQLConnection.db_AAB);
                    Intent menu=new Intent(AsingRepara.this,AccionMant.class);
                    menu.putExtra("idBarrica",barrica);
                    menu.putExtra("etiqueta",eti);
                    startActivity(menu);
                } catch (Exception e) {
                    getFunciones().mostrarMensaje(e.getMessage());
                }
            }
        });
    }
    private void inizializar(){
        aceptar=findViewById(R.id.aceptarARE);
        usuarios=findViewById(R.id.usuarioARE);
        cambio=findViewById(R.id.cambioARE);
        reparacion=findViewById(R.id.reparacionARE);
        etiqueta=findViewById(R.id.etiquetaARE);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        onBackPressed();
    }
}