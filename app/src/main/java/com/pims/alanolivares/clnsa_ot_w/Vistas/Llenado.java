package com.pims.alanolivares.clnsa_ot_w.Vistas;

import android.content.Intent;
import android.os.Bundle;

import android.view.LayoutInflater;
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


public class Llenado extends ClasePadreFragment {
    private TableView<String[]> dataTable;
    private ProgressBar progressBar;
    private JSONArray jsonArray;
    private String[] spaceProbeHeaders={"Tanque","Alcohol","Año","IdLote"};
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_llenado, container, false);
        dataTable=view.findViewById(R.id.llenado_table);
        progressBar=view.findViewById(R.id.progressBarLlenado);
        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(getContext(), 4, 100);
        columnModel.setColumnWidth(1,160);
        dataTable.setColumnModel(columnModel);
        setTabla(dataTable,spaceProbeHeaders);
        setTitle("Órdenes de Llenado");
        return view;
    }

    @Override
    public void proceso(int index, View vista, BottomSheetDialog bottomSheet) {
        super.proceso(index, vista, bottomSheet);
        NoScrollViewTable descripcion=vista.findViewById(R.id.detales_table);
        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(getContext(), 3, 130);
        descripcion.setColumnModel(columnModel);
        ProgressBar progress=vista.findViewById(R.id.progressDR);
        TextView titulo=vista.findViewById(R.id.descripcion_titulo);
        progress.post(new Runnable() {
            @Override
            public void run(){
                try {
                    JSONObject jsonObject=jsonArray.getJSONObject(index);
                    JSONArray datos=getFunciones().consultaJson("exec sp_OrdenLlen_Pistola_Detalle '"+jsonObject.getString("idrecepcion")+"','"+jsonObject.getString("idrecdetail")+"'", SQLConnection.db_AAB);
                    titulo.setText("Tanque: "+jsonObject.getString("tanque"));
                    Button continuar = vista.findViewById(R.id.continuar);
                    String[] head = {"Litros", "Existencia", "Consumo"};
                    String[][] spaceProbes= new String[datos.length()][3];
                    for (int x=0;x<datos.length();x++){
                        JSONObject jsonObject2=datos.getJSONObject(x);
                        spaceProbes[x][0]=getFunciones().formatNumber(jsonObject2.getString("litros")) ;
                        spaceProbes[x][1]=getFunciones().formatNumber(jsonObject2.getString("existencia"));
                        spaceProbes[x][2]=getFunciones().formatNumber(jsonObject2.getString("consumo"));
                    }
                    llenarTabla(head,spaceProbes,descripcion);
                    final String IdLote=jsonObject.getString("idlote");
                    final String tanque=jsonObject.getString("tanque");
                    continuar.setEnabled(true);
                    continuar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent menu=new Intent(getContext(),MenuLlenado.class);
                            menu.putExtra("IdLote",IdLote);
                            menu.putExtra("tanque",tanque);
                            startActivity(menu);
                            bottomSheet.dismiss();
                        }
                    });

                }catch (Exception e){
                    getFunciones().mostrarMensaje(e.getMessage());
                    bottomSheet.dismiss();
                }finally {
                    progress.setVisibility(View.GONE);
                }

            }
        });
    }

    @Override
    public void onResume() {
        cargarDatos();
        super.onResume();
    }

    @Override
    public void cargarDatos(){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    jsonArray=getFunciones().consultaJson("select T.Codigo as Tanque,A.Descripcion as Alcohol,I.Año as annio,RD.IdLote,RD.IdRecepcion,RD.IdRecDetail " +
                            "from WM_RecDetail RD Left Join CM_Tanque T on T.IDTanque = RD.IdTanque left Join CM_Item I on I.IdItem = RD.IdItem " +
                            "Left Join CM_Alcohol A on A.IdAlcohol = RD.IdAlcohol Where RD.Estatus = 0", SQLConnection.db_AAB);
                    String[][] spaceProbes= new String[jsonArray.length()][4];
                    for (int x=0;x<jsonArray.length();x++){
                        JSONObject jsonObject=jsonArray.getJSONObject(x);
                        spaceProbes[x][0]=jsonObject.getString("tanque");
                        spaceProbes[x][1]=jsonObject.getString("alcohol");
                        spaceProbes[x][2]=jsonObject.getString("annio");
                        spaceProbes[x][3]=jsonObject.getString("idlote");
                    }
                    dataTable.setDataAdapter(new SimpleTableDataAdapter(getContext(), spaceProbes));
                } catch (Exception e) {
                    getFunciones().mostrarMensaje(e.getMessage());
                }finally {
                    progressBar.setVisibility(View.GONE);
                }
            }
        },1000);

    }
}
