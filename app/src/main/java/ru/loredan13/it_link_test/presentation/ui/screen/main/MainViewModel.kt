package ru.loredan13.it_link_test.presentation.ui.screen.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.loredan13.it_link_test.domain.repository.ImageRepository
import ru.loredan13.it_link_test.domain.util.launchCatching
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val imageRepository: ImageRepository) :
    ViewModel() {
    val imageList = imageRepository.imageList

    fun refresh() {
        viewModelScope.launchCatching {
            imageRepository.refresh()
        }
    }
}