package com.thechatters.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.thechatters.app.ui.screens.AuthScreen
import com.thechatters.app.ui.screens.MainScreen
import com.thechatters.app.ui.screens.SplashScreen
import com.thechatters.app.ui.theme.TheChattersTheme
import com.thechatters.app.viewmodel.AuthViewModel
import com.thechatters.app.viewmodel.BrainAiViewModel
import com.thechatters.app.viewmodel.CallViewModel
import com.thechatters.app.viewmodel.ChatViewModel
import com.thechatters.app.viewmodel.StatusViewModel

enum class AppDestination {
    SPLASH,
    AUTH,
    MAIN
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as ChattersApplication
        val repository = app.chatRepository
        val geminiService = app.geminiService

        setContent {
            TheChattersTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var currentDestination by remember { mutableStateOf(AppDestination.SPLASH) }

                    val authViewModel = remember { AuthViewModel(repository) }
                    val chatViewModel = remember { ChatViewModel(repository) }
                    val statusViewModel = remember { StatusViewModel(repository) }
                    val callViewModel = remember { CallViewModel(repository) }
                    val brainAiViewModel = remember { BrainAiViewModel(geminiService) }

                    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

                    Crossfade(
                        targetState = currentDestination,
                        label = "AppNavigation"
                    ) { destination ->
                        when (destination) {
                            AppDestination.SPLASH -> {
                                SplashScreen(
                                    onSplashFinished = {
                                        currentDestination = if (isAuthenticated) {
                                            AppDestination.MAIN
                                        } else {
                                            AppDestination.AUTH
                                        }
                                    }
                                )
                            }
                            AppDestination.AUTH -> {
                                AuthScreen(
                                    viewModel = authViewModel,
                                    onAuthSuccess = {
                                        currentDestination = AppDestination.MAIN
                                    }
                                )
                            }
                            AppDestination.MAIN -> {
                                MainScreen(
                                    repository = repository,
                                    chatViewModel = chatViewModel,
                                    statusViewModel = statusViewModel,
                                    callViewModel = callViewModel,
                                    brainAiViewModel = brainAiViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
