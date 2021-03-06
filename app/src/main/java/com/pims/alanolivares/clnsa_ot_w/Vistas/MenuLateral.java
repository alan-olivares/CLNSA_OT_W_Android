package com.pims.alanolivares.clnsa_ot_w.Vistas;

import com.pims.alanolivares.clnsa_ot_w.Funciones.FuncionesGenerales;
import com.pims.alanolivares.clnsa_ot_w.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

public class MenuLateral extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private Llenado llenado =new Llenado();
    private Relleno rellenado =new Relleno();
    private Trasiego trasiego=new Trasiego();
    private TrasiegoHoover trasiegoHoover= new TrasiegoHoover();
    private Reparacion reparacion=new Reparacion();
    private Revision revision=new Revision();
    private Montacargas montacargas=new Montacargas();
    private Inventario inventario=new Inventario();
    private TraspasoHoover traspaso=new TraspasoHoover();
    private Configuracion configuracion;
    private Object paginas[]={llenado,rellenado,trasiego,trasiegoHoover,reparacion,revision,montacargas,inventario};
    private int vistas[]={R.id.llenado,R.id.relleno,R.id.trasiego,R.id.trasiegoHo,R.id.reparacion,R.id.revision,R.id.montacargas,R.id.inventario};
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar toolbar;
    private NavigationView navigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_lateral);
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout=findViewById(R.id.drawer_layout);
        navigationView=findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        actionBarDrawerToggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();
        preparaMenu();
        View hView = navigationView.getHeaderView(0);
        float ver=0;
        String version1="";
        try {
            PackageInfo pInfo = MenuLateral.this.getPackageManager().getPackageInfo(getPackageName(), 0);
            version1 = pInfo.versionName;
            //ver = Float.parseFloat(version1);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        TextView nombres = (TextView) hView.findViewById(R.id.nombreIni);
        TextView pistola = (TextView) hView.findViewById(R.id.pistolaMenu);
        TextView version = (TextView) hView.findViewById(R.id.version);
        configuracion=new Configuracion(nombres,pistola);
        SharedPreferences preferences = getSharedPreferences("Usuarios",MODE_PRIVATE);
        String nombre=preferences.getString("nombre","No existe");
        String pistolas=preferences.getString("pistola","1");
        nombres.setText("Usuario: "+nombre);
        version.setText("Versi??n: "+version1);
        pistola.setText("Pistola #"+pistolas);
        //configurarPrimerUso();
    }
    private void preparaMenu(){
        String grupo=getGrupo();
        //int caso=getIntent().getIntExtra("caso",0);
        int caso=0;
        Menu  menu=navigationView.getMenu();
        fragmentManager=getSupportFragmentManager();
        if(grupo.equals("1")||grupo.equals("2")){
            caso=0;
        }else{
            caso=6;
            menu.removeItem(R.id.llenado);
            menu.removeItem(R.id.relleno);
            menu.removeItem(R.id.trasiego);
            menu.removeItem(R.id.trasiegoHo);
            menu.removeItem(R.id.reparacion);
            menu.removeItem(R.id.revision);
            menu.removeItem(R.id.inventario);
        }
        fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.contenedor, (Fragment) paginas[caso]);
        fragmentTransaction.commit();
        int finalCaso = caso;
        navigationView.post(new Runnable() {
            @Override
            public void run() {
                navigationView.setCheckedItem(vistas[finalCaso]);
            }
        });
    }

    private String getGrupo(){
        SharedPreferences preferences = getSharedPreferences("Usuarios", Context.MODE_PRIVATE);
        return preferences.getString("idGrupo","1");
    }


    long back_pressed;
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (back_pressed + 1000 > System.currentTimeMillis()){
            finishAffinity();
        }
        else{
            Toast.makeText(getBaseContext(),"Presiona de nuevo para salir", Toast.LENGTH_SHORT).show();
        }
        back_pressed = System.currentTimeMillis();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        FuncionesGenerales func=new FuncionesGenerales(this);
        int id = item.getItemId();
        if (id == R.id.llenado) {
            fragmentTransaction=fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contenedor,llenado);
            fragmentTransaction.commit();
            item.setCheckable(true);
        } else if (id == R.id.relleno) {
            fragmentTransaction=fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contenedor,rellenado);
            fragmentTransaction.commit();
            item.setCheckable(true);
        } else if (id == R.id.trasiego) {
            fragmentTransaction=fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contenedor,trasiego);
            fragmentTransaction.commit();
            item.setCheckable(true);
        } else if (id == R.id.traspasoHo) {
            fragmentTransaction=fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contenedor,traspaso);
            fragmentTransaction.commit();
            item.setCheckable(true);
        } else  if (id == R.id.trasiegoHo) {
            fragmentTransaction=fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contenedor,trasiegoHoover);
            fragmentTransaction.commit();
            item.setCheckable(true);
        } else if (id == R.id.reparacion) {
            fragmentTransaction=fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contenedor,reparacion);
            fragmentTransaction.commit();
            item.setCheckable(true);
        } else if (id == R.id.revision) {
            fragmentTransaction=fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contenedor,revision);
            fragmentTransaction.commit();
            item.setCheckable(true);
        } else if (id == R.id.montacargas) {
            fragmentTransaction=fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contenedor,montacargas);
            fragmentTransaction.commit();
            item.setCheckable(true);
        } else if (id == R.id.inventario) {
            fragmentTransaction=fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contenedor,inventario);
            fragmentTransaction.commit();
            item.setCheckable(true);
        } else if (id == R.id.config) {
            fragmentTransaction=fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contenedor,configuracion);
            fragmentTransaction.commit();
            item.setCheckable(true);
        } else if (id == R.id.nav_send) {
            SharedPreferences preferences = getSharedPreferences("Usuarios",MODE_PRIVATE);
            String nombre=preferences.getString("nombre","No existe");
            Intent email = new Intent(Intent.ACTION_SEND);
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{ "sjacobo@pims.com.mx","aolivares@pims.com.mx"});
            email.putExtra(Intent.EXTRA_SUBJECT, "Asunto con la pistola "+func.getPistola());
            email.putExtra(Intent.EXTRA_TEXT, "??Hola! Soy "+nombre+" tengo problemas con: \n" +
                    "\n\n\n\n\n\nDatos para el desarrollador: \n"+getDatos());
            email.setType("message/rfc822");
            startActivity(Intent.createChooser(email, "Escoge una opci??n de correo electronico:"));
        }else if (id == R.id.cerrar) {
            func.cerrarSesion();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private String getDatos(){
        return "Marca: " + Build.BRAND+"\n"+
                "Modelo: " + Build.MODEL+"\n"+
                "ID: " + Build.ID+"\n"+
                "Manufacturaci??n: " + Build.MANUFACTURER+"\n"+
                "Base: " + Build.VERSION_CODES.BASE+"\n"+
                "SDK  " + Build.VERSION.SDK+"\n"+
                "BOARD: " + Build.BOARD+"\n"+
                "Versi??n: " + Build.VERSION.RELEASE+"\n";
    }

}