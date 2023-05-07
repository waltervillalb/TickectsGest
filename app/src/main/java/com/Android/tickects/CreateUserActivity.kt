package com.Android.tickects

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CreateUserActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    lateinit var datePickerDialog: DatePickerDialog
    private lateinit var progressBar: ProgressBar
    private lateinit var FechaRegistro: EditText
    var hayErrores = true
    private lateinit var verificationId: String


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        auth = FirebaseAuth.getInstance()
        progressBar = findViewById(R.id.pbLoading)

        FechaRegistro = findViewById(R.id.etFechaNac)
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
        val etpassword = findViewById<EditText>(R.id.etPasswordCreateUser)
        val cbMostrarContrasena = findViewById<CheckBox>(R.id.cbMostrarContrasena)

// Configurar el CheckBox para mostrar/ocultar la contraseña
        cbMostrarContrasena.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Mostrar la contraseña
                etpassword.transformationMethod = null
            } else {
                // Ocultar la contraseña

                etpassword.transformationMethod = PasswordTransformationMethod()
            }
        }
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.validatephone_number)
        val crearUsuario = dialog.findViewById<Button>(R.id.btn_validate)
        crearUsuario.setOnClickListener {
            // Verificar si hay errores
            if (hayErrores) {
                // hay errores, mostrar mensaje y no hacer nada
                Toast.makeText(this, "Por favor, ingrese información válida en los datos ingresados anteriormente", Toast.LENGTH_SHORT).show()
            } else {
                showProgressBar()
                val nombre = findViewById<EditText>(R.id.etNombre).text.toString().trim()
                val apellido = findViewById<EditText>(R.id.etApellido).text.toString().trim()
                val telefono = findViewById<EditText>(R.id.etNumeroCelular).text.toString().trim()
                val genero = findViewById<Spinner>(R.id.spGenero).selectedItem.toString()
                val fechanac = FechaRegistro.text.toString().trim()
                val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
                val password = findViewById<EditText>(R.id.etPasswordCreateUser).text.toString().trim()
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // El usuario se creó exitosamente en Firebase Authentication
                            val user = FirebaseAuth.getInstance().currentUser
                            user?.sendEmailVerification()
                                ?.addOnCompleteListener { emailTask ->
                                    if (emailTask.isSuccessful) {
                                        // El correo electrónico de verificación se envió exitosamente
                                        Toast.makeText(this, "Se envió un correo electrónico de verificación a ${user.email}", Toast.LENGTH_SHORT).show()
                                        showProgressBarOff()
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
                            // Manejar los errores de Firebase Authentication
                            try {
                                throw task.exception!!
                            } catch (e: FirebaseAuthUserCollisionException) {
                                // El correo electrónico ya está registrado
                                Toast.makeText(
                                    this,
                                    "Este correo electrónico ya está registrado. Por favor, utiliza otro correo electrónico",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } catch (e: FirebaseAuthWeakPasswordException) {
                                // La contraseña es demasiado débil
                                Toast.makeText(
                                    this,
                                    "La contraseña debe tener al menos 6 caracteres. Por favor, intenta con otra contraseña",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } catch (e: Exception) {
                                // Otros errores
                                Toast.makeText(
                                    this,
                                    "Ocurrió un error. Por favor, intenta más tarde",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
            }
        }
        val btnEnviarRegistro = findViewById<Button>(R.id.btnContinuarRegistro)
        btnEnviarRegistro.setOnClickListener {
            dialog.show()
            val phoneNumber = findViewById<EditText>(R.id.etNumeroCelular).text.toString().trim()
            if (phoneNumber.isNotEmpty()) {
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            // Código de seguridad enviado automáticamente y verificado
                            signInWithPhoneAuthCredential(credential)
                        }

                        override fun onVerificationFailed(exception: FirebaseException) {
                            // Error al enviar el código de seguridad
                            Toast.makeText(this@CreateUserActivity, "Error: $exception", Toast.LENGTH_SHORT).show()
                        }

                        override fun onCodeSent(
                            verificationId: String,
                            token: PhoneAuthProvider.ForceResendingToken
                        ) {
                            // Código de seguridad enviado correctamente
                            this@CreateUserActivity.verificationId = verificationId
                        }
                    })
                    .build()

                // Iniciar el proceso de verificación
                PhoneAuthProvider.verifyPhoneNumber(options)
            } else {
                Toast.makeText(this, "Ingrese un número de teléfono válido", Toast.LENGTH_SHORT).show()
            }
            }
        validarCorreo()
        validarNombre()
        validarFechaNacimiento()
        validarTelefono()
        validarGenero()
        validarContrasena()
        validarApellido()
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }
    private fun showProgressBarOff() {
        progressBar.visibility = View.GONE
    }
    private fun validarContrasena() {
        val passwordEditText = findViewById<EditText>(R.id.etPasswordCreateUser)
        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Validar que no esté vacío
                if (s.toString().isEmpty()) {
                    passwordEditText.error = "Ingrese una contraseña"
                    hayErrores=true
                    return
                }
                // Validar que tenga más de 6 caracteres
                if (s.toString().length < 6) {
                    passwordEditText.error = "La contraseña debe tener al menos 6 caracteres"
                    hayErrores=true
                } else {
                    passwordEditText.error = null
                    hayErrores=false
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    private fun validarCorreo() {
        val emailEditText = findViewById<EditText>(R.id.etEmail)
        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Validar que no esté vacío
                if (s.toString().isEmpty()) {
                    emailEditText.error = "Ingrese un correo electrónico"
                    hayErrores=true
                    return
                }
                // Validar el formato del correo electrónico
                if (!Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches()) {
                    emailEditText.error = "Ingrese un correo electrónico válido"
                    hayErrores=true
                    return
                } else if(s.toString().substring(s.toString().indexOf("@")-1, s.toString().indexOf("@")) == ".") {
                    emailEditText.error = "El correo electrónico no puede contener un punto antes del @"
                    hayErrores=true
                } else {
                    emailEditText.error = null
                    hayErrores=false
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    private fun validarGenero() {
        val generoSpinner = findViewById<Spinner>(R.id.spGenero)
        val generoError = findViewById<TextInputLayout>(R.id.titulo_Genero)
        generoSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val genero = parent?.getItemAtPosition(position)?.toString() ?: ""
                if (genero == "Selecciona una opción") {
                    generoError.error = "Seleccione su género"
                    hayErrores=true
                    return
                } else {
                    generoError.error = null
                    hayErrores=false
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    private fun validarNombre() {
        val nombreEditText = findViewById<EditText>(R.id.etNombre)
        nombreEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val nombre = s.toString().trim()
                if (nombre.isEmpty()) {
                    nombreEditText.error = "El nombre no puede estar vacío"
                    hayErrores=true
                    return
                } else if (nombre.length < 5) {
                    nombreEditText.error = "El nombre debe tener al menos 5 letras"
                    hayErrores=true
                } else {
                    nombreEditText.error = null
                    hayErrores=false
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    private fun validarApellido() {
        val apeEditText = findViewById<EditText>(R.id.etApellido)
        apeEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val apellido = s.toString().trim()
                if (apellido.isEmpty()) {
                    apeEditText.error = "El Apellido no puede estar vacío"
                    hayErrores=true
                    return
                } else if (apellido.length < 5) {
                    apeEditText.error = "El nombre debe tener al menos 5 letras"
                    hayErrores=true
                } else {
                    apeEditText.error = null
                    hayErrores=false
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    private fun validarTelefono() {
        val telefonoEditText = findViewById<EditText>(R.id.etNumeroCelular)
        val telefonoRegex = Regex("^\\+595\\d{9}\$")

        telefonoEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val telefono = s.toString().trim()
                val telefonoModificado = when {
                    telefono.startsWith("0") -> "+595" + telefono.substring(1)
                    telefono.startsWith("9") -> "+595" + telefono
                    else -> telefono
                }
                if (telefonoModificado.isEmpty()) {
                    telefonoEditText.error = "El número de teléfono no puede estar vacío"
                    hayErrores=true
                    return
                } else if (!telefonoModificado.matches(telefonoRegex)) {
                    telefonoEditText.error = "El número de teléfono no es válido"
                    hayErrores=true
                } else {
                    telefonoEditText.error = null
                    hayErrores=false
                    if (telefonoModificado != telefono) {
                        telefonoEditText.setText(telefonoModificado)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No se utiliza
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No se utiliza
            }
        })
    }
    private fun validarFechaNacimiento() {
        val fechaNacimientoEditText = findViewById<EditText>(R.id.etFechaNac)
        fechaNacimientoEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val fechaNacimiento = fechaNacimientoEditText.text.toString().trim()
                if (fechaNacimiento.isEmpty()) {
                    fechaNacimientoEditText.error = "Ingrese su fecha de nacimiento"
                    hayErrores=true
                    return
                }
                val formatter = SimpleDateFormat("dd/MM/yyyy")
                val fechaNacimientoDate = try {
                    formatter.parse(fechaNacimiento)
                    //Si la fecha es válida, no es necesario guardar el valor devuelto por parse()
                    //Simplemente podemos pasar al siguiente bloque de código
                } catch (e: Exception) {
                    null
                }
                if (fechaNacimientoDate == null) {
                    fechaNacimientoEditText.error = "Ingrese una fecha de nacimiento válida en formato dd/MM/yyyy"
                    hayErrores=true
                } else {
                    val hoy = Calendar.getInstance().time
                    val edad = calcularEdad(fechaNacimientoDate, hoy)
                    if (edad < 18) {
                        fechaNacimientoEditText.error = "Debe ser mayor de edad para registrarse"
                        hayErrores=true
                    } else {
                        fechaNacimientoEditText.error = null
                        hayErrores=false
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    private fun calcularEdad(fechaNacimiento: Date, hoy: Date): Int {
        val diffInMillis = hoy.time - fechaNacimiento.time
        val edadInMillis = diffInMillis / 31557600000L //milisegundos en un año
        return edadInMillis.toInt()
    }
       private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Verificación exitosa, realizar las acciones necesarias
                        Toast.makeText(this, "Verificación exitosa", Toast.LENGTH_SHORT).show()
                    } else {
                        // Error al verificar el código de seguridad
                        Toast.makeText(this, "Error al verificar el código", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }



