package com.example.autosilent.broadcastreceiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.autosilent.R
import com.example.autosilent.util.Constants.NOTIFICATION_CHANNEL_ID
import com.example.autosilent.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.autosilent.util.Constants.NOTIFICATION_ID
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if(geofencingEvent.hasError()){
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            Log.e("BroadcastReceiver", errorMessage)
            return
        }


        when(geofencingEvent.geofenceTransition){
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Log.d("BroadcastReceiver", "Geofence ENTER")
                displayNotification(context, "Geofence ENTER", AudioManager.RINGER_MODE_SILENT)
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Log.d("BroadcastReceiver", "Geofence EXIT")
                displayNotification(context, "Geofence EXIT", AudioManager.RINGER_MODE_NORMAL)
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                Log.d("BroadcastReceiver", "Geofence DWELL")
                displayNotification(context, "Geofence DWELL", AudioManager.RINGER_MODE_VIBRATE)
            }
            else -> {
                Log.d("BroadcastReceiver", "Invalid Type")
                displayNotification(context, "Geofence INVALID TYPE", AudioManager.RINGER_MODE_NORMAL)
            }
        }
    }

    private fun displayNotification(context: Context, geofenceTransition: String, audioMode: Int){
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

//        val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//        audioManager.ringerMode = audioMode

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Geofence")
            .setContentText(geofenceTransition)
        notificationManager.notify(NOTIFICATION_ID, notification.build())


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && nortificationManager.isNotificationPolicyAccessGranted) {
//            val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
//        }

    }

    private fun createNotificationChannel(notificationManager: NotificationManager){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

}


/*
AvdId=Pixel_4_API_30
PlayStore.enabled=true
abi.type=x86
avd.ini.displayname=Pixel 4 API 30
avd.ini.encoding=UTF-8
disk.dataPartition.size=2G
fastboot.chosenSnapshotFile=
fastboot.forceChosenSnapshotBoot=no
fastboot.forceColdBoot=no
fastboot.forceFastBoot=yes
hw.accelerometer=yes
hw.arc=false
hw.audioInput=yes
hw.battery=yes
hw.camera.back=virtualscene
hw.camera.front=emulated
hw.cpu.arch=x86
hw.cpu.ncore=2
hw.dPad=no
hw.device.hash2=MD5:6b5943207fe196d842659d2e43022e20
hw.device.manufacturer=Google
hw.device.name=pixel_4
hw.gps=yes
hw.gpu.enabled=yes
hw.gpu.mode=auto
hw.initialOrientation=Portrait
hw.keyboard=yes
hw.lcd.density=440
hw.lcd.height=2280
hw.lcd.width=1080
hw.mainKeys=no
hw.ramSize=1536
hw.sdCard=yes
hw.sensors.orientation=yes
hw.sensors.proximity=yes
hw.trackBall=no
image.sysdir.1=system-images/android-30/google_apis_playstore/x86/
runtime.network.latency=none
runtime.network.speed=full
sdcard.path=/home/dheeraj/.android/avd/Pixel_4_API_30.avd/sdcard.img
sdcard.size=512 MB
showDeviceFrame=no
skin.dynamic=yes
skin.name=1080x2280
skin.path=_no_skin
skin.path.backup=_no_skin
tag.display=Google Play
tag.id=google_apis_playstore
vm.heapSize=256

 */












