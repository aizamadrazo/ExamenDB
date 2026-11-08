package com.example.examendb

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.examendb.ui.theme.ExamenDBTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ExamenDBTheme {
                LoginScreen()
            }
        }
    }
}

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "LOGIN", fontSize = 32.sp)

        Spacer(modifier = Modifier.height(35.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            placeholder = { Text("Introduce tu email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            placeholder = { Text("Introduce tu contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
            onClick = {
                iniciarSesion(email.trim(), password) { correcto, mensaje ->
                    Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                    if (correcto) {
                        val intent = Intent(context, CRUDActivity::class.java)
                        intent.putExtra("user", email.trim())
                        context.startActivity(intent)
                    }
                }
            }
        ) {
            Text("LOGIN")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
            onClick = {
                registrarUsuario(email.trim(), password) { _, mensaje ->
                    Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                }
            }
        ) {
            Text("REGISTRO")
        }
    }
}

// ---------- NO TOCADA ----------
fun registrarUsuario(
    email: String,
    password: String,
    onResultado: (Boolean, String) -> Unit
) {
    val error = validarCampos(email, password)
    if (error != null) {
        onResultado(false, error)
        return
    }
    FirebaseAuth.getInstance()
        .createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->

            if (task.isSuccessful) {
                onResultado(true, "Usuario registrado correctamente")
            } else {
                onResultado(
                    false,
                    task.exception?.message ?: "Error al registrar"
                )
            }
        }
}
// -------------------------------

fun iniciarSesion(
    email: String,
    password: String,
    onResultado: (Boolean, String) -> Unit
) {
    val error = validarCampos(email, password)
    if (error != null) {
        onResultado(false, error)
        return
    }
    FirebaseAuth.getInstance()
        .signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResultado(true, "Inicio de sesión correcto")
            } else {
                onResultado(false, task.exception?.message ?: "Error al iniciar sesión")
            }
        }
}

fun validarCampos(email: String, password: String): String? {
    return when {
        email.isBlank() -> "El correo no puede estar vacío"
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
            "El formato del correo no es válido"
        password.isBlank() -> "La contraseña no puede estar vacía"
        password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
        else -> null
    }
}