package com.pims.alanolivares.clnsa_ot_w.Vistas;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadreFragment;
import com.pims.alanolivares.clnsa_ot_w.Funciones.SpinnerObjeto;
import com.pims.alanolivares.clnsa_ot_w.R;

import java.util.ArrayList;

public class Configuracion extends ClasePadreFragment {
    private Button guardar;
    private Spinner bodegas;
    private EditText pistola,nombre;
    private ArrayList<SpinnerObjeto> lista;
    private TextView nombreT,pistolaT;
    public Configuracion(TextView nombreT,TextView pistolaT){
        this.nombreT=nombreT;
        this.pistolaT=pistolaT;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void cargarDatos() {
        lista= getFunciones().llenarSpinner("SELECT AlmacenID as id, Nombre as valor FROM AA_almacen ORDER BY Consecutivo",bodegas);
        pistola.setText(getFunciones().getPistola());
        nombre.setText(getFunciones().getDatoCache("nombre"));
        int bod=Integer.valueOf(getFunciones().getDatoCache("bodegaMan"));
        bodegas.setSelection(getBodega(bod));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_configuracion, container, false);
        setTitle("Configuración");
        inicializar(view);
        cargarDatos();
        return view;
    }
    private void inicializar(View view){
        guardar=view.findViewById(R.id.guardarC);
        bodegas=view.findViewById(R.id.bodegasC);
        pistola=view.findViewById(R.id.pistolaC);
        nombre=view.findViewById(R.id.nombreC);
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(correctSave()){
                    String name=nombre.getText().toString();
                    SpinnerObjeto bod=(SpinnerObjeto) bodegas.getSelectedItem();
                    try {
                        getFunciones().insertaData("update CM_Usuario_WEB set Nombre='"+name+"' where IdUsuario='"+getFunciones().getIdUsuario()+"'", SQLConnection.db_AAB);
                        getFunciones().saveDatoCache("nombre",name);
                        getFunciones().saveDatoCache("bodegaMan", String.valueOf(bod.getId()));
                        getFunciones().saveDatoCache("pistola",pistola.getText().toString());
                        nombreT.setText("Usuario: "+name);
                        pistolaT.setText("Pistola #"+pistola.getText().toString());
                        getFunciones().mostrarMensaje("Datos guardados correctamente");
                    } catch (Exception e) {
                        getFunciones().mostrarMensaje(e.getMessage());
                    }

                }else{
                    getFunciones().mostrarMensaje("..Error.. Datos incorrectos, verificalos e intentalo de nuevo");
                }
            }
        });
    }

    private int getBodega(int id){
        int wip=0;
        for (int x=0;x<lista.size();x++){
            if(lista.get(x).getId()==id)
                return x;
            if(lista.get(x).getId()==21)
                wip=x;
        }
        return wip;
    }
    private boolean correctSave(){
        try {
            SpinnerObjeto bodega=(SpinnerObjeto) bodegas.getSelectedItem();
            Integer.valueOf(pistola.getText().toString());
            return !pistola.getText().toString().isEmpty() && !nombre.getText().toString().isEmpty() && bodega.getValor()!=null;
        }catch (Exception e){
            return false;
        }

    }

}