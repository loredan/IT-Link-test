package ru.loredan13.it_link_test.presentation.ui.screen.image

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.loredan13.it_link_test.domain.repository.ImageRepository
import javax.inject.Inject

@HiltViewModel
class ImageViewModel @Inject constructor(imageRepository: ImageRepository) : ViewModel() {
    val imageList = imageRepository.imageList
}