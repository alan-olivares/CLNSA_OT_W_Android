package com.pims.alanolivares.clnsa_ot_w.Vistas;

import android.content.Intent;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadreFragment;
import com.pims.alanolivares.clnsa_ot_w.Funciones.SpinnerObjeto;
import com.pims.alanolivares.clnsa_ot_w.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Reparacion extends ClasePadreFragment {
    private Button aceptar,camara,continuar;
    private TextView mensaje,barril;
    private Spinner uso, edad;
    private EditText etiqueta,annio;
    private String idBarrica;
    private ProgressBar progressBar;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_reparacion, container, false);
        inicializar(view);
        setEtiqueta(etiqueta);
        setCamara(camara);
        setTitle("Reparación de barriles");
        setHasOptionsMenu(true);
        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validaEtiqueta(etiqueta.getText().toString());
            }
        });
        continuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continuar();
            }
        });
        return view;
    }
    private void inicializar(View view){
        aceptar=view.findViewById(R.id.aceptarREP);
        camara=view.findViewById(R.id.camaraREP);
        continuar=view.findViewById(R.id.continuarREP);
        mensaje=view.findViewById(R.id.msgREP);
        uso=view.findViewById(R.id.usoREP);
        edad=view.findViewById(R.id.edadREP);
        annio=view.findViewById(R.id.annioREP);
        etiqueta=view.findViewById(R.id.etiquetaREP);
        barril=view.findViewById(R.id.barrilREP);
        progressBar=view.findViewById(R.id.progressRep);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                getFunciones().llenarSpinner("Select IdCodificacion as id,Codigo as valor from CM_COdificacion",uso);
                getFunciones().llenarSpinner("Select idedad as id,codigo as valor from CM_Edad",edad);
                progressBar.setVisibility(View.GONE);
                uso.setEnabled(false);
                edad.setEnabled(false);
                annio.setEnabled(false);
            }
        },500);

    }
    private void continuar(){
        progressBar.setVisibility(View.VISIBLE);
        SpinnerObjeto usoOb=(SpinnerObjeto) uso.getSelectedItem();
        SpinnerObjeto edadOb=(SpinnerObjeto) edad.getSelectedItem();
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                try {
                    getFunciones().insertaData("exec sp_BarrilIdentidadUpdate '"+idBarrica+"','"+usoOb.getId()+"','"+edadOb.getId()+"','"+annio.getText().toString()+"'",SQLConnection.db_AAB);
                    Intent menu=new Intent(getContext(),AsingRepara.class);
                    menu.putExtra("idBarrica",idBarrica);
                    menu.putExtra("etiqueta",etiqueta.getText().toString());
                    startActivity(menu);
                } catch (Exception e) {
                    getFunciones().mostrarMensaje(e.getMessage());
                }finally {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });


    }
    @Override
    public void validaEtiqueta(String eti){
        try {
            super.validaEtiqueta(eti);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONArray jsonArray=getFunciones().consultaJson("exec sp_BarrilIdentidad_v3 '"+eti+"'", SQLConnection.db_AAB);
                        JSONObject jsonObject=jsonArray.getJSONObject(0);
                        barril.setText("Barril: "+eti);
                        mensaje.setText(jsonObject.getString("mensaje"));
                        if(!jsonObject.getString("mensaje").equals("¡Barril no encontrado!")){
                            toggleButtons(false);
                            uso.setSelection(jsonObject.getInt("idcodificacion")-1);
                            edad.setSelection(jsonObject.getInt("idedad")-1);
                            annio.setText(jsonObject.getString("año"));
                            idBarrica=jsonObject.getString("idbarrica");
                        }else{
                            getFunciones().makeErrorSound();
                        }
                    } catch (Exception e) {
                        getFunciones().mostrarMensaje(e.getMessage());
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        } catch (Exception e) {
            getFunciones().mostrarMensaje(e.getMessage());
        }
    }
    private void toggleButtons(boolean act){
        aceptar.setEnabled(act);
        continuar.setEnabled(!act);
        camara.setEnabled(act);
        etiqueta.setEnabled(act);
        if (act){
            uso.setSelection(0);
            edad.setSelection(0);
            annio.setText("");
            etiqueta.setText("");
            mensaje.setText("");
            barril.setText("Barril: ");
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_rep, menu);
        final MenuItem actualizar=menu.findItem(R.id.actualizar);
        actualizar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                toggleButtons(true);
                etiqueta.requestFocus();
                return false;
            }
        });
        final MenuItem avance=menu.findItem(R.id.avance);
        avance.setVisible(true);
        avance.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent menu=new Intent(getContext(), ReparacionAvance.class);
                startActivity(menu);
                return false;
            }
        });
    }

    @Override
    public void cargarDatos() {

    }

}