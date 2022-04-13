package com.pims.alanolivares.clnsa_ot_w.Vistas;

import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadre;
import com.pims.alanolivares.clnsa_ot_w.Funciones.NoScrollViewTable;
import com.pims.alanolivares.clnsa_ot_w.R;

import org.json.JSONArray;

import java.util.Timer;
import java.util.TimerTask;

import de.codecrafters.tableview.model.TableColumnDpWidthModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class TrasLoteDest extends ClasePadre {
    private Button bombear;
    private EditText lectura;
    private NoScrollViewTable dataTable;
    private String idTanque,idOrden,idregtanque,lanzaLec,tipo;
    private Timer timer;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tras_lote_dest);
        idOrden=getIntent().getStringExtra("IdOrden");
        idTanque=getIntent().getStringExtra("tanqueId");
        tipo=getIntent().getStringExtra("tipo");
        setTitulo("Orden: "+idOrden);
        inicializar();
        cargarDetalles();
        bombear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iniciarBombeo();
            }
        });
    }
    private void inicializar(){
        bombear=findViewById(R.id.iniciarBomTL);
        lectura=findViewById(R.id.lecturaTLD);
        dataTable=findViewById(R.id.tablaTLD);
        progressBar=findViewById(R.id.progressTLD);
    }
    private void iniciarBombeo(){
        try {
            getFunciones().insertaData("update Com_TagsActual set Valor = 0 where IdTag =3",SQLConnection.db_Emba);
            if(bombear.getText().toString().toLowerCase().equals("iniciar bombeo")){
                getFunciones().insertaData("update Com_TagsActual set Valor = 1 where IdTag = 1",SQLConnection.db_Emba);
                bombear.setBackgroundColor(ContextCompat.getColor(this,R.color.rojo_danger) );
                bombear.setText("Detener bombeo");
                timer = new Timer();
                timer.schedule(new revisarFlujometros(), 2000, 1000);
            }else{
                bombear.setText("Iniciar bombeo");
                bombear.setBackgroundColor(ContextCompat.getColor(this,R.color.verde_claro) );
                getFunciones().insertaData("update Com_TagsActual set Valor = 0 where IdTag = 1",SQLConnection.db_Emba);
                getFunciones().insertaData("Update PR_Orden set Estatus=3 where IdOrden="+idOrden,SQLConnection.db_AAB);
                if(tipo.equals("1"))
                    getFunciones().insertaData("exec sp_RegTanqueLectura '"+idregtanque+"','"+lanzaLec+"'",SQLConnection.db_AAB);
                timer.cancel();
            }
        } catch (Exception e) {
            getFunciones().mostrarMensaje(e.getMessage());
        }
    }
    private void cargarDetalles(){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray jsonArray;
                    String tabla[][];
                    String[] spaceProbeHeaders;
                    TableColumnDpWidthModel columnModel;
                    if(tipo.equals("1")){
                        jsonArray=getFunciones().consultaJson("select isnull((select top 1 IdRegTanque from PR_RegTanque where IdOrden="+idOrden+"),-1) as IdRegTanque",SQLConnection.db_AAB);
                        idregtanque=jsonArray.getJSONObject(0).getString("idregtanque");
                        tabla=getFunciones().consultaTabla("exec sp_RegTanqueOrden '"+idregtanque+"'", SQLConnection.db_AAB,9);
                        spaceProbeHeaders= new String[]{"Cant", "Fecha", "Alcohol", "Uso", "Tanque", "Bodega", "Costado", "Fila", "IdReg"};
                        columnModel = new TableColumnDpWidthModel(TrasLoteDest.this, 9, 110);
                        //columnModel.setColumnWidth(0,150);
                    }else{
                        tabla=getFunciones().consultaTabla("select top 1 Op.Cantidad as 'B. a Vacíar',count(TB.IdBarrica) as 'B. Vacíados' " +
                                ",OP.Fecha as Fecha_LL,AL.Descripcion as Alcohol,C.Codigo as Uso,T.NoSerie as Tanque from WM_OperacionTQHBarrilHis TB  " +
                                "inner Join PR_Orden O on O.IdOrden = TB.IdOrden inner Join PR_Op OP on OP.IdOrden = O.IdOrden  " +
                                "Inner Join CM_Alcohol AL on Al.IdAlcohol = OP.IdAlcohol inner Join CM_Codificacion C on C.IdCodificacion = OP.IdCodificacion " +
                                "inner Join WM_Tanques T on T.IDTanque = OP.IdTanque inner Join AA_Almacen AA On AA.AlmacenID = O.Idalmacen " +
                                "inner Join AA_Area A on A.AlmacenId = AA.AlmacenID and A.AreaId = OP.AreaID inner Join AA_Seccion S on S.AreaId = A.AreaId and S.SeccionID = OP.SeccionID " +
                                "where O.IdOrden=" +idOrden+ " group by Op.Cantidad,OP.Fecha,C.Codigo,T.NoSerie,AA.Nombre,SUBSTRING (A.Nombre,9,5),S.Nombre, TB.IdOrden,AL.Descripcion", SQLConnection.db_AAB,6);
                        dataTable.setColumnCount(6);
                        spaceProbeHeaders= new String[]{"B. a Vacíar", "B. Vacíados", "Fecha", "Alcohol", "Uso", "Tanque"};
                        columnModel = new TableColumnDpWidthModel(TrasLoteDest.this, 6, 90);
                        //columnModel.setColumnWidth(0,150);
                    }
                    dataTable.setAutoHeight(tabla.length);
                    bombear.setEnabled(tabla.length>0);
                    dataTable.setHeaderAdapter(new SimpleTableHeaderAdapter(TrasLoteDest.this,spaceProbeHeaders));
                    dataTable.setDataAdapter(new SimpleTableDataAdapter(TrasLoteDest.this, tabla));
                    dataTable.setColumnModel(columnModel);
                } catch (Exception e) {
                    getFunciones().mostrarMensaje(e.getMessage());
                }finally {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });


    }
    @Override
    public void onBackPressed() {
        if(timer!=null)
            timer.cancel();
        super.onBackPressed();
    }
    private class revisarFlujometros extends TimerTask {

        @Override
        public void run() {
            try {
                obtenerDatosLanzas();
            }catch (Exception e){
                //getFunciones().mostrarMensaje(e.getMessage());
            }
        }

        private void obtenerDatosLanzas() throws Exception{
            JSONArray jsonArray=getFunciones().consultaJson("select Valor from Com_TagsActual where idTag=3",SQLConnection.db_Emba);
            lanzaLec=jsonArray.getJSONObject(0).getString("valor");
            lectura.post(new Runnable() {
                @Override
                public void run() {
                    lectura.setText(lanzaLec);
                }
            });
            //revisarAvance();
        }
    }
}