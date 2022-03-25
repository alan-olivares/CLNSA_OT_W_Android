package com.pims.alanolivares.clnsa_ot_w.Vistas;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
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
import de.codecrafters.tableview.listeners.TableDataLongClickListener;
import de.codecrafters.tableview.model.TableColumnDpWidthModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;

public class MontRealizadas extends ClasePadreFragment {
    TableView<String[]> dataTable;
    int idBodega;
    boolean montrando=false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_mont_realizadas, container, false);
        dataTable=view.findViewById(R.id.monta_table_rea);
        String[] spaceProbeHeaders={"Pallet","Reubicación"};

        setTitle("0 órdenes de montacargas");
        idBodega=Integer.valueOf(getFunciones().getDatoCache("bodegaMan"));
        idBodega=idBodega==-1?21:idBodega;
        setTabla(dataTable,spaceProbeHeaders);
        setHasOptionsMenu(false);
        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(getContext(), 2, 100);
        columnModel.setColumnWidth(1,350);
        dataTable.setColumnModel(columnModel);
        return view;
    }

    @Override
    public void proceso(int index, View vista, BottomSheetDialog bottomSheet) {
        if(montrando)
            return;
        super.proceso(index, vista, bottomSheet);
        ProgressBar progress=vista.findViewById(R.id.progressDR);
        try{
            String pallet=jsonArray.getJSONObject(index).getString("Pallet");
            JSONArray jsonA=getFunciones().getJsonLocal("select '0101' || substr('000000'|| b.consecutivo,-6) as Etiqueta,C.Codigo,case b.estado when 2 then 'Vacio' else 'Lleno' end Estado,A.Descripcion " +
                    " from wm_barril b inner join CM_Codificacion C on b.tipo=C.IdCodificacion " +
                    "inner join CM_Alcohol A on b.alcohol=A.IdAlcohol where b.idpallet="+pallet);
            TextView titulo=vista.findViewById(R.id.descripcion_titulo);
            titulo.setText("Pallet: "+jsonArray.getJSONObject(index).getString("Pallet"));
            Button eliminar = vista.findViewById(R.id.continuar);
            NoScrollViewTable descripcion=vista.findViewById(R.id.detales_table);
            TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(getContext(), 4, 80);
            columnModel.setColumnWidth(0,160);
            columnModel.setColumnWidth(3,150);
            descripcion.setColumnModel(columnModel);
            String[] head = {"Etiqueta","Uso", "Estado","Alcohol"};
            String [][] datos=getFunciones().consultaTabla(jsonA,4);
            llenarTabla(head,datos,descripcion);
            eliminar.setText("Eliminar");
            eliminar.setEnabled(true);
            eliminar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    eliminaRegistro(pallet);
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
            jsonArray=getFunciones().getJsonLocal("select B.idpallet as Pallet," +
                    "A.Nombre || ';Cos:' ||substr(AR.Nombre,8) ||';F:'||substr(S.Nombre,5) || ';T:' ||  substr(P.Nombre,6) ||';N:' || substr(N.Nombre,6) as ubicacion " +
                    " from PR_NvUbicacion U join WM_RackLoc R on R.RackLocID=U.RacklocFin inner join wm_barril B on '0101' || substr('000000'|| b.consecutivo,-6)=U.Consecutivo " +
                    "join aa_nivel N on R.NivelID=N.nivelId join aa_posicion P ON N.PosicionId=P.posicionId join aa_seccion S ON P.seccionid=S.seccionid join aa_area AR ON S.AreaId=AR.areaid join aa_almacen A ON AR.almacenid=A.almacenid ");
            //jsonArray=getFunciones().getJsonLocal("select b.idpallet, from PR_NvUbicacion U inner join wm_barril b on '0101' || substr('000000'|| b.consecutivo,-6)=U.Consecutivo order by u.idpallet");
            String barriles[][]=getFunciones().consultaTabla(jsonArray,2);
            dataTable.setDataAdapter(new SimpleTableDataAdapter(getContext(), barriles));
            setTitle(jsonArray.length()+" pallets listas");
            dataTable.addDataLongClickListener(new TableDataLongClickListener<String[]>() {
                @Override
                public boolean onDataLongClicked(int rowIndex, String[] clickedData) {
                    if(!montrando)
                        eliminaRegistro(clickedData[0]);
                    return false;
                }
            });
        }catch (Exception e){
            getFunciones().mostrarMensaje(e.getMessage());
        }
    }




    private void eliminaRegistro(String pallet){
        montrando=true;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog.dismiss();
                        getFunciones().ejecutarComLocal("delete from PR_NvUbicacion " +
                                "where Consecutivo in (select '0101' || substr('000000'|| consecutivo,-6) from wm_barril where idpallet='"+pallet+"')");
                        cargarDatos();
                        montrando=false;
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        montrando=false;
                        break;

                }
            }
        };
        builder.setMessage("¿Quieres eliminar el registro con el pallet "+pallet+"?")
                .setPositiveButton("Si", dialogClickListener).setCancelable(false)
                .setNegativeButton("No", dialogClickListener).show();
    }
}