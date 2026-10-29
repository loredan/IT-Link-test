package ru.loredan13.it_link_test.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dagger.hilt.android.AndroidEntryPoint
import ru.loredan13.it_link_test.presentation.ui.screen.image.ImageScreen
import ru.loredan13.it_link_test.presentation.ui.screen.main.MainScreen
import ru.loredan13.it_link_test.presentation.ui.theme.ITLinkTestTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ITLinkTestTheme(dynamicColor = false) {
                val navController = rememberNavController()
                CompositionLocalProvider(LocalNavController provides navController) {
                    NavHost(
                        navController = navController,
                        startDestination = MainScreen
                    ) {
                        composable(MainScreen::class) {
                            MainScreen()
                        }

                        composable(ImageScreen::class) {
                            ImageScreen(it.toRoute<ImageScreen>())
                        }
                    }
                }
            }
        }
    }
}

val LocalNavController =
    compositionLocalOf<NavController> { throw IllegalStateException("No NavController provided") }