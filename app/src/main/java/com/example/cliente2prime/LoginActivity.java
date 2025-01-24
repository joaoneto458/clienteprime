package com.example.cliente2prime;

import android.content.IntentSender;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private TextView text_tela_cadastro;
    private TextView edit_email, edit_senha;
    private Button btn_entrar, btn_login_google;
    private ProgressBar progress_bar;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    String[] mensagens = {"Preencha todos os campos", "Login realizado com sucesso", "Erro ao realizar login"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_login);
        IniciarComponentes();

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        text_tela_cadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
                startActivity(intent);
            }
        });

        btn_entrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edit_email.getText().toString();
                String senha = edit_senha.getText().toString();

                if (email.isEmpty() || senha.isEmpty()) {
                    Toast toast = Toast.makeText(LoginActivity.this, mensagens[0], Toast.LENGTH_SHORT);
                    if (toast.getView() != null) {
                        toast.getView().setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                        TextView text = toast.getView().findViewById(android.R.id.message);
                        text.setTextColor(Color.BLACK);
                    }
                    toast.show();
                } else {
                    AutenticarUsuario(v);
                }
            }
        });

        btn_login_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(LoginActivity.this, new OnSuccessListener<BeginSignInResult>() {
                    @Override
                    public void onSuccess(BeginSignInResult result) {
                        try {
                            startIntentSenderForResult(result.getPendingIntent().getIntentSender(), 2, null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .addOnFailureListener(LoginActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private void AutenticarUsuario(View view) {
        String email = edit_email.getText().toString();
        String senha = edit_senha.getText().toString();

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, senha).addOnCompleteListener((new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    progress_bar.setVisibility(View.VISIBLE);
                    // Store login method
                    getSharedPreferences("LoginPrefs", MODE_PRIVATE).edit().putString("loginMethod", "email").apply();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            IrParaPerfilActivity();
                        }
                    }, 2000);
                } else {
                    String erro;
                    if (task.getException() != null) {
                        try {
                            throw task.getException();
                        } catch (Exception e) {
                            erro = "Erro ao realizar login";
                        }
                    } else {
                        erro = "Erro ao realizar login";
                    }
                    Toast toast = Toast.makeText(LoginActivity.this, erro, Toast.LENGTH_SHORT);
                    if (toast.getView() != null) {
                        toast.getView().setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                        TextView text = toast.getView().findViewById(android.R.id.message);
                        text.setTextColor(Color.BLACK);
                    }
                    toast.show();
                }
            }
        }));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser usuarioAtual = FirebaseAuth.getInstance().getCurrentUser();

        if (usuarioAtual != null) {
            IrParaPerfilActivity();
        }

        // Configure Google One Tap Sign-In
        oneTapClient = Identity.getSignInClient(this);
        signInRequest = new BeginSignInRequest.Builder()
        .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                        .setServerClientId(getString(R.string.default_web_client_id)) // Add this in strings.xml
                .setFilterByAuthorizedAccounts(false)
                        .build())
        .setAutoSelectEnabled(true)
        .build();
    }

    private void IrParaPerfilActivity() {
        Intent intent = new Intent(LoginActivity.this, PerfilActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2) {
            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                if (credential != null) {
                    String idToken = credential.getGoogleIdToken();
                    String googleAccountName = credential.getDisplayName();
                    String googleAccountEmail = credential.getId();
                    if (idToken != null) {
                        FirebaseAuth.getInstance().signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Store login method
                                    getSharedPreferences("LoginPrefs", MODE_PRIVATE).edit().putString("loginMethod", "google").apply();
                                    Intent intent = new Intent(LoginActivity.this, PerfilActivity.class);
                                    intent.putExtra("googleAccountName", googleAccountName);
                                    intent.putExtra("googleAccountEmail", googleAccountEmail);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void IniciarComponentes() {
        text_tela_cadastro = findViewById(R.id.text_tela_cadastro);
        edit_email = findViewById(R.id.edit_email);
        edit_senha = findViewById(R.id.edit_senha);
        btn_entrar = findViewById(R.id.btn_entrar);
        btn_login_google = findViewById(R.id.btn_login_google);
        progress_bar = findViewById(R.id.progress_bar);
    }
}