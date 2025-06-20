package com.example.voyago

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voyago.ui.theme.DarkGreen20
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.absoluteValue

class SignUpActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        setContent {
            SignUpScreen(firebaseAuth = firebaseAuth, firestore = firestore)
        }
    }
}

@Composable
fun SignUpScreen(firebaseAuth: FirebaseAuth, firestore: FirebaseFirestore) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGreen20,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Sign up to get started with Voyago",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                nameError = ""
            },
            label = { Text("Full Name") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Name Icon"
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            ),
            isError = nameError.isNotEmpty(),
            supportingText = if (nameError.isNotEmpty()) {
                { Text(nameError, color = MaterialTheme.colorScheme.error) }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = ""
            },
            label = { Text("Email") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email Icon"
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            isError = emailError.isNotEmpty(),
            supportingText = if (emailError.isNotEmpty()) {
                { Text(emailError, color = MaterialTheme.colorScheme.error) }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = ""
            },
            label = { Text("Password") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password Icon"
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = { isPasswordVisible = !isPasswordVisible }
                ) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Outlined.CheckCircle else Icons.Filled.CheckCircle,
                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            isError = passwordError.isNotEmpty(),
            supportingText = if (passwordError.isNotEmpty()) {
                { Text(passwordError, color = MaterialTheme.colorScheme.error) }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                confirmPasswordError = ""
            },
            label = { Text("Confirm Password") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Confirm Password Icon"
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }
                ) {
                    Icon(
                        imageVector = if (isConfirmPasswordVisible) Icons.Outlined.CheckCircle else Icons.Filled.CheckCircle,
                        contentDescription = if (isConfirmPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            isError = confirmPasswordError.isNotEmpty(),
            supportingText = if (confirmPasswordError.isNotEmpty()) {
                { Text(confirmPasswordError, color = MaterialTheme.colorScheme.error) }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Button(
            onClick = {
                var hasError = false

                if (name.isBlank()) {
                    nameError = "Enter your full name"
                    hasError = true
                } else if (name.length < 2) {
                    nameError = "Name must be at least 2 characters"
                    hasError = true
                }

                if (email.isBlank()) {
                    emailError = "Enter an email address"
                    hasError = true
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailError = "Enter a valid email address"
                    hasError = true
                }

                if (password.isBlank()) {
                    passwordError = "Enter a password"
                    hasError = true
                } else if (password.length < 6) {
                    passwordError = "Password must be at least 6 characters"
                    hasError = true
                }

                if (confirmPassword.isBlank()) {
                    confirmPasswordError = "Confirm your password"
                    hasError = true
                } else if (password != confirmPassword) {
                    confirmPasswordError = "Passwords don't match"
                    hasError = true
                }

                if (!hasError) {
                    isLoading = true

                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = firebaseAuth.currentUser
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build()

                                user?.updateProfile(profileUpdates)
                                    ?.addOnCompleteListener { profileTask ->
                                        if (profileTask.isSuccessful) {
                                            // Creazione del documento profile su Firestore
                                            val uid = user.uid
                                            val nameParts = name.split(" ")
                                            val firstName = nameParts.firstOrNull() ?: ""
                                            val lastName = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else ""

                                            val profileRef = firestore.collection("profiles").document(uid)

                                            val dataProfile = DataProfile(
                                                id = uid.hashCode().absoluteValue,
                                                name = firstName,
                                                surname = lastName,
                                                username = "",
                                                email = email,
                                                img = "",
                                                phone = "",
                                                instagram = "",
                                                facebook = "",
                                                memberSince = java.time.LocalDate.now().toString(),
                                                responseRate = "100%",
                                                responseTime = "Fast",
                                                lastSeen = java.time.LocalDate.now().toString()
                                            )

                                            profileRef.set(dataProfile)
                                                .addOnSuccessListener {
                                                    isLoading = false
                                                    Log.d("Firestore", "User profile created successfully")
                                                    Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT).show()

                                                    val intent = Intent(context, SignInActivity::class.java)
                                                    context.startActivity(intent)
                                                    (context as AppCompatActivity).finish()
                                                }
                                                .addOnFailureListener { e ->
                                                    isLoading = false
                                                    Log.e("Firestore", "Failed to create profile", e)
                                                    Toast.makeText(context, "Account created but profile creation failed", Toast.LENGTH_SHORT).show()

                                                    // Naviga comunque alla MainActivity anche se la creazione del profilo fallisce
                                                    val intent = Intent(context, MainActivity::class.java)
                                                    context.startActivity(intent)
                                                    (context as AppCompatActivity).finish()
                                                }

                                        } else {
                                            isLoading = false
                                            Toast.makeText(context, "Account created but profile update failed", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                isLoading = false
                                val errorMessage = when (task.exception) {
                                    is FirebaseAuthUserCollisionException -> "An account with this email already exists"
                                    is FirebaseAuthWeakPasswordException -> "Password is too weak"
                                    else -> "Sign up failed: ${task.exception?.message}"
                                }
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            }
                        }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading,
            colors = ButtonColors(
                containerColor = DarkGreen20,
                contentColor = Color.White,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.White
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = DarkGreen20,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Create Account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                val intent = Intent(context, SignInActivity::class.java)
                context.startActivity(intent)
                (context as AppCompatActivity).finish()
            }
        ) {
            Text(
                text = "Already have an account? Sign In",
                color = DarkGreen20
            )
        }
    }
}