package com.pims.alanolivares.clnsa_ot_w.Funciones;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.pims.alanolivares.clnsa_ot_w.R;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.listeners.SwipeToRefreshListener;
import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.model.TableColumnDpWidthModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class ClasePadreFragment  extends Fragment {
    FuncionesGenerales func;
    EditText etiqueta;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        func=new FuncionesGenerales(getContext());
    }
    public FuncionesGenerales getFunciones(){
        return func;
    }

    public void setEtiqueta(EditText etiqueta){
        this.etiqueta=etiqueta;
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
        etiqueta.requestFocus();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
    public void setTitle(String titulo){
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(titulo);
    }
    public void setCamara(Button camara){
        camara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //func.openCamera();
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
    public void setTabla(TableView<String[]> tabla, String[] spaceProbeHeaders){
        //tabla.setSwipeToRefreshEnabled( true );
        setHasOptionsMenu(true);
        tabla.setHeaderAdapter(new SimpleTableHeaderAdapter(getContext(),spaceProbeHeaders));
        tabla.addDataClickListener(new TableDataClickListener<String[]>() {
            @Override
            public void onDataClicked(int rowIndex, String[] clickedData) {
                final BottomSheetDialog bottomSheet = new BottomSheetDialog(getActivity());
                View bottomSheet2 = getLayoutInflater().inflate(R.layout.detalles_result, null);
                proceso(rowIndex,bottomSheet2,bottomSheet);
            }
        });
    }
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_rep, menu);
        final MenuItem actualizar=menu.findItem(R.id.actualizar);
        actualizar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                cargarDatos();
                return false;
            }
        });
    }
    public void cargarDatos(){
        //Codigo diferente en cada fragment
    }
    public void proceso(int index, View vista,BottomSheetDialog bottomSheet){
        Button cerrar = vista.findViewById(R.id.cancelar);
        cerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheet.dismiss();
            }
        });
        ProgressBar progress=vista.findViewById(R.id.progressDR);
        progress.setVisibility(View.VISIBLE);
    }
    public void llenarTabla(String[] head,String datos[][],NoScrollViewTable tabla){
        tabla.setAutoHeight(datos.length);
        tabla.setColumnCount(head.length);
        tabla.setHeaderAdapter(new SimpleTableHeaderAdapter(getContext(),head));
        tabla.setDataAdapter(new SimpleTableDataAdapter(getContext(), datos));
    }
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents()!=null){
                    if(func.valEtiBarr(result.getContents())){
                        etiqueta.setText(result.getContents());
                    }else{
                        Toast.makeText(getContext(), "Etiqueta invalida" , Toast.LENGTH_LONG).show();
                    }
                }
            });
}
