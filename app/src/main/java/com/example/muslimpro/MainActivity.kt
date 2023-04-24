package com.example.muslimpro


import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.muslimpro.ui.theme.MuslimProTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
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

                    MyApp {
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
fun MyScreenContent() {
    var alarmList by remember {
        mutableStateOf(
            listOf(
                Alarm(id = 1, time = "08:00"),
                Alarm(id = 2, time = "12:00"),
                Alarm(id = 3, time = "18:00")
            )
        )
    }
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = "Liste des alarmes")

        AlarmList(
            alarms = alarmList,
            onAlarmDelete = { alarm ->
                alarmList = alarmList.filter { it.id != alarm.id }
            },
            onAlarmUpdate = { updatedAlarm ->
                alarmList = alarmList.map { alarm ->
                    if (alarm.id == updatedAlarm.id) updatedAlarm else alarm
                }

            }
        )
        AddAlarmButton(onAddAlarm = { time ->
            alarmList = alarmList + Alarm(id = alarmList.size + 1, time = time)
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
            var enabled by remember { mutableStateOf(true) }//{ mutableStateOf(alarm.enabled) }

            var selectedTime by remember { mutableStateOf(LocalTime.now()) }
            //var validated by remember { mutableStateOf(false) }

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
                    onCheckedChange = { enabled = it },
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

                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        selectedTime = LocalTime.of(hour, minute)
                        selectedAlarm?.let {
                            it.time = selectedTime.toString()
                            onAlarmUpdate(it)
                        }
                        selectedAlarm = null
                    },
                    timeselected.hour,
                    timeselected.minute,
                    true
                ).show()
                //selectedAlarm = null

            }


        }
    }

}


//@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddAlarmButton(onAddAlarm: (String) -> Unit) {
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }


//    var selectedTime: LocalTime = LocalTime.now()

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
                contentDescription = "Ajouter une alarme"
            )
        }
    }
}

/*
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MuslimProTheme {
    }
}
*/