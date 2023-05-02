package com.example.muslimpro


import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.AlarmClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.muslimpro.ui.theme.MuslimProTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: AlarmViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            MuslimProTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    // Initialize the view model
                    viewModel = ViewModelProvider(this)[AlarmViewModel::class.java]

                    MyApp {
//                        MyScreenContent(viewModel.alarms)
                        MyScreenContent()
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp(content: @Composable (PaddingValues) -> Unit) {
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(

                    title = { Text(text = stringResource(R.string.app_name)) }
                )
            },
            content = content
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
//fun MyScreenContent(alarms: List<Alarm>) {
fun MyScreenContent() {
    var currentAlarm by remember { mutableStateOf<Alarm?>(null) }
//    var alarmList by remember { mutableStateOf(emptyList<Alarm>()) }
    //recuperer la liste des alarm depuis la base de données
/*
    var alarmList by remember {
        mutableStateOf(
            listOf(
                Alarm(id = 1, time = "22:00",enabled = true),
                Alarm(id = 2, time = "16:00",enabled = false),
                Alarm(id = 3, time = "18:00",enabled = true)
            )
        )
    }
*/
    var database = Database(LocalContext.current)
//    val alarmDao = AlarmDatabase.getInstance(context = LocalContext.current).alarmDao()
//    var data = AlarmRepository(alarmDao = alarmDao, context = LocalContext.current)


    var alarmList by remember {
        mutableStateOf(
                database.getAllAlarms()
//                data.getAlarms()
        )
    }

    // gestion de la sonnerie
    val now = Calendar.getInstance()
    // filtrer les alarms qui ne seront pas pas declanchés et les trier dans l'ordre croissant
    var filteredAlarms = alarmList.filter { alarm ->
        var alarmTime = Calendar.getInstance().apply {
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
            //setAlarm(selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")))
            //playRingtoneAtTime(LocalContext.current, selectedTime.hour , selectedTime.minute)
        }
    }

    // mise en page de l application
    Column(
        modifier = Modifier.padding(16.dp)
    ) {

        Text(text = "Liste des alarmes")

        AlarmList(
            alarms = alarmList.sortedBy { LocalTime.parse(it.time) },

            onAlarmDelete = { alarm ->
                alarmList = alarmList.filter { it.id != alarm.id }
                database.deleteAlarm(alarm.id)
            },
            onAlarmUpdate = { updatedAlarm ->
                alarmList = alarmList.map { alarm ->
                    if (alarm.id == updatedAlarm.id) updatedAlarm else alarm
                }
                database.updateAlarm(updatedAlarm)

            }
        )
        AddAlarmButton(onAddAlarm = { time ->
                val newAlarm = database.addAlarm(time)
//            val newAlarm = data.addAlarm(time)
                alarmList = alarmList + newAlarm

        })
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AlarmList(alarms: List<Alarm>, onAlarmDelete: (Alarm) -> Unit, onAlarmUpdate: (Alarm) -> Unit) {

    var selectedAlarm by remember { mutableStateOf<Alarm?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(top = 16.dp)
    ) {
        items(alarms) { alarm ->
            var enabled by remember { mutableStateOf(alarm.enabled ) }//{ mutableStateOf(alarm.enabled) }

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
                var newAlarm =selectedAlarm

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

@Composable
fun setAlarm(time: String) {
    val intent = Intent(AlarmClock.ACTION_SET_ALARM)
        .putExtra(AlarmClock.EXTRA_HOUR, time.substring(0, 2).toInt())
        .putExtra(AlarmClock.EXTRA_MINUTES, time.substring(3, 5).toInt())
    LocalContext.current.startActivity(intent)
}
private var mediaPlayer: MediaPlayer? = null

fun playRingtoneAtTime(context: Context, hour: Int, minute: Int) {
    val now = Calendar.getInstance()
    val targetTime = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    // Si l'heure ciblée est dans le passé, on ajoute un jour pour programmer la sonnerie le lendemain
    if (targetTime.timeInMillis < now.timeInMillis) {
        targetTime.add(Calendar.DAY_OF_YEAR, 1)
    }

    val delay = targetTime.timeInMillis - now.timeInMillis
    Handler(Looper.getMainLooper()).postDelayed({
        mediaPlayer = MediaPlayer.create(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }, delay)

}

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
        var mediaPlayer = MediaPlayer.create(context, R.raw.audio)
        mediaPlayer?.start()
    }, delay)
}

