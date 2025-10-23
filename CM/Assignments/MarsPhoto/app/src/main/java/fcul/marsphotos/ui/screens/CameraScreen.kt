package fcul.marsphotos.ui.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import fcul.marsphotos.R
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.UUID

@Composable
fun CameraScreen(
    navController: NavHostController,
    uploadPhoto: (photoUri: Uri, onComplete: () -> Unit, onFailure: () -> Unit) -> Unit
) {
    var failure by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraExecutor = rememberCameraExecutor()
    val previewView = remember { PreviewView(context) }

    if (success) {
        AlertDialog(
            onDismissRequest = {
                success = false
                               },
            title = { Text(text = stringResource(R.string.success)) },
            text = { Text(text = stringResource(R.string.photo_upload_success)) },
            confirmButton = {
                Button(onClick = {
                    success = false
                }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    if (failure) {
        AlertDialog(
            onDismissRequest = {
                failure = false
                               },
            title = { Text(text = stringResource(R.string.failure)) },
            text = { Text(text = stringResource(R.string.failure_save_photo)) },
            confirmButton = {
                Button(onClick = {
                    failure = false
                }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    DisposableEffect(key1 = Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val listener = Runnable {
            val cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases(
                context,
                lifecycleOwner,
                previewView,
                cameraProvider
            ) { ic -> imageCapture = ic }
        }
        cameraProviderFuture.addListener(listener, ContextCompat.getMainExecutor(context))
        onDispose {
            cameraProviderFuture.get().unbindAll()
            cameraExecutor.shutdown()
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        if (!isLoading) {
            AndroidView(factory = { previewView }, modifier = Modifier
                .weight(1f)
                .fillMaxSize())
        }

        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                enabled = !isLoading,
                onClick = {
                    imageCapture?.let {
                        isLoading = true
                        takePhoto(
                            uploadPhoto,
                            {
                                isLoading = false
                                success = true
                            }, {
                                isLoading = false
                                failure = true
                            }, context, it, cameraExecutor
                        )
                    }
                },
                modifier = Modifier.padding(top = 12.dp)
            )
            {
                Text(text = stringResource(R.string.take_photo))
            }
            Button(
                enabled = !isLoading,
                onClick = {
                navController.popBackStack()
            }, modifier = Modifier.padding(top = 12.dp)) {
                Text(text = stringResource(R.string.back))
            }
        }
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun rememberCameraExecutor(): ExecutorService {
    val executor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) {
        onDispose { executor.shutdown() }
    }
    return executor
}

private fun bindCameraUseCases(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    cameraProvider: ProcessCameraProvider,
    onImageCaptureReady: (ImageCapture) -> Unit
) {
    val preview = Preview.Builder().build().also {
        it.setSurfaceProvider(previewView.surfaceProvider)
    }

    val imageCapture = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .build()

    try {
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageCapture
        )
        onImageCaptureReady(imageCapture)
    } catch (exc: Exception) {
        Toast.makeText(context, "Erro a iniciar câmara: ${exc.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun takePhoto(
    uploadPhoto: (photoUri: Uri, onComplete: () -> Unit, onFailure: () -> Unit) -> Unit,
    onComplete: () -> Unit,
    onFailure: () -> Unit,
    context: Context,
    imageCapture: ImageCapture,
    executor: ExecutorService
) {
    val tempFile = File.createTempFile("${UUID.randomUUID()}", ".jpg", context.cacheDir)
    val outputOptions = OutputFileOptions.Builder(tempFile).build()

    imageCapture.takePicture(outputOptions, executor,
        object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            try {
                val fileUri: Uri = Uri.fromFile(tempFile)
                uploadPhoto(fileUri, {
                    onComplete()
                    tempFile.delete()
                }, onFailure)
            } catch (e: Exception) {
                onFailure()
                tempFile.delete()
            }
        }

        override fun onError(exception: ImageCaptureException) {
            onFailure()
            tempFile.delete()
        }
    })
}