package com.example.examendb

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.examendb.ui.theme.ExamenDBTheme
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class CRUDActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val usuario = intent.getStringExtra("user") ?: ""

        setContent {
            ExamenDBTheme {
                AlumnosScreen(
                    bueltan = { finish() }
                )
            }
        }
    }
}

data class Alumno(
    val codigo: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val fecha: String = "",
    val edad: Int = 0
)

@Composable
fun AlumnosScreen(bueltan: () -> Unit) {
    val context = LocalContext.current

    var codigo by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var resultados by remember { mutableStateOf("Aquí se mostrarán los resultados") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text("Código")
        OutlinedTextField(
            value = codigo,
            onValueChange = { codigo = it },
            placeholder = { Text("Campo del código") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Campo 2")
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            placeholder = { Text("Campo de Texto 2") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Campo 3")
        OutlinedTextField(
            value = apellido,
            onValueChange = { apellido = it },
            placeholder = { Text("Campo de Texto 3") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Fecha")
        OutlinedTextField(
            value = fecha,
            onValueChange = { fecha = it },
            placeholder = { Text("dd/MM/yyyy") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Campo Numérico")
        OutlinedTextField(
            value = edad,
            onValueChange = { edad = it },
            placeholder = { Text("Campo Numérico") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ERAKUTSI + ITZULI en la misma fila
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { Erakutsi { resultados = it } },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Mostrar todo")
            }

            Button(
                onClick = { bueltan() },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Volver")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // BILATU
        Button(
            onClick = {
                val error = validarBuscar(nombre)
                if (error != null) {
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                } else {
                    Bilatu(nombre.trim()) { resultados = it }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Buscar por nombre")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // GORDE
        Button(
            onClick = {
                val error = validarAlumno(codigo, nombre, apellido, fecha, edad)
                if (error != null) {
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                } else {
                    Gorde(
                        Alumno(codigo.trim(), nombre.trim(), apellido.trim(), fecha.trim(), edad.trim().toInt()),
                        context
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Insertar")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ALDATU
        Button(
            onClick = {
                val error = validarAlumno(codigo, nombre, apellido, fecha, edad)
                if (error != null) {
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                } else {
                    Aldatu(
                        Alumno(codigo.trim(), nombre.trim(), apellido.trim(), fecha.trim(), edad.trim().toInt()),
                        context
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Modificar")
        }
        Spacer(modifier = Modifier.height(8.dp))

        // LIMPIAR
        Button(
            onClick = {
                limpiarCampos { c, n, a, f, e, r ->
                    codigo = c
                    nombre = n
                    apellido = a
                    fecha = f
                    edad = e
                    resultados = r
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Limpiar")
        }
        Spacer(modifier = Modifier.height(8.dp))

        // EZABATU
        Button(
            onClick = {
                val error = validarEliminar(codigo)
                if (error != null) {
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                } else {
                    Ezabatu(codigo.trim(), context)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Borrar")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(resultados)


    }

}

// ================= FILTROS / VALIDACIONES =================

// Insertar y Modificar: todos los campos
fun validarAlumno(
    codigo: String,
    nombre: String,
    apellido: String,
    fecha: String,
    edad: String
): String? {
    val edadNum = edad.trim().toIntOrNull()
    return when {
        codigo.isBlank() -> "El código no puede estar vacío"
        nombre.isBlank() -> "El nombre no puede estar vacío"
        apellido.isBlank() -> "El apellido no puede estar vacío"
        fecha.isBlank() -> "La fecha no puede estar vacía"
        !fechaValida(fecha.trim()) -> "La fecha no es válida (formato dd/MM/yyyy)"
        edad.isBlank() -> "La edad no puede estar vacía"
        edadNum == null -> "La edad debe ser un número"
        edadNum < 0 || edadNum > 120 -> "La edad debe estar entre 0 y 120"
        else -> null
    }
}

// Buscar: solo el campo por el que se busca
fun validarBuscar(nombre: String): String? {
    return when {
        nombre.isBlank() -> "Escribe un nombre para buscar"
        else -> null
    }
}

// Borrar: solo el código
fun validarEliminar(codigo: String): String? {
    return when {
        codigo.isBlank() -> "Escribe el código del alumno a borrar"
        else -> null
    }
}

// Comprueba formato Y que la fecha exista (31/02/2024 no vale)
fun fechaValida(fecha: String): Boolean {
    return try {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        formato.isLenient = false
        formato.parse(fecha)
        Regex("""\d{2}/\d{2}/\d{4}""").matches(fecha)
    } catch (e: Exception) {
        false
    }
}

// ================= FIRESTORE =================
private const val COLECCION = "alumnos"

fun Gorde(alumno: Alumno, context: Context) {
    val db = FirebaseFirestore.getInstance()

    db.collection(COLECCION)
        .whereEqualTo("codigo", alumno.codigo)
        .get()
        .addOnSuccessListener { docs ->
            if (!docs.isEmpty) {
                Toast.makeText(context, "Ya existe un alumno con ese código", Toast.LENGTH_SHORT).show()
            } else {
                db.collection(COLECCION)
                    .add(alumno)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Alumno guardado", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error al buscar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

fun Erakutsi(resultado: (String) -> Unit) {
    FirebaseFirestore.getInstance()
        .collection(COLECCION)
        .get()
        .addOnSuccessListener { docs ->
            if (docs.isEmpty) {
                resultado("No hay documentos")
            } else {
                var datos = ""
                for (doc in docs) {
                    datos += "${doc.getString("codigo")} | ${doc.getString("nombre")} " +
                            "${doc.getString("apellido")} | ${doc.getString("fecha")} | " +
                            "${doc.getLong("edad")}\n"
                }
                resultado(datos)
            }
        }
        .addOnFailureListener { e ->
            resultado("Error: ${e.message}")
        }
}

fun Bilatu(nombre: String, resultado: (String) -> Unit) {
    FirebaseFirestore.getInstance()
        .collection(COLECCION)
        .whereEqualTo("nombre", nombre)
        .get()
        .addOnSuccessListener { docs ->
            if (docs.isEmpty) {
                resultado("No se encontró ningún alumno con ese nombre")
            } else {
                var datos = ""
                for (doc in docs) {
                    datos += "${doc.getString("codigo")} | ${doc.getString("nombre")} " +
                            "${doc.getString("apellido")} | ${doc.getString("fecha")} | " +
                            "${doc.getLong("edad")}\n"
                }
                resultado(datos)
            }
        }
        .addOnFailureListener { e ->
            resultado("Error: ${e.message}")
        }
}

fun Aldatu(alumno: Alumno, context: Context) {
    val db = FirebaseFirestore.getInstance()

    db.collection(COLECCION)
        .whereEqualTo("codigo", alumno.codigo)
        .get()
        .addOnSuccessListener { docs ->
            if (docs.isEmpty) {
                Toast.makeText(context, "No se encontró el alumno", Toast.LENGTH_SHORT).show()
            } else {
                for (doc in docs) {
                    db.collection(COLECCION)
                        .document(doc.id)
                        .update(
                            "nombre", alumno.nombre,
                            "apellido", alumno.apellido,
                            "fecha", alumno.fecha,
                            "edad", alumno.edad
                        )
                        .addOnSuccessListener {
                            Toast.makeText(context, "Alumno actualizado", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error al buscar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

fun Ezabatu(codigo: String, context: Context) {
    val db = FirebaseFirestore.getInstance()

    db.collection(COLECCION)
        .whereEqualTo("codigo", codigo)
        .get()
        .addOnSuccessListener { docs ->
            if (docs.isEmpty) {
                Toast.makeText(context, "No se encontró el alumno", Toast.LENGTH_SHORT).show()
            } else {
                for (doc in docs) {
                    db.collection(COLECCION)
                        .document(doc.id)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Alumno borrado", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error al buscar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}
// ================= LIMPIAR =================
fun limpiarCampos(
    onLimpiar: (codigo: String, nombre: String, apellido: String, fecha: String, edad: String, resultados: String) -> Unit
) {
    onLimpiar("", "", "", "", "", "Aquí se mostrarán los resultados")
}
