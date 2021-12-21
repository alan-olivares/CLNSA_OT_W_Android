package com.pims.alanolivares.clnsa_ot_w.Funciones;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.pims.alanolivares.clnsa_ot_w.R;

import de.codecrafters.tableview.listeners.TableDataClickListener;

public class ClasePadre  extends AppCompatActivity {
    FuncionesGenerales func;
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
    public FuncionesGenerales getFunciones(){
        return func;
    }
    public void setTitulo(String nombre){
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(nombre);
        }
    }

    public void setEtiqueta(EditText etiqueta){
        this.etiqueta=etiqueta;
        etiqueta.requestFocus();
        etiqueta.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().length()>=10 && !func.valEtiBarr(charSequence.toString())){
                    etiqueta.setText("");
                    func.mostrarMensaje("Etiqueta invalida");
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
    public void setCopyEtiqueta(NoScrollViewTable tabla){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        tabla.addDataClickListener(new TableDataClickListener<String[]>() {
            @Override
            public void onDataClicked(int rowIndex, String[] clickedData) {
                ClipData clip = ClipData.newPlainText("Etiqueta copiada", clickedData[0]);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(ClasePadre.this,"Etiqueta "+clickedData[0]+" copiada",Toast.LENGTH_SHORT).show();
            }
        });
    }
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
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents()!=null){
                    if(func.valEtiBarr(result.getContents())){
                        etiqueta.setText(result.getContents());
                    }else{
                        Toast.makeText(this, "Etiqueta invalida" , Toast.LENGTH_LONG).show();
                    }
                }
            });

}
