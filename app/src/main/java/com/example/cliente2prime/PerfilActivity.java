package com.example.cliente2prime;

import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class PerfilActivity extends AppCompatActivity {
    private TextView perfil_nome, perfil_email;
    private Button btn_sair;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String usuarioID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_perfil);
        IniciarComponentes();

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btn_sair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(PerfilActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String loginMethod = getSharedPreferences("LoginPrefs", MODE_PRIVATE).getString("loginMethod", "email");
            if ("google".equals(loginMethod)) {
                String googleAccountName = getIntent().getStringExtra("googleAccountName");
                String googleAccountEmail = getIntent().getStringExtra("googleAccountEmail");
                if (googleAccountName != null && googleAccountEmail != null) {
                    perfil_nome.setText(googleAccountName);
                    perfil_email.setText(googleAccountEmail);
                } else {
                    perfil_nome.setText(currentUser.getDisplayName());
                    perfil_email.setText(currentUser.getEmail());
                }
            } else {
                String email = currentUser.getEmail();
                usuarioID = currentUser.getUid();
                DocumentReference documentReference = db.collection("Usuarios").document(usuarioID);
                documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            perfil_nome.setText(documentSnapshot.getString("nome"));
                            perfil_email.setText(email);
                        }
                    }
                });
            }
        } else {
            perfil_nome.setText("User not logged in");
            perfil_email.setText("");
        }
    }

    private void IniciarComponentes() {
        perfil_nome = findViewById(R.id.perfil_nome);
        perfil_email = findViewById(R.id.perfil_email);
        btn_sair = findViewById(R.id.btn_sair);
    }
}