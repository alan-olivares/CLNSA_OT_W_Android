package com.pims.alanolivares.clnsa_ot_w.Vistas;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pims.alanolivares.clnsa_ot_w.BuildConfig;
import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLConnection;
import com.pims.alanolivares.clnsa_ot_w.DataBase.SQLLocal;
import com.pims.alanolivares.clnsa_ot_w.Funciones.FuncionesGenerales;
import com.pims.alanolivares.clnsa_ot_w.R;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class Inicio extends AppCompatActivity {
    FuncionesGenerales func;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        SharedPreferences preferences = getSharedPreferences("Usuarios",MODE_PRIVATE);
        String usuario = preferences.getString("usuario","No existe");
        String contrasena = preferences.getString("contra","No existe");
        func=new FuncionesGenerales(this);
        int caso=getIntent().getIntExtra("caso",0);
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent mainIntent=null;
                if(!usuario.equals("No existe")){
                    if(func.isOnlineNet() && func.servidorActivo()){
                        if(!func.horaCorrecta()){
                            return;
                        }
                        String result=func.Login(usuario,contrasena);
                        if(result.equals("1")){
                            mainIntent = new Intent(Inicio.this,MenuLateral.class);
                            func.activarNotificaciones();
                        }else{
                            mainIntent = new Intent(Inicio.this,LoginActivity.class);
                            func.mostrarMensaje(result);
                            func.desactivarNotificaciones();
                        }
                    }else{
                        mainIntent = new Intent(Inicio.this,MenuLateral.class);
                        func.activarNotificaciones();
                    }
                }else{
                    mainIntent = new Intent(Inicio.this,LoginActivity.class);
                    func.desactivarNotificaciones();
                }
                mainIntent.putExtra("tipo",caso);
                Inicio.this.startActivity(mainIntent);
                Inicio.this.finish();
            }
        }, 100);;
    }

}
