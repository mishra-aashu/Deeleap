package com.yourname.videoeditor.ui.screens.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.videoeditor.data.datasource.MediaDataSource
import com.yourname.videoeditor.domain.model.AudioItem
import com.yourname.videoeditor.domain.model.MediaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GalleryViewModel(
    private val mediaDataSource: MediaDataSource
) : ViewModel() {

    data class UIState(
        val permissionGranted: Boolean = false,
        val selectedTabIndex: Int = 0,
        val searchQuery: String = "",
        val allMedia: List<MediaItem> = emptyList(),
        val filteredMediaList: List<MediaItem> = emptyList(),
        val isLoading: Boolean = false
    )

    private val _uiState = MutableStateFlow(UIState())
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    fun updatePermissionState(granted: Boolean) {
        _uiState.update { it.copy(permissionGranted = granted) }
        if (granted) {
            loadMediaForTab(_uiState.value.selectedTabIndex)
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index, searchQuery = "") }
        loadMediaForTab(index)
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { 
            val filtered = applySearch(it.allMedia, query)
            it.copy(searchQuery = query, filteredMediaList = filtered)
        }
    }

    private fun loadMediaForTab(tab: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val media = when (tab) {
                0 -> mediaDataSource.getImages()
                1 -> mediaDataSource.getVideos()
                2 -> mediaDataSource.getAudios()
                else -> emptyList()
            }
            _uiState.update { 
                it.copy(
                    allMedia = media, 
                    filteredMediaList = applySearch(media, it.searchQuery),
                    isLoading = false
                ) 
            }
        }
    }

    private fun applySearch(media: List<MediaItem>, query: String): List<MediaItem> {
        if (query.isBlank()) return media
        return media.filter { item ->
            when (item) {
                is AudioItem -> {
                    item.displayName.contains(query, ignoreCase = true) ||
                    (item.artist?.contains(query, ignoreCase = true) ?: false) ||
                    (item.album?.contains(query, ignoreCase = true) ?: false)
                }
                else -> item.displayName.contains(query, ignoreCase = true)
            }
        }
    }
}
