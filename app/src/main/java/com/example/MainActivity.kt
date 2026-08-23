package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.AuthRepository
import com.example.ui.auth.AuthViewModel
import com.example.ui.navigation.MainContainer
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getInstance(applicationContext)
        val authRepository = AuthRepository(
            userDao = db.userDao(),
            addressDao = db.addressDao(),
            merchantProfileDao = db.merchantProfileDao(),
            driverProfileDao = db.driverProfileDao(),
            sessionDao = db.sessionDao()
        )

        val authViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(authRepository) as T
                }
            }
        )[AuthViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainContainer(authViewModel = authViewModel)
            }
        }
    }
}
