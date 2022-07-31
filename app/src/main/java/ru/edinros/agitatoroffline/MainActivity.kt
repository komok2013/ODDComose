package ru.edinros.agitatoroffline

import android.content.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.android.style.PlaceholderSpan
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.clearBackStack
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.navigation.popUpTo
import com.ramcosta.composedestinations.rememberNavHostEngine
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.edinros.agitatoroffline.core.PrefDataKeyValueStore
import ru.edinros.agitatoroffline.destinations.*
import ru.edinros.agitatoroffline.ui.theme.ODDComposeTheme
import timber.log.Timber
import javax.inject.Inject
//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
sealed class LauncherState {
    object Auth : LauncherState()
    object OpenSession : LauncherState()
    object Home : LauncherState()
}

@HiltViewModel
class TestVM @Inject constructor(private val dataSore: PrefDataKeyValueStore) : ViewModel() {
    init {
        Timber.d("Model--> ")
    }

    override fun onCleared() {
        Timber.d("-->Close VM")
        super.onCleared()
    }
}

@HiltViewModel
class MainVM @Inject constructor(private val dataSore: PrefDataKeyValueStore) : ViewModel() {
    fun signIn() = viewModelScope.launch {
        dataSore.updateAuthorized(true)
    }

    fun signOut() = viewModelScope.launch {
        dataSore.updateAuthorized(false)
    }

    fun openSession() = viewModelScope.launch {
        dataSore.updateSession(true)
    }

    fun closeSession() = viewModelScope.launch {
        dataSore.updateSession(false)
    }

    fun finish() {
        _finish.value = true
        Timber.d("${finish.value}")
    }

    private val _finish = MutableStateFlow(false)
    val finish = _finish.asStateFlow()
    val state = combine(
        dataSore.watchAuthorized(),
        dataSore.watchSession()
    ) { isAuthorized, isSessionOpen ->
        when {
            !isAuthorized -> LauncherState.Auth
            !isSessionOpen -> LauncherState.OpenSession
            else -> LauncherState.Home
        }
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ODDComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LauncherPage(onFinish = {finish()})
                }
            }
        }
    }
}

@Composable
fun LauncherPage(onFinish: () -> Unit,model: MainVM = hiltViewModel()) {
    val engine = rememberNavHostEngine()
    val navController = engine.rememberNavController()
    val state = model.state.collectAsState(null)
    DestinationsNavHost(navGraph = NavGraphs.root, engine = engine, navController = navController)

    val currentDestination by navController.appCurrentDestinationAsState()
    when (state.value) {
        LauncherState.Auth -> {
            currentDestination?.apply {
                if (this != AuthPageDestination)
                    navController.navigate(AuthPageDestination) {
                        popUpTo(this@apply) {
                            inclusive = true
                        }
                    }
            }
        }
        LauncherState.OpenSession -> {
            navController.navigate(OpenSessionPageDestination) {
                popUpTo(AuthPageDestination) {
                    inclusive = true
                }
            }
        }
        LauncherState.Home -> {
            navController.navigate(HomeScreenDestination) {
                popUpTo(AuthPageDestination) {
                    inclusive = true
                }
            }

        }
        else -> navController.navigateUp()
    }

}

//@Destination
//@Composable
//fun LauncherScreen(model: MainVM = hiltViewModel(), navigator: DestinationsNavigator) {
//    val state = model.state.collectAsState(null)
//
//    when (state.value) {
//        LauncherState.Auth -> {
//            AuthPage(navigator = navigator)
//        }
//        LauncherState.OpenSession -> {
//            navigator.navigate(OpenSessionPageDestination){
//                popUpTo(AuthPageDestination){
//                    inclusive=true
//                }
//            }
//            OpenSessionPage(navigator = navigator)
//        }
//        LauncherState.Home -> {
//            navigator.navigate(HomeScreenDestination){
//                popUpTo(OpenSessionPageDestination){
//                    inclusive=true
//                }
//            }
//            HomeScreen(navigator = navigator)
//        }
//        else -> {}
//    }
//}

