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

public class IdentTrasiegoParcial extends ClasePadre {
    private TextView tanque,total;
    private EditText etiqueta,lectura;
    private Button camara,aceptar,bombear;
    private NoScrollViewTable tabla;
    private String idOrden,noSerie,idTanque,opera,barril;
    private ProgressBar progressBar;
    private Timer timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ident_trasiego_parcial);
        inicializar();
    }

    private void inicializar(){
        tanque=findViewById(R.id.tanqueD);
        etiqueta=findViewById(R.id.etiquetaIT);
        camara=findViewById(R.id.camaraIT);
        aceptar=findViewById(R.id.agregarIT);
        lectura=findViewById(R.id.lecturaTLD);
        tabla=findViewById(R.id.tablaIT);
        bombear=findViewById(R.id.iniciarBomTL);
        progressBar=findViewById(R.id.progressIT);
        total=findViewById(R.id.totalIT);
        idOrden=getIntent().getStringExtra("IdOrden");
        setTitulo("Orden: "+idOrden);
        setEtiqueta(etiqueta);
        setCamara(camara);
        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertaEtiqueta(etiqueta.getText().toString());
            }
        });
        try {
            noSerie=getFunciones().consultaJson("select T.NoSerie from PR_Orden O inner join PR_Op D on O.IdOrden=D.IdOrden " +
                    "inner join WM_Tanques T on D.IdTanque=T.IdTanque where O.IdTipoOp=7 and O.IdOrden="+idOrden,SQLConnection.db_AAB).getJSONObject(0).getString("noserie");
            tanque.setText("Tanque destino: "+noSerie);
        } catch (Exception e) {
            getFunciones().mostrarMensaje("..Error.. No se pudo obtener el número de serie del tanque, "+e.getMessage());
            onBackPressed();
        }
        bombear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iniciarBombeo();
            }
        });
        String[] spaceProbeHeaders= new String[]{"Etiqueta", "Edad", "Alcohol", "Capacidad", "Tipo"};
        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(this, 5, 140);
        columnModel.setColumnWidth(1,90);
        columnModel.setColumnWidth(4,120);
        tabla.setHeaderAdapter(new SimpleTableHeaderAdapter(this,spaceProbeHeaders));
        tabla.setColumnModel(columnModel);
        cargarBarriles();
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
                //getFunciones().insertaData("Update PR_Orden set Estatus=3 where IdOrden="+idOrden,SQLConnection.db_AAB);
                timer.cancel();
                insertaRegistros();
                toggleButtons(true);
            }
        } catch (Exception e) {
            getFunciones().mostrarMensaje(e.getMessage());
        }
    }
    private void insertaRegistros(){
        getFunciones().reintentadoRegistro("exec sp_ActualizaRegHoov '"+barril+"','"+opera+"','"+idOrden+"','"+lectura.getText().toString()+"','"+idTanque+"'", SQLConnection.db_AAB, new Runnable() {
            @Override
            public void run() {
                cargarBarriles();
                etiqueta.setText("");
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
                        String consulta="exec sp_ValidaEtiTrasHoov '" + eti + "'," + idOrden + "," + noSerie + ",2";
                        JSONArray jsonArray=getFunciones().consultaJson(consulta, SQLConnection.db_AAB);
                        barril=eti;
                        idTanque=jsonArray.getJSONObject(0).getString("tanque");
                        opera=jsonArray.getJSONObject(0).getString("operacion");
                        toggleButtons(false);
                        //getFunciones().mostrarMensaje(jsonArray.getJSONObject(0).getString("msg"));
                        //cargarBarriles();
                        //etiqueta.setText("");
                    } catch (Exception e) {
                        getFunciones().makeErrorSound();
                        getFunciones().mostrarMensaje("Barril invalido");
                        toggleButtons(true);
                    }finally {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
        } catch (Exception e) {
            getFunciones().mostrarMensaje(e.getMessage());
        }

    }

    private void cargarBarriles(){
        String tablaD[][];
        String consulta="SELECT isnull((('01' + right('00' + convert(varChar(2),1),2) + right('000000' + convert(varChar(6),OpHis.Consecutivo),6))),'Sin Asignar') as Etiqueta, " +
                " C.Codigo as Uso,Al.Descripcion as Alcohol,OpHis.Capacidad,case when OpHis.tipoLl=1 then 'Completo' else 'Parcial' end as Tipo from WM_OperacionTQH Op " +
                " left join WM_OperacionTQHDetalle OpDe on Op.IdOperacion = OpDe.IdOperacion left join WM_OperacionTQHBarrilHis OpHis on OpHis.IdOperacion=Op.IdOperacion " +
                " left Join WM_LoteBarrica LB on LB.IdLoteBarica = OpHis.IdLoteBarrica inner Join PR_Lote L on L.Idlote = LB.IdLote " +
                " inner Join CM_CodEdad CE on CE.IdCodEdad = OpHis.IdCodificacion " +
                " inner Join CM_Codificacion C on C.IdCodificacion = CE.IdCodificicacion " +
                " inner Join CM_Edad E on E.IdEdad = CE.IdEdad " +
                " inner Join CM_Alcohol Al on Al.IdAlcohol = L.IdAlcohol where OpHis.IdOrden="+idOrden;
        try {
            tablaD=getFunciones().consultaTabla(consulta, SQLConnection.db_AAB,5);
            //trasegar.setEnabled(tabla.length==totalBarriles);
            tabla.setDataAdapter(new SimpleTableDataAdapter(this, tablaD));
            tabla.setAutoHeight(tablaD.length);
            total.setText("Total: "+tablaD.length);
        } catch (Exception e) {
            getFunciones().mostrarMensaje(e.getMessage());
        }
    }
    private void toggleButtons(boolean opc){
        aceptar.setEnabled(opc);
        etiqueta.setEnabled(opc);
        camara.setEnabled(opc);
        bombear.setEnabled(!opc);

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
            String lanzaLec=jsonArray.getJSONObject(0).getString("valor");
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