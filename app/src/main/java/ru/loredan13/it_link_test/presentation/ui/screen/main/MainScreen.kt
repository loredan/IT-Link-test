package ru.loredan13.it_link_test.presentation.ui.screen.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import kotlinx.serialization.Serializable
import ru.loredan13.it_link_test.R
import ru.loredan13.it_link_test.domain.util.dataOrNull
import ru.loredan13.it_link_test.presentation.LocalNavController
import ru.loredan13.it_link_test.presentation.ui.screen.image.ImageScreen

private val MIN_CELL_SIZE = 100.dp

@Serializable
object MainScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(mainViewModel: MainViewModel = hiltViewModel()) {
    val navController = LocalNavController.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        }
    ) { paddingValues ->
        val images by mainViewModel.imageList.collectAsState()
        LazyVerticalGrid(
            columns = GridCells.Adaptive(MIN_CELL_SIZE),
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            items(images.dataOrNull()?.size ?: 0) { index ->
                val image = images.dataOrNull()?.get(index)
                Box(modifier = Modifier.aspectRatio(1f)) {
                    val size = with(LocalDensity.current) { MIN_CELL_SIZE.roundToPx() }
                    val imagePainter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(image)
                            .size(size)
                            .build(),
                        placeholder = painterResource(R.drawable.ic_launcher_background),
                        error = painterResource(R.drawable.ic_launcher_background),
                    )
                    val imageLoadingState by imagePainter.state.collectAsState()

                    Image(
                        painter = imagePainter,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                navController.navigate(
                                    ImageScreen(
                                        index = index,
                                        memoryCacheKey = (imagePainter.state.value as? AsyncImagePainter.State.Success)?.result?.memoryCacheKey?.key
                                    )
                                )
                            },
                        contentScale = ContentScale.Crop,
                    )

                    if (imageLoadingState is AsyncImagePainter.State.Error) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x80000000))
                        ) {
                            IconButton(
                                onClick = { imagePainter.restart() },
                                modifier = Modifier
                                    .align(Alignment.Center)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.refresh),
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}