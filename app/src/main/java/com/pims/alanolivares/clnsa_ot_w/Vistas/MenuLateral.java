package com.pims.alanolivares.clnsa_ot_w.Vistas;
import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.Funciones.FuncionesGenerales;
import com.pims.alanolivares.clnsa_ot_w.R;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;

public class MenuLateral extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    Llenado llenado =new Llenado();
    Relleno rellenado =new Relleno();
    Trasiego trasiego=new Trasiego();
    TrasiegoHoover trasiegoHoover= new TrasiegoHoover();
    Reparacion reparacion=new Reparacion();
    Revision revision=new Revision();
    Montacargas montacargas=new Montacargas();
    Inventario inventario=new Inventario();
    Object paginas[]={llenado,rellenado,trasiego,trasiegoHoover,reparacion,revision,montacargas,inventario};
    int vistas[]={R.id.llenado,R.id.relleno,R.id.trasiego,R.id.trasiegoHo,R.id.reparacion,R.id.revision,R.id.montacargas,R.id.inventario};
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;
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
        fragmentManager=getSupportFragmentManager();
        //int caso=getIntent().getIntExtra("caso",0);
        int caso=0;
        fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.contenedor, (Fragment) paginas[caso]);
        fragmentTransaction.commit();
        navigationView.post(new Runnable() {
            @Override
            public void run() {
                navigationView.setCheckedItem(vistas[caso]);
            }
        });

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
        SharedPreferences preferences = getSharedPreferences("Usuarios",MODE_PRIVATE);
        String nombre=preferences.getString("nombre","No existe");
        String pistolas=preferences.getString("pistola","1");
        nombres.setText("Usuario: "+nombre);
        version.setText("Versión: "+version1);
        pistola.setText("Pistola #"+pistolas);
        configurarPrimerUso();
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
    private void configurarPrimerUso(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE )
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE )
                == PackageManager.PERMISSION_GRANTED ){
            File f = new File(Environment.getExternalStorageDirectory().getPath(), "TBRE");
            try {
                if(!f.exists()){
                    Files.createDirectory(Paths.get(f.getPath()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(this,"Se necesitan permisos de archivos para escribir los logs",Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

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
        } else if (id == R.id.trasiegoHo) {
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
        } else if (id == R.id.nav_send) {
            Intent email = new Intent(Intent.ACTION_SEND);
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{ "sjacobo@pims.com.mx"});
            email.putExtra(Intent.EXTRA_SUBJECT, "Asunto con la pistola "+func.getPistola());
            email.putExtra(Intent.EXTRA_TEXT, "¡Hola! Soy el usuario con ID: "+func.getIdUsuario()+"\n" +
                    "\n\n\n\nDatos para el desarrollador: \n"+getDatos());
            email.setType("message/rfc822");
            startActivity(Intent.createChooser(email, "Escoge una opción de correo electronico:"));
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
                "Manofacturación: " + Build.MANUFACTURER+"\n"+
                "Base: " + Build.VERSION_CODES.BASE+"\n"+
                "SDK  " + Build.VERSION.SDK+"\n"+
                "BOARD: " + Build.BOARD+"\n"+
                "Versión: " + Build.VERSION.RELEASE+"\n";
    }

}