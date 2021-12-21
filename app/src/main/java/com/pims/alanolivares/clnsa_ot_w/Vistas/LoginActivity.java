package com.pims.alanolivares.clnsa_ot_w.Vistas;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.pims.alanolivares.clnsa_ot_w.Funciones.FuncionesGenerales;
import com.pims.alanolivares.clnsa_ot_w.R;

import java.sql.ResultSet;
import java.sql.SQLException;


public class LoginActivity extends AppCompatActivity {
    // UI references.
    private AutoCompleteTextView usuarioEdit;
    private EditText contrasenaEdit,pistola;
    Boolean as=true;
    FuncionesGenerales func;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Configure el formulario de inicio de sesión.
        usuarioEdit = (AutoCompleteTextView) findViewById(R.id.email);
        contrasenaEdit = (EditText) findViewById(R.id.password);
        pistola=findViewById(R.id.pistolaLogin);
        progressBar=findViewById(R.id.login_progress);
        contrasenaEdit.setLongClickable(false);
        usuarioEdit.setLongClickable(false);
        func=new FuncionesGenerales(this);
        contrasenaEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    as=true;
                    return true;
                }
                as=false;
                return false;
            }
        });
        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(contrasenaEdit.getWindowToken(), 0);
                attemptLogin();

            }
        });
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE )
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE )
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA )
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE )
                == PackageManager.PERMISSION_GRANTED){

        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void attemptLogin() {
        // Restablecer errores.
        progressBar.setVisibility(View.VISIBLE);
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                usuarioEdit.setError(null);
                contrasenaEdit.setError(null);
                pistola.setError(null);
                // Store values at the time of the login attempt.
                String usuario = usuarioEdit.getText().toString();
                String password = contrasenaEdit.getText().toString();
                String pistol = pistola.getText().toString();
                boolean cancel=false;
                View focusView = null;
                if (!isPasswordValid(password)) {
                    contrasenaEdit.setError("Campo obligatorio");
                    focusView = contrasenaEdit;
                    cancel = true;
                }
                if (!isEmailValid(usuario)) {
                    usuarioEdit.setError("Campo obligatorio");
                    focusView = usuarioEdit;
                    cancel = true;
                }
                if (!isEmailValid(pistol)) {
                    pistola.setError("Campo obligatorio");
                    focusView = pistola;
                    cancel = true;
                }
                if (cancel) {
                    // Hubo un error; No intente iniciar sesión
                    // y enfocar el primer campo de formulario con un error.
                    focusView.requestFocus();
                } else {
                    Intent mainIntent=null;
                    if(func.isOnlineNet() && func.servidorActivo()){
                        if(func.horaCorrecta()){
                            String result=func.Login(usuario,password,pistol);
                            if(result.equals("1")){
                                func.activarNotificaciones();
                                mainIntent = new Intent(LoginActivity.this,MenuLateral.class);
                                startActivity(mainIntent);
                                finish();
                            }else{
                                Snackbar.make(getCurrentFocus(), result, Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }else{
                        Snackbar.make(getCurrentFocus(), "No tienes conexión a la base de datos", Snackbar.LENGTH_LONG).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return !email.isEmpty();
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return !password.isEmpty();
    }

}

