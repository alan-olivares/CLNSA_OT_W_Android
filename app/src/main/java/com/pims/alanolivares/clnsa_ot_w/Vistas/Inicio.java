package com.pims.alanolivares.clnsa_ot_w.Vistas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.view.WindowManager;
import com.pims.alanolivares.clnsa_ot_w.Funciones.FuncionesGenerales;
import com.pims.alanolivares.clnsa_ot_w.R;

public class Inicio extends AppCompatActivity {
    private FuncionesGenerales func;
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
                if(!usuario.equals("No existe")){//Ya ha iniciado sessión anteriormente
                    if(func.isOnlineNet() && func.servidorActivo()){//Si tiene conexión a internet
                        if(!func.horaCorrecta()){//La hora del dispositivo no coincide con el servidor
                            return;
                        }
                        if(func.versionUpdate()){//Existe una actualización disponible
                            return;
                        }
                        String result=func.Login(usuario,contrasena);//Autenticamos al usuario
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
