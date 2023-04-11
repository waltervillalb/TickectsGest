package com.Android.tickects

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class CreateUserActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    lateinit var datePickerDialog: DatePickerDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        auth = FirebaseAuth.getInstance()


        val FechaRegistro = findViewById<EditText>(R.id.text_fechCreateUser)
        FechaRegistro.setOnClickListener {
            val c: Calendar = Calendar.getInstance()
            val mYear: Int = c.get(Calendar.YEAR)

            val mMonth: Int = c.get(Calendar.MONTH)

            val mDay: Int = c.get(Calendar.DAY_OF_MONTH)
            datePickerDialog = DatePickerDialog(
                this,
                { view, year, monthOfYear, dayOfMonth ->
                    FechaRegistro.setText(dayOfMonth.toString() + "/" + (monthOfYear + 1) + "/" + year)
                }, mYear, mMonth, mDay
            )
            datePickerDialog.show()
        }

        val btnEnviarRegistro = findViewById<Button>(R.id.btn_createUser)
        btnEnviarRegistro.setOnClickListener {
            val nombre = findViewById<EditText>(R.id.text_NamesCreateUser).text.toString().trim()
            val apellido = findViewById<EditText>(R.id.text_ApellidoCreateUser).text.toString().trim()
            val telefono = findViewById<EditText>(R.id.text_NumeroCeluluarCreateUser).text.toString().trim()
            val genero = findViewById<EditText>(R.id.text_GeneroCreateUser).text.toString().trim()
            val fechanac = FechaRegistro.text.toString().trim()
            val email = findViewById<EditText>(R.id.text_EmailCreateUser).text.toString().trim()
            val password = findViewById<EditText>(R.id.text_PasswordCreateUser).text.toString().trim()

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // El usuario se creó exitosamente en Firebase Authentication
                        val user = FirebaseAuth.getInstance().currentUser
                        user?.sendEmailVerification()
                            ?.addOnCompleteListener { emailTask ->
                                if (emailTask.isSuccessful) {
                                    // El correo electrónico de verificación se envió exitosamente
                                    Toast.makeText(
                                        this,
                                        "Se envió un correo electrónico de verificación a ${user.email}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    // Ocurrió un error al enviar el correo electrónico de verificación
                                    Toast.makeText(
                                        this,
                                        "Error al enviar el correo electrónico de verificación",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        // El usuario se creó exitosamente en Firebase Authentication

                        val uid = user!!.uid
                        val userData = hashMapOf(
                            "nombre" to nombre,
                            "apellido" to apellido,
                            "telefono" to telefono,
                            "genero" to genero,
                            "fechanacimiento" to fechanac,
                            "correo" to email
                        )

                        // agregar nuevo documento generando el ID ccorrespondiente
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(uid).set(userData)
                            .addOnSuccessListener {
                                // Los datos se guardaron exitosamente en Firestore
                                Toast.makeText(
                                    this,
                                    "Usuario registrado exitosamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener {
                                // Ocurrió un error al guardar los datos en Firestore
                                Toast.makeText(
                                    this,
                                    "Error al registrar el usuario",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        // Ocurrió un error al crear el usuario en Firebase Authentication
                        Toast.makeText(this, "Error al crear el usuario", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
