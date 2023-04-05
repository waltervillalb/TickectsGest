package com.Android.tickects

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeActivity : AppCompatActivity() {



    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)



        auth = Firebase.auth

        val currentUser = auth.currentUser
        val uid = currentUser!!.uid
        val db = Firebase.firestore

        db.collection("users").document(uid).get().addOnSuccessListener { documentSnapshot ->
            val apellido = documentSnapshot.get("apellido") as String?
            val correo = documentSnapshot.get("correo") as String?
            val telefono = documentSnapshot.get("telefono") as String?
            val genero = documentSnapshot.get("genero") as String?
            val fechaNacimiento = documentSnapshot.get("fechanacimiento") as String?

            runOnUiThread {
                findViewById<TextView>(R.id.eTxt_ApellidoUser).text = "Apellido: $apellido"
                findViewById<TextView>(R.id.eTxt_CorreoUser).text = "Correo: $correo"
                findViewById<TextView>(R.id.eTxt_TelefonoUser).text = "Telefono: $telefono"
                findViewById<TextView>(R.id.eTxt_GeneroUser).text = "Genero: $genero"
                findViewById<TextView>(R.id.eTxtfechaUser).text =
                    "Fecha de nacimiento: $fechaNacimiento"

            }
        }
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            cerrarSesion()
        }
    }
    private fun cerrarSesion() {
        auth.signOut()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            reload()
        } else {

        }
    }

    private fun reload() {

    }

}