@RootNavGraph(start = true)
@Destination
@Composable
fun AuthPage(
    model: MainVM = hiltViewModel(),
    navigator: DestinationsNavigator,
    testVM: TestVM = hiltViewModel()
) {
    Timber.d("NAVIGATOR AUTH is %s", navigator)
    BackHandler(true) {  }
    Column {
        Text(text = "AuthPage")
        Button(onClick = { model.signIn() }) {
            Text("Sign In")
        }

    }
}

@Destination
@Composable
fun OpenSessionPage(model: MainVM = hiltViewModel(), navigator: DestinationsNavigator) {
    Timber.d("NAVIGATOR SESSION is %s", navigator)
    SmsRetrieverUserConsentBroadcast(onCodeReceived = { s ->
        Timber.d("Code = %s", s)
    })
    Column {
        Text(text = "OpenSessionPage")
        Button(onClick = { model.openSession() }) {
            Text("Open")
        }
        Button(onClick = { model.signOut() }) {
            Text("Sign Out")
        }
    }
}

@Destination
@Composable
fun HomeScreen(
    model: MainVM = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    Timber.d("NAVIGATOR HOME is %s", navigator)
    Column {
        Text(text = "Home")
        Button(onClick = { model.closeSession() }) {
            Text("Close")
        }
        Button(onClick = {
            navigator.navigate(InnerPageDestination)
        }) {
            Text("Next page")
        }

    }
}

@Destination
@Composable
fun LocationsOnMapPage() {

}

@Destination
@Composable
fun LocationsPage() {

}

@Destination
@Composable
fun PlacesPage() {

}

@Destination
@Composable
fun PlaceStatusPage() {

}

@Destination
@Composable
fun SurveyPage() {

}

@Destination
@Composable
fun OptionsPage(navigator: DestinationsNavigator) {
    Timber.d("NAVIGATOR Option is %s", navigator)
    Column {
        Text(text = "Option Page")
    }
}

@Destination
@Composable
fun InnerPage(navigator: DestinationsNavigator) {
    Timber.d("NAVIGATOR INNER is %s", navigator)
    Button(onClick = { navigator.navigate(OptionsPageDestination) }) {
        Text("To option page")
    }
}

@Composable
fun SystemBroadcastReceiver(
    systemAction: String,
    onSystemEvent: (intent: Intent?) -> Unit
) {
    val context = LocalContext.current
    val currentOnSystemEvent by rememberUpdatedState(onSystemEvent)
    DisposableEffect(context, systemAction) {
        val intentFilter = IntentFilter(systemAction)
        val broadcast = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                currentOnSystemEvent(intent)
            }
        }
        context.registerReceiver(broadcast, intentFilter)
        onDispose {
            context.unregisterReceiver(broadcast)
            Timber.d("-->ON DISPOSE")
        }
    }
}

@Composable
private fun SmsRetrieverUserConsentBroadcast(onCodeReceived: (code: String) -> Unit) {
    val context = LocalContext.current
    var shouldRegisterReceiver by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        SmsRetriever.getClient(context).also {
            it.startSmsUserConsent(null)
                .addOnSuccessListener {
                    Timber.d("LISTENING_SUCCESS")
                    shouldRegisterReceiver = true
                }
                .addOnFailureListener {
                    Timber.d("LISTENING_FAILURE")
                }
        }
    }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == ComponentActivity.RESULT_OK) {
                result.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)?.let { s ->
                    val code = fetchVerificationCode(s)
                    onCodeReceived(code)
                }
            } else {
                Timber.d("Canceled")
            }
        }

    if (shouldRegisterReceiver) {
        SystemBroadcastReceiver(systemAction = SmsRetriever.SMS_RETRIEVED_ACTION) { intent ->
            if (intent != null && SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
                val extras = intent.extras
                val smsRetrieverStatus = extras?.get(SmsRetriever.EXTRA_STATUS) as Status
                when (smsRetrieverStatus.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        val consentIntent =
                            extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                        try {
                            launcher.launch(consentIntent)
                        } catch (e: ActivityNotFoundException) {
                            Timber.e(e, "Activity Not found for SMS consent API")
                        }
                    }
                    CommonStatusCodes.TIMEOUT -> Timber.d("Timeout in sms verification receiver")
                }
            }
        }
    }
}

private fun fetchVerificationCode(message: String): String {
    return Regex("(\\d{6})").find(message)?.value ?: ""
}