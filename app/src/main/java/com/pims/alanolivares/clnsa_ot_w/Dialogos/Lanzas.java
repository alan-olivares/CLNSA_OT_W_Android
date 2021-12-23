package com.pims.alanolivares.clnsa_ot_w.Dialogos;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ActionBar;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Funciones.FuncionesGenerales;
import com.pims.alanolivares.clnsa_ot_w.R;
import org.json.JSONArray;
import org.json.JSONObject;

public class Lanzas extends AppCompatActivity {
    Button guardar;
    Switch lanza1,lanza2,lanza3;
    ImageButton cerrar;
    FuncionesGenerales func;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lanzas);
        getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT,800);
        this.setFinishOnTouchOutside(false);
        try {
            inicilizar();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void inicilizar() throws Exception {
        func=new FuncionesGenerales(this);
        lanza1=findViewById(R.id.lanza1Act);
        lanza2=findViewById(R.id.lanza2Act);
        lanza3=findViewById(R.id.lanza3Act);
        guardar=findViewById(R.id.guardarAct);
        cerrar=findViewById(R.id.cerrarAct);
        JSONArray jsonArray=func.consultaJson("select nombre, idlanza, edollenada, seleccion, estado from cm_lanza", SQLConnection.db_AAB);
        for (int x=0;x<jsonArray.length();x++){
            JSONObject jsonObject=jsonArray.getJSONObject(x);
            switch (jsonObject.getString("nombre")){
                case "L1":
                    lanza1.setChecked(jsonObject.getString("edollenada").equals("1"));
                    lanza1.setEnabled(jsonObject.getString("seleccion").equals("0"));
                    break;
                case "L2":
                    lanza2.setChecked(jsonObject.getString("edollenada").equals("1"));
                    lanza2.setEnabled(jsonObject.getString("seleccion").equals("0"));
                    break;
                case "L3":
                    lanza3.setChecked(jsonObject.getString("edollenada").equals("1"));
                    lanza3.setEnabled(jsonObject.getString("seleccion").equals("0"));
                    break;
            }
            //lanzas.addView(createSwitches(jsonObject));
        }

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!lanza1.isChecked() && !lanza2.isChecked() && !lanza3.isChecked()){
                    func.mostrarMensaje("Al menos debe de haber una lanza encendida");
                }else{
                    String ids=(lanza1.isChecked()?"1,":"")+(lanza2.isChecked()?"2,":"")+(lanza3.isChecked()?"3,":"");
                    try {
                        func.insertaData("Update CM_Lanza set estado=0,edollenada=0",SQLConnection.db_AAB);
                        func.insertaData("Update CM_Lanza set estado=1,edollenada=1 where idlanza in ("+ids.substring(0,ids.length()-1)+")",SQLConnection.db_AAB);
                        func.insertaData("insert into ADM_LogOperation(Objeto,Descripcion,Accion,IdUsuario,Scan) values('UPD','Config Lanzas','"+ids.substring(0,ids.length()-1)+"'," +func.getIdUsuario()+"," +func.getPistola()+ ")",SQLConnection.db_AAB);
                        onBackPressed();
                        finish();
                    } catch (Exception e) {
                        func.mostrarMensaje(e.getMessage());
                    }
                }
            }
        });
        cerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });
    }
}