package com.Android.tickects

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class CreateUserActivity : AppCompatActivity() {
    private lateinit var firebasAuth: FirebaseAuth

    var txtFecha: EditText?= null
    var btnfecha: ImageButton?= null
    var editfecha: DatePicker?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)

        firebasAuth=Firebase.auth

        //varables para la creación de datos
        val txtNomNew: TextView = findViewById(R.id.text_Names_CreateUser)
        val txtApeNew: TextView = findViewById(R.id.text_ApellidoCreateUser)
        val txtEmaiNew: TextView = findViewById(R.id.text_correoEmail)
        val txtpassnew: TextView= findViewById(R.id.editTextTextPassword1)
        val btnCreateNewUs: Button= findViewById(R.id.btn_createUser)
        //val dateNaci:DatePicker = findViewById(R.id.editTextDate)

        btnCreateNewUs.setOnClickListener (){
            createAccount(txtEmaiNew.text.toString(), txtpassnew.text.toString())
        }



        //variables para la creacio del calendario
      txtFecha=findViewById(R.id.editTextDate)
        btnfecha=findViewById(R.id.imageButton)
        editfecha=findViewById(R.id.dpFecha)




        //cambiar color
        val datePicker = findViewById<DatePicker>(R.id.dpFecha)
        datePicker.setBackgroundColor(ContextCompat.getColor(this, R.color.white))



        //envía el text la fecha
        txtFecha?.setText(getFecha())

        editfecha?.setOnDateChangedListener{
            editfecha,anho,mes,dia->
            txtFecha?.setText(getFecha())
            editfecha.visibility=View.GONE
        }
    }




    //settea la fecha
    fun getFecha(): String{
        var dia=editfecha?.dayOfMonth.toString().padStart(2,'0')
        var mes=(editfecha!!.month+1).toString().padStart(2,'0')
        var anho= editfecha?.year.toString().padStart(4,'0')
    return dia+"/"+mes+"/"+anho




    }






//muestra el calendario
    fun muestraCalendario(view: View){
        editfecha?.visibility=View.VISIBLE
    }



    private fun createAccount(email: String, password: String){
            firebasAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this){
                task ->
                if (task.isSuccessful){
                    Toast.makeText(baseContext,"Cuenta Creada correctamente",Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(baseContext,"Algo salió mal "+task.exception, Toast.LENGTH_SHORT).show()

                }
            }
    }
    }
