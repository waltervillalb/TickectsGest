package com.Android.tickects

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ContraRecuperar : AppCompatActivity() {
    private lateinit var btnEnviar: Button
    private lateinit var txtRecuperarEmail: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contra_recuperar)

        btnEnviar = findViewById(R.id.btn_recuContra)
        txtRecuperarEmail = findViewById(R.id.recu_emailText)
        btnEnviar.setOnClickListener {
            val emailAddres = txtRecuperarEmail.text.toString().trim()

            Firebase.auth.sendPasswordResetEmail(emailAddres).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Toast.makeText(this,"Correo Enviado", Toast.LENGTH_SHORT).show()
                    backHome()
                }
            }
        }
    }

    private fun backHome() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

}