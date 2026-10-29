package ru.loredan13.it_link_test.presentation.ui.screen.image

import android.app.Activity
import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.BitmapImage
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.memory.MemoryCache
import coil3.request.ImageRequest
import coil3.size.SizeResolver
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import ru.loredan13.it_link_test.R
import ru.loredan13.it_link_test.domain.util.dataOrNull
import ru.loredan13.it_link_test.domain.util.launchCatching
import kotlin.coroutines.cancellation.CancellationException

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

    val coroutineScope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val resources = LocalResources.current
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
                    if (currentImageState != AsyncImagePainter.State.Empty) {
                        IconButton(onClick = {
                            val currentImageLink = images.dataOrNull()?.get(pagerState.currentPage)
                            coroutineScope.launch {
                                if (currentImageLink != null) {
                                    clipboard.setClipEntry(
                                        ClipEntry(
                                            ClipData(
                                                ClipDescription(
                                                    "link",
                                                    arrayOf("text/plain")
                                                ),
                                                ClipData.Item(currentImageLink)
                                            )
                                        )
                                    )

                                    snackbarHostState.showSnackbar(
                                        message = resources.getString(R.string.link_copied),
                                        duration = SnackbarDuration.Short
                                    )
                                } else {
                                    snackbarHostState.showSnackbar(
                                        message = resources.getString(R.string.link_not_copied),
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.content_copy),
                                contentDescription = null
                            )
                        }
                    }

                    when (val imageState = currentImageState) {
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
                                coroutineScope.launchCatching {
                                    val bitmap = (imageState.result.image as? BitmapImage)?.bitmap

                                    if (bitmap != null) {
                                        runCatching {
                                            val uri = imageViewModel.saveImageForSharing(bitmap)

                                            val intent = Intent(Intent.ACTION_SEND)
                                            intent.type = "image/png"
                                            intent.putExtra(Intent.EXTRA_STREAM, uri)
                                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            context.startActivity(intent)

                                            return@launchCatching
                                        }.onFailure { if (it is CancellationException) throw it }
                                    }

                                    snackbarHostState.showSnackbar(
                                        message = resources.getString(R.string.image_share_error),
                                        duration = SnackbarDuration.Short
                                    )
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

            Image(
                painter = imagePainter,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        fullScreen = !fullScreen
                    },
                contentScale = ContentScale.Fit
            )

            LaunchedEffect(
                pagerState.currentPage,
                imageState
            ) {
                Log.wtf(
                    "WTF",
                    "current: ${pagerState.currentPage}, this: ${index}, state: ${imageState}"
                )
                if (pagerState.currentPage == index) {
                    currentImageState = imageState
                    refreshCurrentImage = { imagePainter.restart() }
                }
            }
        }
    }
}