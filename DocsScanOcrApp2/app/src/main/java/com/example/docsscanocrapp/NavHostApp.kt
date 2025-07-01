package com.example.docsscanocrapp

import android.net.Uri
import android.widget.Toast
import androidx.navigation.navArgument
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File

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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("문서 스캔 홈") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { navController.navigate("camera") }) {
                Text("사진 촬영하기")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(navController: NavController) {
    val context = LocalContext.current
    val imageUri = remember { mutableStateOf<Uri?>(null) }


    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && imageUri.value != null) {
            navController.navigate("ocr?imageUri=${Uri.encode(imageUri.value.toString())}")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrResultScreen(navController: NavController) {
    val context = LocalContext.current
    val imageUriString = remember { mutableStateOf("") }
    val textResult = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val uri = navController.currentBackStackEntry
            ?.arguments
            ?.getString("imageUri")
            ?.let {  Uri.parse(it) }

        if (uri != null) {

            val file = File(uri.path ?: "")
            if (!file.exists() || file.length() == 0L) {
                textResult.value = "이미지 파일이 손상되었거나 존재하지 않습니다."
                return@LaunchedEffect
            }

            val image = InputImage.fromFilePath(context, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val result = recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    textResult.value = visionText.text
                }
                .addOnFailureListener { e ->
                    textResult.value = "인식 실패: ${e.localizedMessage}"
                }
        }
    }

    Scaffold (
        topBar = {
            TopAppBar(title = { Text("OCR 결과 확인") })
        }
    ){ padding ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ){
            Text("추출된 텍스트: ", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(10.dp))
            Text(textResult.value)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("뒤로가기")
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { navController.navigate("home")}) {
                Text("홈으로")
            }
        }
    }
}