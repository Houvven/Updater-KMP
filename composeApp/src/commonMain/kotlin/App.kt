import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import data.DataHelper
import misc.MessageUtils.Companion.Snackbar
import misc.json
import org.jetbrains.compose.resources.stringResource
import ui.AboutDialog
import ui.DownloadCardViews
import ui.LoginCardView
import ui.LoginDialog
import ui.MessageCardViews
import ui.MoreInfoCardViews
import ui.TextFieldViews
import ui.components.FloatActionButton
import updaterkmp.composeapp.generated.resources.Res
import updaterkmp.composeapp.generated.resources.app_name

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val deviceName = remember { mutableStateOf(perfGet("deviceName") ?: "") }
    val codeName = remember { mutableStateOf(perfGet("codeName") ?: "") }
    val deviceRegion = remember { mutableStateOf(perfGet("deviceRegion") ?: "") }
    val systemVersion = remember { mutableStateOf(perfGet("systemVersion") ?: "") }
    val androidVersion = remember { mutableStateOf(perfGet("androidVersion") ?: "") }

    val loginInfo = perfGet("loginInfo")?.let { json.decodeFromString<DataHelper.LoginData>(it) }
    val isLogin = remember { mutableStateOf(loginInfo?.authResult?.toInt() ?: 0) }

    val curRomInfo = remember { mutableStateOf(DataHelper.RomInfoData()) }
    val incRomInfo = remember { mutableStateOf(DataHelper.RomInfoData()) }

    val curIconInfo: MutableState<List<DataHelper.IconInfoData>> = remember { mutableStateOf(listOf()) }
    val incIconInfo: MutableState<List<DataHelper.IconInfoData>> = remember { mutableStateOf(listOf()) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val listState = rememberLazyListState()
    var fabVisible by remember { mutableStateOf(true) }
    var scrollDistance by remember { mutableStateOf(0f) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val isScrolledToEnd =
                    (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == listState.layoutInfo.totalItemsCount - 1 && (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.size
                        ?: 0) < listState.layoutInfo.viewportEndOffset)

                val delta = available.y
                if (!isScrolledToEnd) {
                    scrollDistance += delta
                    if (scrollDistance < -50f) {
                        if (fabVisible) fabVisible = false
                        scrollDistance = 0f
                    } else if (scrollDistance > 50f) {
                        if (!fabVisible) fabVisible = true
                        scrollDistance = 0f
                    }
                }
                return Offset.Zero
            }
        }
    }

    val offsetHeight by animateDpAsState(
        targetValue = if (fabVisible) 0.dp else 74.dp + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(), // 74.dp = FAB + FAB Padding
        animationSpec = tween(durationMillis = 350)
    )

    AppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.statusBars)
                .displayCutoutPadding(),
            topBar = {
                TopAppBar(scrollBehavior, isLogin)
            }
        ) { padding ->
            Box(
                modifier = Modifier.nestedScroll(nestedScrollConnection)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .padding(top = padding.calculateTopPadding())
                        .padding(horizontal = 20.dp)
                ) {
                    item {
                        BoxWithConstraints {
                            if (maxWidth < 768.dp) {
                                Column(
                                    modifier = Modifier.navigationBarsPadding()
                                ) {
                                    LoginCardView(isLogin)
                                    TextFieldViews(deviceName, codeName, deviceRegion, systemVersion, androidVersion)
                                    MessageCardViews(curRomInfo)
                                    MoreInfoCardViews(curRomInfo, curIconInfo)
                                    DownloadCardViews(curRomInfo)
                                    MessageCardViews(incRomInfo)
                                    MoreInfoCardViews(incRomInfo, incIconInfo)
                                    DownloadCardViews(incRomInfo)
                                }
                            } else {
                                Column(
                                    modifier = Modifier.navigationBarsPadding()
                                ) {
                                    Row {
                                        Column(modifier = Modifier.weight(0.8f).padding(end = 20.dp)) {
                                            LoginCardView(isLogin)
                                            TextFieldViews(deviceName, codeName, deviceRegion, systemVersion, androidVersion)
                                        }
                                        Column(modifier = Modifier.weight(1.0f)) {
                                            MessageCardViews(curRomInfo)
                                            MoreInfoCardViews(curRomInfo, curIconInfo)
                                            DownloadCardViews(curRomInfo)
                                            MessageCardViews(incRomInfo)
                                            MoreInfoCardViews(incRomInfo, incIconInfo)
                                            DownloadCardViews(incRomInfo)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                FloatActionButton(
                    offsetHeight, deviceName, codeName, deviceRegion, systemVersion,
                    androidVersion, curRomInfo, incRomInfo, curIconInfo, incIconInfo, isLogin
                )
                Snackbar(offsetY = offsetHeight)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(scrollBehavior: TopAppBarScrollBehavior, isLogin: MutableState<Int>) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
            )
        },
        colors = TopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            scrolledContainerColor = MaterialTheme.colorScheme.background,
        ),
        navigationIcon = {
            AboutDialog()
        },
        actions = {
            LoginDialog(isLogin)
        },
        scrollBehavior = scrollBehavior
    )
}
