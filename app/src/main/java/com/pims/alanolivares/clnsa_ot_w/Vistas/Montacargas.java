package com.pims.alanolivares.clnsa_ot_w.Vistas;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Funciones.AdapterVentana;
import com.pims.alanolivares.clnsa_ot_w.Funciones.ClasePadreFragment;
import com.pims.alanolivares.clnsa_ot_w.Funciones.FuncionesGenerales;
import com.pims.alanolivares.clnsa_ot_w.Funciones.NoScrollViewTable;
import com.pims.alanolivares.clnsa_ot_w.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.model.TableColumnDpWidthModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class Montacargas extends ClasePadreFragment {
    private TabLayout mTabs;
    private View mIndicator;
    private ViewPager mViewPager;
    private int indicatorWidth;
    private AdapterVentana adapter;
    private int idBodega;
    private ProgressBar progressBar;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_montacargas, container, false);
        mTabs = view.findViewById(R.id.tab);
        idBodega=Integer.valueOf(getFunciones().getDatoCache("bodegaMan"));
        idBodega=idBodega==-1?21:idBodega;
        mIndicator = view.findViewById(R.id.indicator);
        mViewPager = view.findViewById(R.id.viewPager);
        progressBar=view.findViewById(R.id.progressMonta);
        //Set up the view pager and fragments
        adapter = new AdapterVentana(getChildFragmentManager());
        adapter.addFragment(new MontPendientes(), "Pendientes");
        adapter.addFragment(new MontRealizadas(), "Realizadas");
        mViewPager.setAdapter(adapter);
        mTabs.setupWithViewPager(mViewPager);
        setHasOptionsMenu(true);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float positionOffset, int positionOffsetPx) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)mIndicator.getLayoutParams();
                //Multiply positionOffset with indicatorWidth to get translation
                float translationOffset =  (positionOffset+i) * indicatorWidth ;
                params.leftMargin = (int) translationOffset;
                mIndicator.setLayoutParams(params);
            }

            @Override
            public void onPageSelected(int i) {
                adapter.getItem(i).onResume();
                //System.out.println(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        mTabs.post(new Runnable() {
            @Override
            public void run() {
                indicatorWidth = mTabs.getWidth() / mTabs.getTabCount();
                //Assign new width
                FrameLayout.LayoutParams indicatorParams = (FrameLayout.LayoutParams) mIndicator.getLayoutParams();
                indicatorParams.width = indicatorWidth;
                mIndicator.setLayoutParams(indicatorParams);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter!=null)
            mTabs.post(new Runnable() {
                @Override
                public void run() {
                    ((MontPendientes) adapter.getItem(0)).setPendientes();
                }
            });

    }
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_monta, menu);
        final MenuItem sincronizar=menu.findItem(R.id.sincronizar);
        final MenuItem reubicar=menu.findItem(R.id.reubicar);
        final MenuItem actualizarBod=menu.findItem(R.id.actualizarBod);
        final MenuItem actualizarEst=menu.findItem(R.id.actualizarEst);
        final MenuItem actualizarPall=menu.findItem(R.id.formarMP);
        sincronizar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                sincronizar();
                return false;
            }
        });
        actualizarPall.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent menu=new Intent(getContext(),FormarPaleta.class);
                startActivity(menu);
                return false;
            }
        });
        reubicar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent menu=new Intent(getContext(),ReubiTarima.class);
                startActivity(menu);
                return false;
            }
        });
        actualizarBod.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent menu=new Intent(getContext(),SincBodega.class);
                startActivity(menu);
                return false;
            }
        });
        actualizarEst.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                new Inventario().sincEstructura(getContext(),getFunciones());
                return false;
            }
        });
    }

    @Override
    public void cargarDatos() {
        switch (mTabs.getSelectedTabPosition()){
            case 1:
                ((MontPendientes) adapter.getItem(0)).cargarDatos();
                break;
            case 2:
                ((MontRealizadas) adapter.getItem(1)).cargarDatos();
                break;
        }
    }

    private void sincronizar(){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    guardarProgreso();
                    //Sincronizar la bodega de recien llenados
                    new SincBodega().cargarBodega(idBodega,getFunciones());
                    getFunciones().mostrarMensaje("Los cambios se han cargado correctamente");
                    cargarDatos();
                } catch (Exception e) {
                    getFunciones().mostrarMensaje(e.getMessage());
                }finally {
                    progressBar.setVisibility(View.GONE);
                }
            }
        },500);

    }
    private void guardarProgreso() throws Exception{
        JSONArray jsonArray = getFunciones().getJsonLocal("Select Consecutivo,Racklocfin,Tipo from PR_NvUbicacion");
        //Guradar datos en PR_NvUbicacion
        for(int x=0;x<jsonArray.length();x++){
            JSONObject object=jsonArray.getJSONObject(x);
            getFunciones().insertaData("INSERT INTO PR_NvUbicacion (Consecutivo,RacklocFin,Tipo) Values(" +object.getString("Consecutivo")+ "," +object.getString("Racklocfin")+ "," +object.getString("tipo")+ ")", SQLConnection.db_AAB);
        }
        //Ejecutar procedure
        jsonArray=getFunciones().consultaJson("exec sp_AplicaNivelacion '"+getFunciones().getIdUsuario()+"','"+getFunciones().getPistola()+"'",SQLConnection.db_AAB);
        if(jsonArray.getJSONObject(0).getString("msg").equals("1")){
            getFunciones().borrarRegistros("PR_NvUbicacion");
        }else{
            getFunciones().mostrarMensaje("Error al aplicar las nivelaciones en el servidor, por favor inteta de nuevo");
        }
    }
}


