package com.example.docsscanocrapp.ocr

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrResultScreen(navController: NavController) {
    val context = LocalContext.current
    val textResult = remember { mutableStateOf("") }

    val uri = navController.currentBackStackEntry
        ?.arguments
        ?.getString("imageUri")?.toUri()

    LaunchedEffect(uri) {
        if (uri != null) {
            try {
                val image = InputImage.fromFilePath(context, uri)
//                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        textResult.value = visionText.text
                    }
                    .addOnFailureListener { e ->
                        textResult.value = "인식 실패: ${e.localizedMessage}"
                    }
            } catch (e: Exception) {
                textResult.value = "이미지 처리 중 오류 발생: ${e.localizedMessage}"
            }
        } else {
            textResult.value = "이미지를 불러올 수 없습니다."
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