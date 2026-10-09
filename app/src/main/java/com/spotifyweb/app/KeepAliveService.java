package com.spotifyweb.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

/** Mantiene la app viva con una notificación para que la música siga con la pantalla apagada. */
public class KeepAliveService extends Service {

    private static final String CHANNEL = "playback";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(
                new NotificationChannel(CHANNEL, "Reproducción", NotificationManager.IMPORTANCE_LOW));

        PendingIntent pi = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification n = new Notification.Builder(this, CHANNEL)
                .setContentTitle("Spotify Web")
                .setContentText("Reproduciendo en segundo plano")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(pi)
                .setOngoing(true)
                .build();

        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(1, n);
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
