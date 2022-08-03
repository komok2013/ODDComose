package ru.edinros.agitatoroffline

import android.content.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.rememberNavHostEngine
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.edinros.agitatoroffline.core.PreferencesDataStore
import ru.edinros.agitatoroffline.core.repositories.AuthRepository
import ru.edinros.agitatoroffline.core.repositories.SessionRepository
import ru.edinros.agitatoroffline.destinations.AuthPageDestination
import ru.edinros.agitatoroffline.destinations.OptionsPageDestination
import ru.edinros.agitatoroffline.destinations.SessionPageDestination
import ru.edinros.agitatoroffline.ui.theme.ODDComposeTheme
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TestVM @Inject constructor(private val dataSore: PreferencesDataStore) : ViewModel() {
    init {
        Timber.d("Model--> ")
    }

    override fun onCleared() {
        Timber.d("-->Close VM")
        super.onCleared()
    }
}

@HiltViewModel
class AuthVM @Inject constructor(private val repository: AuthRepository) : ViewModel() {
    fun signIn() = viewModelScope.launch {
        repository.updateAuthorized(true)
    }
    val state = repository.watchAuthorized()
}

@HiltViewModel
class SessionVM @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository
    ) : ViewModel() {
    fun openSession() = viewModelScope.launch {
        sessionRepository.updateSession(true)
    }

    fun closeSession() = viewModelScope.launch {
        sessionRepository.updateSession(false)
    }

    fun signOut() = viewModelScope.launch {
        authRepository.updateAuthorized(false)
    }

    val state = sessionRepository.watchSession()
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
                    LauncherPage(onFinish = { finish() })
                }
            }
        }
    }
}

@Composable
fun LauncherPage(onFinish: () -> Unit, model: AuthVM = hiltViewModel()) {
    val engine = rememberNavHostEngine()
    val navController = engine.rememberNavController()
    val state = model.state.collectAsState(true)
    DestinationsNavHost(navGraph = NavGraphs.root, engine = engine, navController = navController, startRoute = if(state.value) SessionPageDestination else AuthPageDestination)
    val currentDestination by navController.appCurrentDestinationAsState()
//    when (state.value) {
//        false -> {
//            currentDestination?.apply {
//                if (this != AuthPageDestination)
//                    navController.navigate(AuthPageDestination) {
//                        popUpTo(this@apply) {
//                            inclusive = true
//                        }
//                    }
//            }
//        }
//        true -> {
//            navController.navigate(SessionPageDestination) {
//                popUpTo(AuthPageDestination) {
//                    inclusive = true
//                }
//            }
//        }
//        else->{}
//
//    }
}

@RootNavGraph(start = true)
@Destination
@Composable
fun AuthPage(
    model: AuthVM = hiltViewModel(),
    navigator: DestinationsNavigator,
    testVM: TestVM = hiltViewModel()
) {
    Timber.d("NAVIGATOR AUTH is %s", navigator)
    BackHandler(true) { }
    Column {
        Text(text = "AuthPage")
        Button(onClick = { model.signIn() }) {
            Text("Sign In")
        }

    }
}

@Destination
@Composable
fun SessionPage(
    model: SessionVM = hiltViewModel(),
    navigator: DestinationsNavigator,
) {
    val state = model.state.collectAsState(null)

    when (state.value) {
        false -> {
            Column {
                Text(text = "Сессия закрыта")
                Button(onClick = { model.openSession() }) {
                    Text("Open")
                }
                Button(onClick = {
                    model.signOut()

                }) {
                    Text("Sign Out")
                }
            }
        }
        else -> {
            Column {
                Text(text = "Сессия открыта")
                Button(onClick = { model.closeSession() }) {
                    Text("Close")
                }

            }
        }

    }
//    Timber.d("NAVIGATOR SESSION is %s", navigator)
//    SmsRetrieverUserConsentBroadcast(onCodeReceived = { s ->
//        Timber.d("Code = %s", s)
//    })
//    Column {
//        Text(text = "Сессия закрыта")
//        Button(onClick = { model.openSession() }) {
//            Text("Open")
//        }
//        Button(onClick = { model.signOut() }) {
//            Text("Sign Out")
//        }
//    }
}

//@Destination
//@Composable
//fun CloseSessionPage(
//    model: MainVM = hiltViewModel(),
//    navigator: DestinationsNavigator,
//
//    ) {
//    Timber.d("NAVIGATOR SESSION is %s", navigator)
//    Column {
//        Text(text = "Сессия открыта")
//        Button(onClick = { model.closeSession() }) {
//            Text("Close")
//        }
//    }
//}

//@Destination
//@Composable
//fun HomeScreen(
//    model: MainVM = hiltViewModel(),
//    navigator: DestinationsNavigator
//) {
//    Timber.d("NAVIGATOR HOME is %s", navigator)
//    Column {
//        Text(text = "Home")
//        Button(onClick = { model.closeSession() }) {
//            Text("Close")
//        }
//        Button(onClick = {
//            navigator.navigate(InnerPageDestination)
//        }) {
//            Text("Next page")
//        }
//
//    }
//}

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