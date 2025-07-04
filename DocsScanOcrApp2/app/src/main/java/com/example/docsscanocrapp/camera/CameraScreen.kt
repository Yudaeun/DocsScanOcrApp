package com.example.docsscanocrapp.camera

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(navController: NavController) {
    val context = LocalContext.current
    val imageUri = remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && imageUri.value != null) {
//            navController.navigate("ocr?imageUri=${Uri.encode(imageUri.value.toString())}")
            navController.navigate("crop?imageUri=${Uri.encode(imageUri.value.toString())}")

        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imageUri.value?.let { launcher.launch(it) }
        } else {
            Toast.makeText(context, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    val photoFile = remember {
        File.createTempFile("ocr_image",".jpg", context.cacheDir).apply {
            imageUri.value = FileProvider.getUriForFile(
                context,
                "com.example.docsscanocrapp.fileprovider",
                this
            )
        }
    }

    Scaffold (
        topBar = {
            TopAppBar(title = { Text("카메라 화면") })
        }
    ){ padding ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Button(onClick = {
                permissionLauncher.launch(android.Manifest.permission.CAMERA)
            }) {
                Text("사진 촬영")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("뒤로 가기")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("home")}) {
                Text("홈으로")
            }
        }
    }
}