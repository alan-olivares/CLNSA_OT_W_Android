package com.pims.alanolivares.clnsa_ot_w.Funciones;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.pims.alanolivares.clnsa_ot_w.Vistas.AvanceOrden;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

/**
 * <p>Clase que extiende de AppCompatActivity para el procesamiento de
 * funciones principales dentro de las activities usadas
 * </p>
 *
 * @author Alan Israel Olivares Mora
 * @version v1.0
 *
 */

public class ClasePadre  extends AppCompatActivity {
    /**
     * Referencia a la clase de FuncionesGenerales
     */
    private FuncionesGenerales func;
    /**
     * Editext del campo etiqueta
     */
    private EditText etiqueta;
    private int opcion=-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        func=new FuncionesGenerales(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
    /**
     * Método que obtiene la referencia de las funciones principales
     *
     * @return FuncionesGenerales
     */
    public FuncionesGenerales getFunciones(){
        return func;
    }

    /**
     * Método que asigna un texto al Action Bar
     *
     * @param nombre - Texto a asignar
     */
    public void setTitulo(String nombre){
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(nombre);
        }
    }

    /**
     * Método que valida la entrada de texto del usuario, así como habilitar la entrada de la etiqueta
     * en cuanto el usuario ingrese a la fragment
     *
     * @param etiqueta - Campo de la etiqueta
     */
    public void setEtiqueta(EditText etiqueta){
        this.etiqueta=etiqueta;
        //Habilitamos el campo de la etiqueta para poder traer los datos copiados del dispositivo
        etiqueta.requestFocus();
        /*etiqueta.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    insertaEtiqueta(etiqueta.getText().toString());
                    return true;
                }
                return false;
            }
        });*/
        etiqueta.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                etiqueta.setError(null);
                if(charSequence.toString().length()>=10 && !func.valEtiBarr(charSequence.toString()))
                    etiqueta.setError("Etiqueta invalida");
                else if(charSequence.toString().contains("\n"))
                    insertaEtiqueta(charSequence.toString());
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        //Cerramos el teclado para que no sea visible y no obstraya información de la pantalla
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
    /**
     * Método que permite correr el proceso de insertar la etiqueta desde esta clase, donde al final
     * termina dejando vacio el campo de etiqueta para estar prepardo para el siguiente proceso
     *
     */
    public void insertaEtiqueta(String eti){
        try {
            validaEtiqueta(eti);
        } catch (Exception e) {
            getFunciones().mostrarMensaje(e.getMessage());
        }finally {
            etiqueta.post(new Runnable() {
                @Override
                public void run() {
                    etiqueta.setText("");
                    etiqueta.requestFocus();
                }
            });

        }
    }
    /**
     * Método que permite validar si la etiqueta está correcta, este metodo debe ser sobre escrito
     * en todas las clases que necesite validar la etiqueta
     *
     * @param etiqueta - Etiqueta que se necesita validar
     */
    public void validaEtiqueta(String etiqueta) throws Exception{
        if(!func.valEtiBarr(etiqueta)){
            func.makeErrorSound();
            throw new Exception("Etiqueta invalida");
        }
    }
    /**
     * Método que permite copiar el campo de etiqueta al portapapeles para cualquier uso requerido
     *
     * @param tabla - Referencia de la tabla
     */
    public void setCopyEtiqueta(NoScrollViewTable tabla,int indice){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        tabla.addDataClickListener(new TableDataClickListener<String[]>() {
            @Override
            public void onDataClicked(int rowIndex, String[] clickedData) {
                ClipData clip = ClipData.newPlainText("Etiqueta", clickedData[indice]);
                clipboard.setPrimaryClip(clip);
                getFunciones().mostrarMensaje("Etiqueta "+clickedData[indice]+" copiada");
            }
        });
    }
    /**
     * Método que ejecuta el proceso de abrir la camara del dispositivo y ejecutar la actividad
     * de la libreria journeyapps el cual se encargará de darnos el resultado del valor escaneado
     * y posteriormente agregar este resultado al EditText de la etiqueta
     *
     * @param camara - Referencia del boton de la camara
     */
    public void setCamara(Button camara){
        camara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScanOptions options = new ScanOptions();
                options.setDesiredBarcodeFormats(ScanOptions.ONE_D_CODE_TYPES);
                options.setPrompt("Escanea la etiqueta");
                options.setCameraId(0);  // Use a specific camera of the device
                options.setBeepEnabled(true);
                options.setBarcodeImageEnabled(true);
                barcodeLauncher.launch(options);
            }
        });
    }
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents()!=null){
                    if(func.valEtiBarr(result.getContents())){
                        etiqueta.setText(result.getContents()+"\n");
                        //insertaEtiqueta(result.getContents());
                    }else{
                        getFunciones().mostrarMensaje("Etiqueta invalida");
                    }
                }
            });

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void ordenar(NoScrollViewTable dataTable,String tabla[][],ArrayList<String> listaopc){
        final android.app.AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setTitle("Ordenar por: ");
        builderSingle.setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        ArrayAdapter<String> adapter= new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice,listaopc);

        builderSingle.setSingleChoiceItems(adapter, opcion, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                opcion=which;
                Arrays.sort(tabla, new Comparator<String[]>() {
                    @Override
                    public int compare(String[] strings, String[] t1) {
                        return strings[which].compareTo(t1[which]);
                    }
                });
                updateTable(dataTable,tabla,tabla.length );
                dialog.dismiss();
            }
        });
        builderSingle.show();
    }
    protected void updateTable(NoScrollViewTable dataTable,String nueva[][],int tamano ){
        dataTable.setDataAdapter(new SimpleTableDataAdapter(this, nueva));
        dataTable.setAutoHeight(tamano);
    }
    public void llenarTabla(String[] head,String datos[][],NoScrollViewTable tabla){
        tabla.setAutoHeight(datos.length);
        tabla.setColumnCount(head.length);
        tabla.setHeaderAdapter(new SimpleTableHeaderAdapter(this,head));
        tabla.setDataAdapter(new SimpleTableDataAdapter(this, datos));
    }

}
