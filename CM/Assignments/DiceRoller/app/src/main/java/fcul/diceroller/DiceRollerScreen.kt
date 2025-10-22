package fcul.diceroller

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.math.sqrt

@Composable
fun DiceRollerScreen(navController: NavController,
                     modifier: Modifier = Modifier
                         .fillMaxSize()
                         .wrapContentSize(Alignment.Center)
) {
    var result by remember { mutableStateOf(1) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    val imageResource = when (result) {
        1 -> R.drawable.dice_1
        2 -> R.drawable.dice_2
        3 -> R.drawable.dice_3
        4 -> R.drawable.dice_4
        5 -> R.drawable.dice_5
        else -> R.drawable.dice_6
    }

    AccelerometerAction( changeResult = { newResult ->
        result = newResult
    })

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(imageResource),
            contentDescription = "Dice showing $result"
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            modifier = Modifier
                .offset { IntOffset(dragOffset.x.toInt(), dragOffset.y.toInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        dragOffset = Offset(dragOffset.x + dragAmount.x, dragOffset.y + dragAmount.y)
                    }
                },
            onClick = { result = (1..6).random() }
        ) {
            Text(text = stringResource(R.string.roll), fontSize = 24.sp)
        }
//        Button(
//            onClick = {
//                navController.navigate( Screens.DiceResult.route
//                    .replace( oldValue = "{result}", newValue = result.toString() ) )
//            }
//        ) {
//            Text(text = stringResource(R.string.result_screen), fontSize = 24.sp)
//        }
    }
}

@Composable
fun AccelerometerAction(
    changeResult: (Int) -> Unit
) {
    val context = LocalContext.current
    // Shake detection params
    val SHAKE_THRESHOLD_GRAVITY = 2.7f
    val SHAKE_SLOP_TIME_MS = 500L
    var lastShakeTimestamp = remember { 0L }

    // Register sensor listener
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    DisposableEffect(sensorManager, accelSensor) {
        val handler = Handler(Looper.getMainLooper())
        val listener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val x = event.values[0] / SensorManager.GRAVITY_EARTH
                val y = event.values[1] / SensorManager.GRAVITY_EARTH
                val gForce = sqrt(x * x + y * y)
                if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                    val now = System.currentTimeMillis()
                    if (lastShakeTimestamp + SHAKE_SLOP_TIME_MS < now) {
                        lastShakeTimestamp = now
                        handler.post {
                            changeResult((1..6).random())
                        }
                    }
                }
            }
        }
        if (accelSensor != null) {
            sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
        }
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
}