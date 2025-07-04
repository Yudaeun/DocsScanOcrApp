package com.example.docsscanocrapp.camera

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropScreen(
    navController: NavController,
    imageUri: Uri
) {
    val context = LocalContext.current
    val bitmap = remember(imageUri) {
        MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
    }

    var startOffset by remember { mutableStateOf(Offset.Zero) }
    var endOffset by remember { mutableStateOf(Offset.Zero) }

    val cropRect = remember(startOffset, endOffset) {
        androidx.compose.ui.geometry.Rect(
            min(startOffset.x, endOffset.x),
            min(startOffset.y, endOffset.y),
            max(startOffset.x, endOffset.x),
            max(startOffset.y, endOffset.y)
        )
    }

    var croppedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(cropRect) {
        if (cropRect.width > 10 && cropRect.height > 10) {
            croppedBitmap = Bitmap.createBitmap(
                bitmap,
                cropRect.left.toInt(),
                cropRect.top.toInt(),
                cropRect.width.toInt(),
                cropRect.height.toInt()
            )
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("영역지정") }) },
        bottomBar = {
            Button(
                onClick = {
                    if (cropRect.width < 10 || cropRect.height < 10) {
                        navController.navigate("ocr?imageUri=${Uri.encode(imageUri.toString())}")
                    } else {
                        croppedBitmap?.let { bmp ->
                            val file = File(context.cacheDir, "cropped.jpg")
                            FileOutputStream(file).use { out ->
                                bmp.compress(Bitmap.CompressFormat.JPEG, 100, out) // TODO: 개선 필요(메모리 잔류 위험)
                            }
                            val uri = FileProvider.getUriForFile(
                                context,
                                "com.example.docsscanocrapp.fileprovider",
                                file
                            )
                            navController.navigate("ocr?imageUri=${Uri.encode(imageUri.toString())}")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crop한 영역 OCR 인식 시작")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ){
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.LightGray,
                        contentColor = Color.Black
                    ) ) {
                    Text("뒤로가기")
                }
            }

        }

    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset -> startOffset = offset; endOffset = offset },
                        onDrag = { _, dragAmount -> endOffset += dragAmount }
                    )
                }
        )

        {
            Image(bitmap = bitmap.asImageBitmap(), contentDescription = null)

            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    color = Color.Red.copy(alpha = 0.5f),
                    topLeft = Offset(
                        min(startOffset.x, endOffset.x),
                        min(startOffset.y, endOffset.y)
                    ),
                    size = Size(
                        abs(endOffset.x - startOffset.x),
                        abs(endOffset.y - startOffset.y)
                    ),
                    style = Stroke(width = 3f)
                )
            }
        }
    }

}