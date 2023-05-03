package com.example.muslimpro


import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.muslimpro.ui.theme.MuslimProTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: AlarmViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[AlarmViewModel::class.java]

        setContent {
            MuslimProTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    MyApp {
                        MyScreenContent(viewModel.alarms, viewModel)
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp(content: @Composable (PaddingValues) -> Unit) {
    val myImage = painterResource(R.drawable.logo)
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Image(
                                painter = myImage,
                                contentDescription = null
                            )
                        }
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.app_name),
                            fontFamily = FontFamily.Serif
                        )
                    },
                )
            },
            content = content
        )
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyScreenContent(alarms: LiveData<List<AlarmEntity>>, viewModel: AlarmViewModel) {
    var currentAlarm by remember { mutableStateOf<AlarmEntity?>(null) }
    //INSTANCE DE LA ABSE DE DONNEE
    // Define a CoroutineScope for the database operations
    val dbScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

// Instantiate the DAO and repository inside the scope
    val alarmDao = AlarmDatabase.getInstance(LocalContext.current).alarmDao()
    val alarmRepository = AlarmRepository(alarmDao, LocalContext.current)

    // recuperer la liste des alarm depuis la base de donnnées

    var alarmList by remember {
        mutableStateOf(emptyList<AlarmEntity>())
    }

    LaunchedEffect(true) {
        val alarms = withContext(Dispatchers.IO) {
            alarmRepository.getAlarms()
        }
    }

    // gestion de la sonnerie
    val now = Calendar.getInstance()
    // filtrer les alarms qui ne seront pas pas declanchés et les trier dans l'ordre croissant
    val filteredAlarms = alarmList.filter { alarm ->
        val alarmTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.time.substringBefore(":").toInt())
            set(Calendar.MINUTE, alarm.time.substringAfter(":").toInt())
        }
        alarmTime >= now
    }
    if(filteredAlarms.isNotEmpty()) {
        currentAlarm = filteredAlarms.sortedBy{ LocalTime.parse(it.time) }[0]
        if (currentAlarm!!.enabled) {
            val selectedTime = LocalTime.parse(currentAlarm!!.time)
            playAudioAtTime(LocalContext.current, selectedTime.hour, selectedTime.minute)
        }
    }
    // mise en page de l application
    Column(
        modifier = Modifier.padding(16.dp)
    ) {

        Text(text = "Liste des alarmes")

        val alarms = alarms.observeAsState(emptyList())

        AlarmList(
            alarms = alarms.value.sortedBy { LocalTime.parse(it.time) },

            onAlarmDelete = { alarm ->
                dbScope.launch {
                    alarmRepository.deleteAlarm(alarm.id)
                    val updatedList = alarmRepository.getAlarms().value ?: emptyList()
                    withContext(Dispatchers.Main) {
                        alarmList = updatedList.filter { it.id != alarm.id }
                    }
                }
            },
            onAlarmUpdate = { updatedAlarm ->
                dbScope.launch {
                    alarmRepository.updateAlarm(updatedAlarm)
                    val updatedList = alarmRepository.getAlarms().value ?: emptyList()
                    withContext(Dispatchers.Main) {
                        alarmList = updatedList.map { if (it.id == updatedAlarm.id) updatedAlarm else it }
                    }
                }
            }
        )
        AddAlarmButton(
            onAddAlarm = { time ->
                dbScope.launch {
                    val newAlarm = alarmRepository.addAlarm(time)
                    alarmList = alarmList + newAlarm
                }
            }
        )
    }
}


// la liste des Alarmes , pour chaqu'un lorsque la ligne est selectionner , une boite de dialogue s'ouvre pour la modification
// et lorsque l'icon de poubelle est appuyée, l'alarme sera supprimer
// le switch nous permettra d act  viter ou de desactiver l'alarme

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AlarmList(alarms: List<AlarmEntity>, onAlarmDelete: (AlarmEntity) -> Unit, onAlarmUpdate: (AlarmEntity) -> Unit) {

    var selectedAlarm by remember { mutableStateOf<AlarmEntity?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(top = 16.dp)
    ) {
        items(alarms) { alarm ->
            var enabled by remember { mutableStateOf(alarm.enabled ) }

            var selectedTime by remember { mutableStateOf(LocalTime.now()) }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .background(
                        Color.DarkGray.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
                    .clickable(onClick = {
                        selectedAlarm = alarm
                    })
            ) {
                Switch(
                    checked = enabled,
                    onCheckedChange = {
                                        enabled = it
                                        alarm.enabled= enabled
                                        onAlarmUpdate(alarm)
                                      },
                    modifier = Modifier.padding(end = 16.dp)
                )
                Text(
                    text = alarm.time,
                    modifier = Modifier.weight(1f),
                    fontSize = 20.sp
                )
                IconButton(
                    onClick = { onAlarmDelete(alarm) }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
            if (selectedAlarm != null) {
                val context = LocalContext.current
                val timeselected = LocalTime.parse(selectedAlarm!!.time, DateTimeFormatter.ofPattern("HH:mm"))
                val newAlarm =selectedAlarm

                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        selectedTime = LocalTime.of(hour, minute)
                        newAlarm?.let {
                            it.time = selectedTime.toString()
                            onAlarmUpdate(it)
                        }
                    },
                    timeselected.hour,
                    timeselected.minute,
                    true
                ).show()

                selectedAlarm = null
            }


        }
    }

}
//bouton Ajout des Alarm . une dialoge (horloge) s'ouvre lorsque celui-ci est appuyé
//et à la validation du dialoge, une Alarm sera ajouté
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddAlarmButton(onAddAlarm: (String) -> Unit) {
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    val timePickerDialog = TimePickerDialog(
        LocalContext.current,
        { _, hour, minute ->
            selectedTime = LocalTime.of(hour, minute)
            onAddAlarm(selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")))

        },
        selectedTime.hour,
        selectedTime.minute,
        true
    )

    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        FloatingActionButton(
            onClick = {
                timePickerDialog.show()
            },
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Ajouter une alarme!"
            )
        }
    }
}
//gestion de la sonnerie des Alarmes
private var mediaPlayer: MediaPlayer? = null

fun stopRingtone() {
    mediaPlayer?.stop()
    mediaPlayer?.release()
    mediaPlayer = null
}

fun playAudioAtTime(context: Context, hour: Int, minute: Int) {
    val now = Calendar.getInstance()
    val targetTime = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    // Si l'heure ciblée est dans le passé, on ajoute un jour pour programmer la lecture audio le lendemain
    if (targetTime.timeInMillis < now.timeInMillis) {
        targetTime.add(Calendar.DAY_OF_YEAR, 1)
    }

    val delay = targetTime.timeInMillis - now.timeInMillis
    Handler(Looper.getMainLooper()).postDelayed({
        val mediaPlayer = MediaPlayer.create(context, R.raw.audio)
        mediaPlayer?.start()

        val message = "Il est $hour:$minute ! \n c'est l'heure d'aller Prier"
        val createNotification = CreateNotification(context, "Heure de Prierre", message)
        createNotification.showNotification()
    }, delay)
}

