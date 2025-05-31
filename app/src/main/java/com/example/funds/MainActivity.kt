package com.example.funds

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.example.funds.presentation.pages.mainscreen.MainScreen
import com.example.funds.presentation.ui.theme.FundsTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                100
            )
        }
        setContent {
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = true
            val navigateToRaiseId = remember { mutableStateOf<Int?>(null) }

            // Set system bar color
            SideEffect {
                systemUiController.setSystemBarsColor(
                    color = Color.Black,
                    darkIcons = useDarkIcons
                )
            }

            // Read the dynamic link and update the state
            LaunchedEffect(Unit) {
                Firebase.dynamicLinks
                    .getDynamicLink(intent)
                    .addOnSuccessListener { pendingDynamicLinkData ->
                        val deepLink = pendingDynamicLinkData?.link
                        Log.d("DynamicLink", "Received dynamic link: $deepLink")

                        val raiseId = deepLink?.getQueryParameter("id")?.toIntOrNull()
                        Log.d("DynamicLink", "Parsed raiseId: $raiseId")

                        navigateToRaiseId.value = raiseId
                    }
                    .addOnFailureListener {
                        Log.e("DynamicLink", "Failed to retrieve dynamic link", it)
                    }
            }

            FundsTheme {
                MainScreen(navigateToRaiseId = navigateToRaiseId.value)
            }
        }
    }
}
