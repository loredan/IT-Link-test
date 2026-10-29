package ru.loredan13.it_link_test.presentation.ui.screen.image

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.size.SizeResolver
import kotlinx.serialization.Serializable
import ru.loredan13.it_link_test.R
import ru.loredan13.it_link_test.domain.util.dataOrNull
import androidx.core.net.toUri

@Serializable
data class ImageScreen(
    val index: Int,
    val memoryCacheKey: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageScreen(args: ImageScreen, imageViewModel: ImageViewModel = hiltViewModel()) {
    var currentImageState by remember {
        mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty)
    }
    var refreshCurrentImage by remember { mutableStateOf({}) }

    val context = LocalContext.current
    val view = LocalView.current

    val snackbarHostState = remember { SnackbarHostState() }

    val images by imageViewModel.imageList.collectAsState()
    val pagerState =
        rememberPagerState(
            initialPage = args.index,
            pageCount = { images.dataOrNull()?.size ?: 0 })

    var fullScreen by remember { mutableStateOf(false) }

    DisposableEffect(fullScreen) {
        if (fullScreen) {
            val insetsController =
                WindowCompat.getInsetsController((context as Activity).window, view)
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            onDispose {
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        } else {
            onDispose { }
        }
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = !fullScreen,
                enter = slideInVertically { -it } + expandVertically(),
                exit = slideOutVertically { -it } + shrinkVertically(),
            ) {
                TopAppBar(title = { Text(stringResource(R.string.app_name)) }, actions = {
                    when (currentImageState) {
                        is AsyncImagePainter.State.Loading -> {
                            Box(Modifier.size(40.dp)) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.Center)
                                )
                            }
                        }

                        is AsyncImagePainter.State.Success -> {
                            IconButton(onClick = {
                                val currentImageLink =
                                    images.dataOrNull()?.get(pagerState.currentPage)
                                if (currentImageLink != null) {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            currentImageLink.toUri()
                                        )
                                    )
                                }
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.open_in_browser),
                                    contentDescription = null
                                )
                            }

                            IconButton(onClick = {
                                val currentImageLink =
                                    images.dataOrNull()?.get(pagerState.currentPage)
                                if (currentImageLink != null) {
                                    val intent = Intent(Intent.ACTION_SEND)
                                    intent.type = "text/plain"
                                    intent.putExtra(Intent.EXTRA_TEXT, currentImageLink)
                                    context.startActivity(intent)
                                }
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.share),
                                    contentDescription = null
                                )
                            }
                        }

                        is AsyncImagePainter.State.Error -> {
                            IconButton(onClick = { refreshCurrentImage() }) {
                                Icon(
                                    painter = painterResource(R.drawable.refresh),
                                    contentDescription = null
                                )
                            }
                        }

                        AsyncImagePainter.State.Empty -> {}
                    }
                })
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) { index ->
            val image = images.dataOrNull()?.get(index)
            val imagePainter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(image)
                    .size(SizeResolver.ORIGINAL)
                    .placeholderMemoryCacheKey(args.memoryCacheKey)
                    .build()
            )
            val imageState by imagePainter.state.collectAsState()

            var containerSize by remember { mutableStateOf(IntSize.Zero) }

            var scale by remember { mutableFloatStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }
            val transformableState =
                rememberTransformableState { _, zoomChange, panChange, _ ->
                    scale = (scale * zoomChange).coerceAtLeast(1f)
                    offset += panChange

                    val imageWidth =
                        (imageState as? AsyncImagePainter.State.Success)?.result?.image?.width
                    val imageHeight =
                        (imageState as? AsyncImagePainter.State.Success)?.result?.image?.height

                    if (imageWidth == null || imageHeight == null || imageWidth <= 0 || imageHeight <= 0 || containerSize == IntSize.Zero)
                        return@rememberTransformableState

                    val initialScale =
                        if (imageWidth.toFloat() / imageHeight < containerSize.width.toFloat() / containerSize.height) {
                            containerSize.height / imageHeight.toFloat()
                        } else {
                            containerSize.width / imageWidth.toFloat()
                        }


                    // Сброс на середину по горизонтали при малом масштабе
                    if (initialScale * scale < containerSize.width.toFloat() / imageWidth) {
                        offset = offset.copy(x = 0f)
                    }
                    // Прилипание к левой границе контейнера
                    else if (offset.x + containerSize.width / 2 - imageWidth / 2 * initialScale * scale > 0) {
                        offset =
                            offset.copy(x = imageWidth / 2 * initialScale * scale - containerSize.width / 2)
                    }
                    // Прилипание к правой границе контейнера
                    else if (offset.x + containerSize.width / 2 + imageWidth / 2 * initialScale * scale < containerSize.width) {
                        offset =
                            offset.copy(x = containerSize.width / 2 - imageWidth / 2 * initialScale * scale)
                    }

                    // Сброс на середину по вертикали при малом масштабе
                    if (initialScale * scale < containerSize.height.toFloat() / imageHeight) {
                        offset = offset.copy(y = 0f)
                    }
                    // Прилипание к верхней границе контейнера
                    else if (offset.y + containerSize.height / 2 - imageHeight / 2 * initialScale * scale > 0) {
                        offset =
                            offset.copy(y = imageHeight / 2 * initialScale * scale - containerSize.height / 2)
                    }
                    // Прилипание к нижней границе контейнера
                    else if (offset.y + containerSize.height / 2 + imageHeight / 2 * initialScale * scale < containerSize.height) {
                        offset =
                            offset.copy(y = containerSize.height / 2 - imageHeight / 2 * initialScale * scale)
                    }
                }

            Image(
                painter = imagePainter,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { containerSize = it.size }
                    .clickable(interactionSource = null, indication = null) {
                        fullScreen = !fullScreen
                    }
                    .transformable(state = transformableState, canPan = { panOffset ->
                        val imageWidth =
                            (imageState as? AsyncImagePainter.State.Success)?.result?.image?.width
                        val imageHeight =
                            (imageState as? AsyncImagePainter.State.Success)?.result?.image?.height

                        if (imageWidth == null || imageHeight == null || imageWidth <= 0 || imageHeight <= 0 || containerSize == IntSize.Zero)
                            return@transformable false

                        val initialScale =
                            if (imageWidth / imageHeight < containerSize.width / containerSize.height) {
                                containerSize.height / imageHeight.toFloat()
                            } else {
                                containerSize.width / imageWidth.toFloat()
                            }

                        // Проверка левой и правой границы картинки для работы перелистывания
                        !(initialScale * scale > containerSize.width / imageWidth && (offset.x + panOffset.x + containerSize.width / 2 - imageWidth / 2 * initialScale * scale >= 0 ||
                                offset.x + panOffset.x + containerSize.width / 2 + imageWidth / 2 * initialScale * scale <= containerSize.width))
                    })
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y,
                    ),
                contentScale = ContentScale.Fit
            )

            LaunchedEffect(
                pagerState.currentPage,
                imageState
            ) {
                if (pagerState.currentPage == index) {
                    currentImageState = imageState
                    refreshCurrentImage = { imagePainter.restart() }
                }
            }
        }
    }
}