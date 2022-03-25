package com.pims.alanolivares.clnsa_ot_w.Vistas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadre;
import com.pims.alanolivares.clnsa_ot_w.Funciones.SpinnerObjeto;
import com.pims.alanolivares.clnsa_ot_w.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class ReubiTarima extends ClasePadre {
    Spinner planta,bodega,costado,fila,torre,nivel;
    Button reubicar,camara;
    TextView mensaje,aviso;
    EditText etiqueta;
    ArrayList<SpinnerObjeto> listaNiveles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reubi_tarima);
        inicializar();
        setCamara(camara);
        setEtiqueta(etiqueta);
        String eti =getIntent().getStringExtra("etiqueta");
        etiqueta.setText(eti);
    }
    private void inicializar(){
        bodega=findViewById(R.id.bodegaRT);
        costado=findViewById(R.id.costadoRT);
        fila=findViewById(R.id.filaRT);
        torre=findViewById(R.id.torreRT);
        nivel=findViewById(R.id.nivelRT);
        planta=findViewById(R.id.plantaRT);
        reubicar=findViewById(R.id.asignarRT);
        mensaje=findViewById(R.id.mensajeRT);
        camara=findViewById(R.id.camaraRT);
        etiqueta=findViewById(R.id.etiquetaRT);
        aviso=findViewById(R.id.avisoRT);
        getFunciones().llenarSpinnerLocal("select -1 as id,'' as valor union select PlantaID,Nombre from AA_Plantas",planta);
        planta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                vaciarValor(1);
                SpinnerObjeto planta=(SpinnerObjeto)adapterView.getSelectedItem();
                mensaje.setText("");
                if(planta.getId()!=-1){
                    getFunciones().llenarSpinnerLocal("select -1 as id,'' as valor,-1 as conse union select AlmacenID,Nombre,Consecutivo from AA_Almacen where PlantaID="+planta.getId()+" order by conse",bodega);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        bodega.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                vaciarValor(2);
                SpinnerObjeto bod=(SpinnerObjeto)adapterView.getSelectedItem();
                if(bod.getId()!=-1){
                    JSONArray json=getFunciones().getJsonLocal("select strftime('%d/%m/%Y %H:%M', fecha) as fecha from cm_SyncBodega where idbodega="+bod.getId());
                    try {
                        if(json.length()>0){
                            mensaje.setText("Última actualización: "+json.getJSONObject(0).getString("fecha"));
                            mensaje.setTextColor(ContextCompat.getColor(ReubiTarima.this,R.color.black));
                            getFunciones().llenarSpinnerLocal("select -1 as id,'' as valor,-1 as conse union select AreaId, Nombre,Consecutivo from AA_Area A WHERE AlmacenId="+bod.getId()+" order by Consecutivo",costado);
                        }else{
                            mensaje.setText("Bodega no sincronizada");
                            mensaje.setTextColor(ContextCompat.getColor(ReubiTarima.this,R.color.rojo_danger));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    mensaje.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        costado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                vaciarValor(3);
                SpinnerObjeto cos=(SpinnerObjeto)adapterView.getSelectedItem();
                if(cos.getId()!=-1){
                    getFunciones().llenarSpinnerLocal("select -1 as id,'' as valor,-1 as conse union SELECT SeccionID,Nombre,Consecutivo FROM AA_Seccion WHERE AreaId="+cos.getId()+" ORDER BY  Consecutivo",fila);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        fila.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                vaciarValor(4);
                SpinnerObjeto fil=(SpinnerObjeto)adapterView.getSelectedItem();
                if(fil.getId()!=-1){
                    getFunciones().llenarSpinnerLocal("select -1 as id,'' as valor,-1 as conse union SELECT PosicionId,Nombre,Consecutivo FROM AA_Posicion WHERE SeccionID="+fil.getId()+" ORDER BY  Consecutivo",torre);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        torre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                vaciarValor(5);
                SpinnerObjeto torr=(SpinnerObjeto)adapterView.getSelectedItem();
                if(torr.getId()!=-1){
                    listaNiveles=getFunciones().llenarSpinnerLocal("select -1 as id,'' as valor,-1 as conse union SELECT NivelId,Nombre,Consecutivo FROM AA_Nivel WHERE PosicionId="+torr.getId()+" ORDER BY  Consecutivo ",nivel);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        nivel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                vaciarValor(6);
                SpinnerObjeto niv=(SpinnerObjeto)adapterView.getSelectedItem();
                reubicar.setEnabled(niv.getId()!=-1 && verificaNivel(niv.getId(),i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        reubicar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertaEtiqueta(etiqueta.getText().toString());
            }
        });
    }

    @Override
    public void validaEtiqueta(String eti){
        try {
            super.validaEtiqueta(eti);
            if(solicitudPendiente()){
                aviso.setText("El pallet donde se encuentra la etiqueta "+etiqueta.getText().toString()+" ya fue posicionada en otro lugar");
                return;
            }
            if(reubicar.isEnabled()){
                SpinnerObjeto niv=(SpinnerObjeto)nivel.getSelectedItem();
                String rackLoc=getRack(niv.getId());
                getFunciones().ejecutarComLocal("Insert into PR_NvUbicacion(Consecutivo,RacklocFin,Tipo) Values(" +eti+ "," +rackLoc+ ",1)");
                getFunciones().mostrarMensaje("Ubicación guardada con exito");
                etiqueta.setText("");
                vaciarValor(1);
                planta.setSelection(0);
            }else{
                getFunciones().makeErrorSound();
                getFunciones().mostrarMensaje("Debes seleccionar un nivel valido primero");
            }

        } catch (Exception e) {
            getFunciones().mostrarMensaje(e.getMessage());
        }
    }

    private boolean verificaNivel(int nivelID, int pos){
        try{
            if(tieneSolicitud(nivelID)){
                aviso.setText("Ya has registrado una solicitud dentro de este nivel");
                return false;
            }
            int barriles=obtenerBarrilesNiv(nivelID);
            if(barriles>0){
                aviso.setText("Este nivel ya cuenta con "+barriles+" barriles asignados, y no se puede asignar otro pallet");
                return false;
            }

            if(pos==1)//Es el primer nivel y no se necesita verificar si hay barriles debajo de él
                return true;
            else{//Verificamos que existan barriles debajo de él
                if(tieneSolicitud(listaNiveles.get(pos-1).getId()))//Existe una solicitud en el nivel debajo, por lo tanto se puede agregar uno arriba
                    return true;
                int barrilesAnt=obtenerBarrilesNiv(listaNiveles.get(pos-1).getId());
                aviso.setText(barrilesAnt==0?"El nivel anterior no tiene barriles asignados, por lo tanto no se puede asignar un pallet a este nivel":"");
                return barrilesAnt!=0;
            }
        }catch (Exception e){
            aviso.setText(e.getMessage());
            return false;
        }
    }
    private boolean solicitudPendiente() throws Exception{
        int pallet=getFunciones().getJsonLocal("select idpallet from wm_barril where consecutivo="+getFunciones().etiToConse(etiqueta.getText().toString())).getJSONObject(0).getInt("idpallet");
        System.out.println(pallet);
        JSONArray nivel=getFunciones().getJsonLocal("select count(b.consecutivo) as barriles from PR_NvUbicacion U inner join wm_barril b on U.Consecutivo='0101' || substr('000000'|| b.consecutivo,-6) where b.IdPallet="+pallet);
        return nivel.getJSONObject(0).getInt("barriles")!=0;
    }
    private int obtenerBarrilesNiv(int nivelID) throws Exception{
        JSONArray nivel=getFunciones().getJsonLocal("select count(consecutivo) as barriles from wm_barril where Nivel="+nivelID);
        return nivel.getJSONObject(0).getInt("barriles");
    }
    private String getRack(int nivelID) throws Exception{
        JSONArray json=getFunciones().getJsonLocal("Select RackLocID from WM_RackLoc Where NivelId = "+nivelID+" order by RackLocId desc");
        return json.getJSONObject(0).getString("RackLocID");
    }
    private boolean tieneSolicitud(int nivelID) throws Exception{
        String rackLoc=getRack(nivelID);
        JSONArray json=getFunciones().getJsonLocal("select count(RacklocFin) as racks from PR_NvUbicacion where RacklocFin="+rackLoc);
        return json.getJSONObject(0).getInt("racks")!=0;
    }

    //Metodo extraido de la versión original y mejorado en verificaNivel()
    private void reubicarPalet(String eti) throws Exception{
        SpinnerObjeto niv=(SpinnerObjeto)nivel.getSelectedItem();
        JSONArray json=getFunciones().getJsonLocal("select PosicionId,Consecutivo from aa_nivel where nivelid="+niv.getId());
        if(json.length()>0){
            String nivelAnt="",result="",rackLoc="";
            int barriles=0,ocupado=0;
            int selectedCons=json.getJSONObject(0).getInt("Consecutivo");
            int selectedPos=json.getJSONObject(0).getInt("PosicionId");
            json=getFunciones().getJsonLocal("select Consecutivo from aa_nivel where posicionid="+selectedPos+" order by consecutivo");
            int primerCons=json.getJSONObject(0).getInt("Consecutivo");
            if(selectedCons>primerCons){
                json=getFunciones().getJsonLocal("select NivelID from aa_nivel where posicionid="+selectedPos+" and consecutivo< "+selectedCons+" order by consecutivo desc");
                nivelAnt=json.getJSONObject(0).getString("NivelID");
                json=getFunciones().getJsonLocal("select count(B.idbarrica) as barriles from wm_pallet P join WM_barrica B on P.idpallet=B.idpallet join wm_Rackloc R on P.racklocid=R.racklocid where R.nivelid="+nivelAnt);
                barriles=json.getJSONObject(0).getInt("barriles");
                if(barriles>0){
                    result="0";
                }else{
                    json=getFunciones().getJsonLocal("select count(NU.racklocfin) as cont from PR_NvUbicacion NU join WM_rackloc R on NU.RacklocFin=R.racklocid where R.nivelid="+nivelAnt);
                    ocupado=json.getJSONObject(0).getInt("cont");
                    result=ocupado>0?"0":"1";
                }
            }else{
                result=(selectedCons==primerCons)?"2":"3";
            }
            if(result.equals("2")){
                getFunciones().mostrarMensaje("Error en el consecutivo del nivel, verifique la estructura de la bodega");
            }else if(result.equals("3")){
                getFunciones().mostrarMensaje("No se puede usar este nivel porque no hay una paleta nivelada abajo");
            }else{
                json=getFunciones().getJsonLocal("select count(B.idbarrica) as barriles from wm_pallet P join WM_barrica B on P.idpallet=B.idpallet join wm_Rackloc R on P.racklocid=R.racklocid where R.nivelid="+niv.getId());
                barriles=json.getJSONObject(0).getInt("barriles");
                if(barriles>0){
                    getFunciones().mostrarMensaje("No se puede nivelar en esta ubicación, este nivel se encuentra ocupado");
                }else{
                    json=getFunciones().getJsonLocal("Select RackLocID from WM_RackLoc Where NivelId = "+niv.getId()+" order by RackLocId desc");
                    rackLoc=json.getJSONObject(0).getString("RackLocID");
                    json=getFunciones().getJsonLocal("select count(RacklocFin) as racks from PR_NvUbicacion where RacklocFin="+rackLoc);
                    ocupado=json.getJSONObject(0).getInt("racks");
                    if(ocupado>0){
                        getFunciones().mostrarMensaje("No se puede nivelar en esta ubicación, este nivel se encuentra ocupado");
                    }else{
                        getFunciones().ejecutarComLocal("Insert into PR_NvUbicacion(Consecutivo,RacklocFin,Tipo) Values(" +eti+ "," +rackLoc+ ",1)");
                        getFunciones().mostrarMensaje("Ubicación guardada con exito");
                        etiqueta.setText("");
                    }
                }
            }


        }

    }
    private void vaciarValor(int caso){
        switch (caso){
            case 1://Plantas
                bodega.setAdapter(null);
            case 2://Bodegas
                costado.setAdapter(null);
            case 3://Costados
                fila.setAdapter(null);
            case 4://Filas
                torre.setAdapter(null);
            case 5://Torres
                nivel.setAdapter(null);
            case 6://Nivel
                reubicar.setEnabled(false);
                aviso.setText("");
        }
    }
}