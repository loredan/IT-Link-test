package ru.loredan13.it_link_test.presentation.ui.screen.image

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.loredan13.it_link_test.domain.repository.ImageRepository
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ImageViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    imageRepository: ImageRepository
) : ViewModel() {
    val imageList = imageRepository.imageList

    suspend fun saveImageForSharing(bitmap: Bitmap): Uri {
        val folder = context.cacheDir.resolve("shared_images")
        folder.mkdirs()
        val file = folder.resolve("${LocalDateTime.now()}.png")
        withContext(Dispatchers.IO) {
            file.outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
        return FileProvider.getUriForFile(context, "ru.loredan13.it_link_test.fileprovider", file)
    }
}