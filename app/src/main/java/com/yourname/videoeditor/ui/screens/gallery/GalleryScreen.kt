package com.yourname.videoeditor.ui.screens.gallery

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourname.videoeditor.domain.model.AudioItem
import com.yourname.videoeditor.domain.model.ImageItem
import com.yourname.videoeditor.domain.model.MediaItem
import com.yourname.videoeditor.domain.model.VideoItem
import com.yourname.videoeditor.ui.screens.gallery.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onItemSelected: (MediaItem) -> Unit,
    onBack: () -> Unit,
    viewModel: GalleryViewModel // Expecting this to be provided by a factory or DI
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        viewModel.updatePermissionState(allGranted)
    }

    LaunchedEffect(Unit) {
        if (!PermissionHelper.arePermissionsGranted(context)) {
            permissionLauncher.launch(PermissionHelper.getRequiredPermissions())
        } else {
            viewModel.updatePermissionState(true)
        }
    }

    var previewItem by remember { mutableStateOf<MediaItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gallery") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (!state.permissionGranted) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Storage permission is required to access media")
                        Button(
                            onClick = { permissionLauncher.launch(PermissionHelper.getRequiredPermissions()) },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    TabRow(selectedTabIndex = state.selectedTabIndex) {
                        Tab(
                            selected = state.selectedTabIndex == 0,
                            onClick = { viewModel.selectTab(0) },
                            text = { Text("Images") }
                        )
                        Tab(
                            selected = state.selectedTabIndex == 1,
                            onClick = { viewModel.selectTab(1) },
                            text = { Text("Videos") }
                        )
                        Tab(
                            selected = state.selectedTabIndex == 2,
                            onClick = { viewModel.selectTab(2) },
                            text = { Text("Audio") }
                        )
                    }

                    if (state.selectedTabIndex == 2) {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            placeholder = { Text("Search audio...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true
                        )
                    }

                    if (state.isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        when (state.selectedTabIndex) {
                            0 -> ImageGrid(
                                images = state.filteredMediaList,
                                onItemSelected = { previewItem = it }
                            )
                            1 -> VideoGrid(
                                videos = state.filteredMediaList,
                                onItemSelected = { previewItem = it }
                            )
                            2 -> AudioList(
                                audios = state.filteredMediaList,
                                onItemSelected = { previewItem = it }
                            )
                        }
                    }
                }
            }

            // Preview Components
            previewItem?.let { item ->
                when (item) {
                    is ImageItem -> {
                        ImagePreviewSheet(
                            imageItem = item,
                            onDismiss = { previewItem = null },
                            onSelect = {
                                onItemSelected(it)
                                previewItem = null
                            }
                        )
                    }
                    is VideoItem -> {
                        VideoPreviewSheet(
                            videoItem = item,
                            onDismiss = { previewItem = null },
                            onSelect = {
                                onItemSelected(it)
                                previewItem = null
                            }
                        )
                    }
                    is AudioItem -> {
                        AudioPreviewSheet(
                            audioItem = item,
                            onDismiss = { previewItem = null },
                            onSelect = {
                                onItemSelected(it)
                                previewItem = null
                            }
                        )
                    }
                }
            }
        }
    }
}
