package com.example.nutritrack.ui.addfood

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.nutritrack.NutriTrackApp
import com.example.nutritrack.databinding.ActivityAddFoodBinding
import com.example.nutritrack.utils.ImageUtils

class AddFoodActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddFoodBinding
    private var selectedImageUri: Uri? = null
    private var capturedBitmap: Bitmap? = null

    private val viewModel: AddFoodViewModel by viewModels {
        AddFoodViewModelFactory((application as NutriTrackApp).repository)
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                val bmp = ImageUtils.uriToBitmap(contentResolver, uri)
                selectedImageUri = null
                capturedBitmap = bmp
                binding.ivFoodPreview.setImageBitmap(bmp)
            } catch (e: Exception) {
                    showError("Error al cargar imagen: ${e.message}")
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            pickImageLauncher.launch("image/*")
        } else {
                showError("Permiso denegado")
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            takePictureLauncher.launch(null)
        } else {
                showError("Permiso de cámara denegado")
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            selectedImageUri = null
            capturedBitmap = bitmap
            binding.ivFoodPreview.setImageBitmap(bitmap)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPickImage.setOnClickListener {
            requestGalleryPermissionAndPick()
        }

        binding.btnTakePhoto.setOnClickListener {
            requestCameraPermissionAndCapture()
        }

        binding.btnAnalyzeSave.setOnClickListener {
            val description = binding.etDescription.text.toString().trim()
            val imageUri = selectedImageUri
            val bitmap = capturedBitmap

            if (imageUri == null && bitmap == null) {
                viewModel.analyzeAndSaveText(description)
            } else if (bitmap != null) {
                try {
                    val base64 = ImageUtils.bitmapToBase64(bitmap)
                    viewModel.analyzeAndSaveImage(description, base64, null)
                } catch (e: Exception) {
                        showError(e.message ?: "Error con la foto")
                }
            } else {
                try {
                    val selectedBitmap = ImageUtils.uriToBitmap(contentResolver, imageUri!!)
                    val base64 = ImageUtils.bitmapToBase64(selectedBitmap)
                    viewModel.analyzeAndSaveImage(description, base64, imageUri.toString())
                } catch (e: Exception) {
                        showError(e.message ?: "Error con la imagen")
                }
            }
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
            binding.btnAnalyzeSave.isEnabled = !isLoading
        }

        viewModel.error.observe(this) { error ->
            if (!error.isNullOrBlank()) {
                    showError(error)
            }
        }

        viewModel.saved.observe(this) { saved ->
            if (saved) {
                Toast.makeText(this, "Comida guardada", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

        private fun showError(message: String) {
            binding.tvError.text = message
            binding.tvError.visibility = View.VISIBLE
        }

    private fun requestGalleryPermissionAndPick() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                pickImageLauncher.launch("image/*")
            }
            else -> permissionLauncher.launch(permission)
        }
    }

    private fun requestCameraPermissionAndCapture() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                takePictureLauncher.launch(null)
            }
            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}
