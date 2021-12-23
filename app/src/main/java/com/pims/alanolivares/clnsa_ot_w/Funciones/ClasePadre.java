package com.pims.alanolivares.clnsa_ot_w.Funciones;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import de.codecrafters.tableview.listeners.TableDataClickListener;

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
    FuncionesGenerales func;
    /**
     * Editext del campo etiqueta
     */
    EditText etiqueta;

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
        etiqueta.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                etiqueta.setError(null);
                if(charSequence.toString().length()>=10 && !func.valEtiBarr(charSequence.toString()))
                    etiqueta.setError("Etiqueta invalida");
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        //Cerramos el teclado para que no sea visible y no obstraya información de la pantalla
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
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
                        etiqueta.setText(result.getContents());
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


}
