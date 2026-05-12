package com.yourname.videoeditor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yourname.videoeditor.ui.screens.gallery.GalleryScreen
import com.yourname.videoeditor.ui.screens.gallery.GalleryViewModel
import com.yourname.videoeditor.ui.screens.gallery.GalleryViewModelFactory
import com.yourname.videoeditor.ui.screens.home.HomeScreen
import com.yourname.videoeditor.ui.screens.home.HomeViewModel
import com.yourname.videoeditor.ui.screens.home.HomeViewModelFactory
import com.yourname.videoeditor.ui.screens.settings.SettingsScreen
import com.yourname.videoeditor.ui.screens.timeline.TimelineScreen
import com.yourname.videoeditor.ui.theme.VideoEditorAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = (application as VideoEditorApp).container
        
        setContent {
            val themeMode by appContainer.themePreferences.themeMode.collectAsState(initial = com.yourname.videoeditor.ui.theme.ThemeMode.SYSTEM)
            
            VideoEditorAppTheme(themeMode = themeMode) {
                val navController = rememberNavController()
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            val homeViewModel: HomeViewModel = viewModel(
                                factory = HomeViewModelFactory(appContainer.database.projectDao())
                            )
                            HomeScreen(
                                viewModel = homeViewModel,
                                onNewProjectClick = {
                                    homeViewModel.createNewProject("New Project") { id ->
                                        navController.navigate("timeline/$id")
                                    }
                                },
                                onProjectClick = { project ->
                                    navController.navigate("timeline/${project.id}")
                                },
                                onSettingsClick = {
                                    navController.navigate("settings")
                                }
                            )
                        }
                        
                        composable("settings") {
                            SettingsScreen(
                                onBack = { navController.popBackStack() },
                                themePreferences = appContainer.themePreferences
                            )
                        }
                        
                        composable(
                            route = "timeline/{projectId}",
                            arguments = listOf(navArgument("projectId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val projectId = backStackEntry.arguments?.getLong("projectId") ?: -1L
                            val selectedUriString = backStackEntry.savedStateHandle.get<String>("selectedUri")
                            val selectedUri = remember(selectedUriString) {
                                selectedUriString?.let { android.net.Uri.parse(it) }
                            }

                            TimelineScreen(
                                projectId = projectId,
                                onAddMedia = { navController.navigate("gallery") },
                                initialUri = selectedUri
                            )
                        }
                        
                        composable("gallery") {
                            val galleryViewModel: GalleryViewModel = viewModel(
                                factory = GalleryViewModelFactory(appContainer.mediaDataSource)
                            )
                            GalleryScreen(
                                onItemSelected = { item ->
                                    // In a real app, we'd update the project in the DB
                                    // For now, we'll just navigate back to timeline
                                    navController.previousBackStackEntry?.savedStateHandle?.set("selectedUri", item.uri.toString())
                                    navController.popBackStack()
                                },
                                onBack = { navController.popBackStack() },
                                viewModel = galleryViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
