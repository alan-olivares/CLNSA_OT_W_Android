package com.pims.alanolivares.clnsa_ot_w.Funciones;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.pims.alanolivares.clnsa_ot_w.R;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

/**
 * <p>Clase que extiende de Fragment para el procesamiento de
 * funciones principales dentro de las fragments usadas
 * </p>
 *
 * @author Alan Israel Olivares Mora
 * @version v1.0
 *
 */
public class ClasePadreFragment  extends Fragment {
    /**
     * Referencia a la clase de FuncionesGenerales
     */
    FuncionesGenerales func;
    /**
     * Editext del campo etiqueta
     */
    EditText etiqueta;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        func=new FuncionesGenerales(getContext());
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
     * Método que valida la entrada de texto del usuario, así como habilitar la entrada de la etiqueta
     * en cuanto el usuario ingrese a la fragment
     *
     * @param etiqueta - Campo de la etiqueta
     */
    public void setEtiqueta(EditText etiqueta){
        this.etiqueta=etiqueta;
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
        //Habilitamos el campo de la etiqueta para poder traer los datos copiados del dispositivo
        etiqueta.requestFocus();
        //Cerramos el teclado para que no sea visible y no obstraya información de la pantalla
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
    /**
     * Método que asigna un texto al Action Bar
     *
     * @param titulo - Texto a asignar
     */
    public void setTitle(String titulo){
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(titulo);
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
                options.setCameraId(0);  // 0 es la camara trasera del celular
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
                        Toast.makeText(getContext(), "Etiqueta invalida" , Toast.LENGTH_LONG).show();
                    }
                }
            });

    /**
     * Método que se encarga de los eventos de click dentro de la tabla. Este ejecutará el codigo que se
     * encuentra en el método proceso enviandole nuevas referencias del menu inferior BottomSheetDialog
     *
     * @param tabla - Referencia la tabla
     * @param spaceProbeHeaders - Array del nombre de las columnas
     */
    public void setTabla(TableView<String[]> tabla, String[] spaceProbeHeaders){
        //tabla.setSwipeToRefreshEnabled( true );
        setHasOptionsMenu(true);
        tabla.setHeaderAdapter(new SimpleTableHeaderAdapter(getContext(),spaceProbeHeaders));
        tabla.addDataClickListener(new TableDataClickListener<String[]>() {
            @Override
            public void onDataClicked(int rowIndex, String[] clickedData) {
                final BottomSheetDialog bottomSheet = new BottomSheetDialog(getActivity());
                View vista = getLayoutInflater().inflate(R.layout.detalles_result, null);
                proceso(rowIndex,vista,bottomSheet);
            }
        });
    }
    /**
     * Método que crea un boton en el Action Bar el cual permite actualizar la tabla principal
     *
     * @param menu - Referencia del menu
     * @param inflater - Referencia del inflador
     */
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
    /**
     * Estructura de método que se encarga de traer los datos del servidor para agregarlos
     * a la tabla principal
     */
    public void cargarDatos(){
        //Codigo diferente en cada fragment
    }
    /**
     * Método que trae la información adicional de cada renglon de la tabla principal
     * y la muestra en un menu inferior
     *
     * @param index - Indice de la tabla rpincipal
     * @param vista - Vista creada del layout detalles_result
     * @param bottomSheet - Referencia de BottomSheetDialog
     */
    public void proceso(int index, View vista,BottomSheetDialog bottomSheet){
        bottomSheet.setContentView(vista);
        bottomSheet.show();
        Button cerrar = vista.findViewById(R.id.cancelar);
        cerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheet.dismiss();
            }
        });
    }
    /**
     * Método agrega encabezado y datos a una tabla de tipo NoScrollViewTable
     *
     * @param head - Columna de encabezado
     * @param datos - Array bi-dimencional de datos a agregar a la tabla
     * @param tabla - Referencia de la tabla
     */
    public void llenarTabla(String[] head,String datos[][],NoScrollViewTable tabla){
        tabla.setAutoHeight(datos.length);
        tabla.setColumnCount(head.length);
        tabla.setHeaderAdapter(new SimpleTableHeaderAdapter(getContext(),head));
        tabla.setDataAdapter(new SimpleTableDataAdapter(getContext(), datos));
    }

}
