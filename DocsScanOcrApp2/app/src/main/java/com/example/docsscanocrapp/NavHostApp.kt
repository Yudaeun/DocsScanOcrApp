package com.example.docsscanocrapp

import android.net.Uri
import android.widget.Toast
import androidx.navigation.navArgument
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.docsscanocrapp.camera.CameraScreen
import com.example.docsscanocrapp.camera.CropScreen
import com.example.docsscanocrapp.main.HomeScreen
import com.example.docsscanocrapp.ocr.OcrResultScreen

@Composable
fun NavHostApp() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "home"){
        composable("home") {
            HomeScreen(navController)
        }
        composable("camera") {
            CameraScreen(navController)
        }
        composable("ocr") {
            OcrResultScreen(navController)
        }
        composable("ocr?imageUri={imageUri}", arguments = listOf(
            navArgument("imageUri") { nullable = true }
        )) {
            OcrResultScreen(navController)
        }
        composable("crop?imageUri={imageUri}", arguments = listOf(
            navArgument("imageUri") { nullable = true }
        )) { backStackEntry ->
            val uriString = backStackEntry.arguments?.getString("imageUri")
            val uri = uriString?.let { Uri.parse(it) }
            if (uri != null) {
                CropScreen(navController, uri)
            } else {
                Toast.makeText(LocalContext.current, "error!", Toast.LENGTH_SHORT).show()

            }
        }
    }
}
