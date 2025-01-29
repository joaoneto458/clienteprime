package com.example.cliente2prime;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.widget.TextView;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CadastroActivity extends AppCompatActivity {

    private EditText edit_nome, edit_email, edit_senha;
    private Button btn_cadastrar;

    String[] mensagens = {"Preencha todos os campos", "Cadastro realizado com sucesso", "Erro ao cadastrar usuário", "Senha muito curta"};
    String usuarioID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_cadastro);
        IniciarComponentes();

        btn_cadastrar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String nome = edit_nome.getText().toString();
                String email = edit_email.getText().toString();
                String senha = edit_senha.getText().toString();
                if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                    Toast.makeText(CadastroActivity.this, mensagens[0], Toast.LENGTH_SHORT).show();
                } else {
                    if (senha.length() < 6) {
                        Toast.makeText(CadastroActivity.this, mensagens[3], Toast.LENGTH_SHORT).show();
                    } else {
                        CadastrarUsuario(v);
                    }
                }
            }
        });

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void IniciarComponentes() {
        edit_nome = findViewById(R.id.edit_nome);
        edit_email = findViewById(R.id.edit_email);
        edit_senha = findViewById(R.id.edit_senha);
        btn_cadastrar = findViewById(R.id.btn_cadastrar);
    }

    private void CadastrarUsuario(View v) {
        // Implementation for user registration
        String email = edit_email.getText().toString();
        String senha = edit_senha.getText().toString();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    SalvarDadosUsuario();

                    Toast toast = Toast.makeText(CadastroActivity.this, mensagens[1], Toast.LENGTH_SHORT);
                    Objects.requireNonNull(toast.getView()).setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                    TextView text = toast.getView().findViewById(android.R.id.message);
                    text.setTextColor(Color.BLACK);
                    toast.show();
                } else {
                    String erro;
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        erro = "Digite uma senha com no mínimo 6 caracteres";
                    } catch (FirebaseAuthUserCollisionException e) {
                        erro = "Essa conta já foi cadastrada";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        erro = "E-mail inválido";
                    } catch (Exception e) {
                        erro = "Erro ao cadastrar usuário";
                    }
                    Toast toast = Toast.makeText(v.getContext(), erro, Toast.LENGTH_SHORT);
                    Objects.requireNonNull(toast.getView()).setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                    TextView text = toast.getView().findViewById(android.R.id.message);
                    text.setTextColor(Color.BLACK);
                    toast.show();
                }
            }
        });
    }

    private void SalvarDadosUsuario() {

        String nome = edit_nome.getText().toString();
        String email = edit_email.getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, String> usuarios = new HashMap<>();
        usuarios.put("nome", nome);
        usuarios.put("email", email);

        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference documentReference = db.collection("Usuarios").document(usuarioID);
        documentReference.set(usuarios).addOnCompleteListener(new OnCompleteListener<Void>() {
          @Override
          public void onComplete(@NonNull Task<Void> task) {
              Log.d("db", "Sucesso ao salvar os dados do usuário");
          }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("db", "Erro ao salvar os dados do usuário" + e.toString());
            }
        });
    }
}