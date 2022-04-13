package com.pims.alanolivares.clnsa_ot_w.Vistas;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadreFragment;
import com.pims.alanolivares.clnsa_ot_w.Funciones.NoScrollViewTable;
import com.pims.alanolivares.clnsa_ot_w.R;

import org.json.JSONArray;
import org.json.JSONObject;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.model.TableColumnDpWidthModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;

public class MontPendientes extends ClasePadreFragment {
    private TableView<String[]> dataTable;
    private int idBodega;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_mont_pendientes, container, false);
        dataTable=view.findViewById(R.id.monta_table);
        String[] spaceProbeHeaders={"Pallet","Barriles","Litros"};
        setTitle("0 órdenes de montacargas");
        idBodega=Integer.valueOf(getFunciones().getDatoCache("bodegaMan"));
        idBodega=idBodega==-1?21:idBodega;
        setTabla(dataTable,spaceProbeHeaders);
        setHasOptionsMenu(false);
        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(getContext(), 3, 140);
        dataTable.setColumnModel(columnModel);
        return view;
    }

    @Override
    public void proceso(int index, View vista, BottomSheetDialog bottomSheet) {
        super.proceso(index, vista, bottomSheet);
        ProgressBar progress=vista.findViewById(R.id.progressDR);
        try{
            JSONArray jsonA=getFunciones().getJsonLocal("select '0101' || substr('000000'|| b.consecutivo,-6) as Etiqueta,C.Codigo,case b.estado when 2 then 'Vacio' else 'Lleno' end Estado,A.Descripcion " +
                    " from wm_barril b inner join CM_Codificacion C on b.tipo=C.IdCodificacion " +
                    "inner join CM_Alcohol A on b.alcohol=A.IdAlcohol where b.idpallet="+jsonArray.getJSONObject(index).getString("Pallet"));
            TextView titulo=vista.findViewById(R.id.descripcion_titulo);
            titulo.setText("Pallet: "+jsonArray.getJSONObject(index).getString("Pallet"));
            Button continuar = vista.findViewById(R.id.continuar);
            NoScrollViewTable descripcion=vista.findViewById(R.id.detales_table);
            TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(getContext(), 4, 80);
            columnModel.setColumnWidth(0,160);
            columnModel.setColumnWidth(3,150);
            descripcion.setColumnModel(columnModel);
            String[] head = {"Etiqueta","Uso", "Estado","Alcohol"};
            String [][] datos=getFunciones().consultaTabla(jsonA,4);
            llenarTabla(head,datos,descripcion);
            continuar.setText("  Reubicar  ");
            continuar.setEnabled(true);
            continuar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent menu=new Intent(getContext(),ReubiTarima.class);
                    menu.putExtra("etiqueta",datos[0][0]);
                    startActivity(menu);
                    bottomSheet.dismiss();
                }
            });
        }catch (Exception e){
            getFunciones().mostrarMensaje(e.getMessage());
        }finally {
            progress.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarDatos();
    }

    JSONArray jsonArray;
    @Override
    public void cargarDatos(){
        try{
            jsonArray=getFunciones().getJsonLocal("select idpallet as Pallet,count(consecutivo) as barriles,sum(Litros) as Litros " +
                    "from wm_barril where bodega="+idBodega+" and idpallet not in(select b.idpallet from PR_NvUbicacion U inner join wm_barril b on '0101' || substr('000000'|| b.consecutivo,-6)=U.Consecutivo) group by idpallet");
            String barriles[][]=getFunciones().consultaTabla(jsonArray,3);
            dataTable.setDataAdapter(new SimpleTableDataAdapter(getContext(), barriles));
            setPendientes();
        }catch (Exception e){
            getFunciones().mostrarMensaje(e.getMessage());
        }
    }
    public void setPendientes(){
        if (jsonArray!=null)
            setTitle(jsonArray.length()+" órdenes de montacargas");
    }

}
