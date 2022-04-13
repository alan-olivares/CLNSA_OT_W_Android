package com.pims.alanolivares.clnsa_ot_w.Vistas;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadre;
import com.pims.alanolivares.clnsa_ot_w.Funciones.NoScrollViewTable;
import com.pims.alanolivares.clnsa_ot_w.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import de.codecrafters.tableview.model.TableColumnDpWidthModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class TrasiegoHooverInicia extends ClasePadre {
    private NoScrollViewTable dataTable;
    private TextView tanqueO,tanqueD,litrosT,litrosD;
    private Button bombear;
    private String idOrden,IdTanque,NoSerie;
    private double Cantidad;
    private boolean terminar=false;
    private int totalBarriles=0;
    private ProgressBar progressBar;
    private Timer timer;
    private EditText lectura;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trasiego_hoover_inicia);
        idOrden=getIntent().getStringExtra("IdOrden");
        setTitulo("Orden: "+idOrden);
        inicializar();
        cargarDatos();
    }


    private void inicializar(){
        tanqueO=findViewById(R.id.tanqueO);
        tanqueD=findViewById(R.id.tanqueD);
        litrosT=findViewById(R.id.litrosT);
        litrosD=findViewById(R.id.litrosD);
        bombear=findViewById(R.id.iniciarBomTL);
        dataTable=findViewById(R.id.tablaTHI);
        progressBar=findViewById(R.id.progressIT);
        lectura=findViewById(R.id.lecturaTLD);
        String[] spaceProbeHeaders={"Barril","Alcohol","Año","Litros","Tipo"};
        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(this, 5, 120);
        columnModel.setColumnWidth(0,150);
        columnModel.setColumnWidth(1,150);
        dataTable.setColumnModel(columnModel);
        dataTable.setHeaderAdapter(new SimpleTableHeaderAdapter(this,spaceProbeHeaders));
        setCopyEtiqueta(dataTable,0);
        bombear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iniciarBombeo();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(terminar) {
            try {
                getFunciones().insertaData("update PR_Orden set Estatus=3 where IdOrden="+idOrden,SQLConnection.db_AAB);
            } catch (Exception e) {
                getFunciones().mostrarMensaje("..Error.. No se pudo finalizar la orden "+e.getMessage());
            }
        }
        if(timer!=null)
            timer.cancel();
        super.onBackPressed();
    }

    private void cargarDatos(){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                String consulta="select T.IdTanque,T.NoSerie,T.Litros,CONVERT(varchar(10),T.FechaLLenado,120) as fecha,D.Cantidad,CT.Codigo  " +
                        "from WM_Tanques T inner join PR_Orden O on O.IdLote=T.IdTanque inner join PR_op D on O.IdOrden=D.IdOrden " +
                        "inner join CM_Tanque CT on D.IdTanque=CT.IDTanque where O.IdOrden="+idOrden;
                try {
                    JSONArray jsonArray=getFunciones().consultaJson(consulta, SQLConnection.db_AAB);
                    if(jsonArray.length()>0){
                        JSONObject jsonObject=jsonArray.getJSONObject(0);
                        NoSerie=jsonObject.getString("noserie");
                        IdTanque=jsonObject.getString("idtanque");
                        Cantidad=jsonObject.getDouble("cantidad");
                        tanqueO.setText("Tanque origen: "+NoSerie);
                        tanqueD.setText("Tanque destino: "+jsonObject.getString("codigo"));
                        litrosT.setText("Litros a trasegar: "+Cantidad);
                        litrosD.setText("Litros disponibles: "+jsonObject.getString("litros"));
                        cargarBarriles(jsonObject.getString("fecha"),IdTanque);
                    }else{
                        onBackPressed();
                    }

                } catch (Exception e) {
                    getFunciones().mostrarMensaje(e.getMessage());
                }finally {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

    }
    String tabla[][];
    private void cargarBarriles(String fecha,String idTanque){
        String consulta="SELECT isnull((('01' + right('00' + convert(varChar(2),1),2) + right('000000' + convert(varChar(6),OpHis.Consecutivo),6))),'Sin Asignar') as Barril, " +
                " Al.Descripcion as Alcohol,Datepart(YYYY,L.Recepcion) as Año,OpHis.Capacidad,case OpHis.tipoLl when 1 then 'Completo' else 'Parcial' end as Tipo " +
                " from WM_OperacionTQH Op left join WM_OperacionTQHDetalle OpDe on Op.IdOperacion = OpDe.IdOperacion " +
                " left join WM_OperacionTQHBarrilHis OpHis on OpHis.IdOperacion=Op.IdOperacion " +
                " inner Join WM_LoteBarrica LB on LB.IdLoteBarica = OpHis.IdLoteBarrica " +
                " inner Join PR_Lote L on L.Idlote = LB.IdLote inner join PR_Orden O on OpHis.IdOrden=O.IdOrden " +
                " inner Join CM_Alcohol Al on Al.IdAlcohol = L.IdAlcohol  where Op.fecha between '"+fecha+" 00:00' and '"+fecha+" 23:59' and op.IdTanque='"+idTanque+"' order by Op.IdOperacion,OpHis.IdOrden ";
        try {
            tabla=getFunciones().consultaTabla(consulta, SQLConnection.db_AAB,5);
            dataTable.setDataAdapter(new SimpleTableDataAdapter(this, tabla));
            dataTable.setAutoHeight(tabla.length);
            bombear.setEnabled(true);
        } catch (Exception e) {
            getFunciones().mostrarMensaje(e.getMessage());
        }

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
                getFunciones().insertaData("exec sp_RegistroHoover '" +NoSerie+ "','" + IdTanque + "','" + idOrden + "','8','" + lectura.getText().toString() + "'",SQLConnection.db_AAB);
                //getFunciones().insertaData("Update PR_Orden set Estatus=3 where IdOrden="+idOrden,SQLConnection.db_AAB);
                timer.cancel();
                terminar=true;
            }
        } catch (Exception e) {
            getFunciones().mostrarMensaje(e.getMessage());
        }
    }

    private class revisarFlujometros extends TimerTask {

        @Override
        public void run() {
            try {
                obtenerDatosLanzas();
            } catch (Exception e) {
                //getFunciones().mostrarMensaje(e.getMessage());
            }
        }

        private void obtenerDatosLanzas() throws Exception {
            JSONArray jsonArray = getFunciones().consultaJson("select Valor from Com_TagsActual where idTag=3", SQLConnection.db_Emba);
            double lanzaLec = jsonArray.getJSONObject(0).getDouble("valor");
            lectura.post(new Runnable() {
                @Override
                public void run() {
                    lectura.setText(lanzaLec+"");
                    if(Cantidad<=lanzaLec){
                        iniciarBombeo();//Se termina el boombeo
                        bombear.setEnabled(false);
                    }
                }
            });
        }
    }
}