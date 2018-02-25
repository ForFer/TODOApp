package collado.fernando.todoapp.helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import collado.fernando.todoapp.R;
import collado.fernando.todoapp.activities.MainActivity;

/**
 * Created by Fernando on 18/02/18.
 * Code is inspired from Stackoverflow answers:
 * https://stackoverflow.com/a/23440985/6648597
 * https://stackoverflow.com/a/16448278/6648597
 */

public class AlarmReceiver extends BroadcastReceiver {

    // Default values in case the ones from the Bundle extras are not set
    // or properly received
    private String CONTENT_TITLE = "Remember to set your tasks for the next day";
    private String CONTENT_TEXT  = "Remember TODO stuff ;)";
    private String CHANNEL_ID    = "1";
    private String CHANNEL_NAME  = "TODO Channel";

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Bundle extras = intent.getExtras();
        if(extras != null){
            CONTENT_TITLE = extras.getString("CONTENT_TITLE");
            CONTENT_TEXT = extras.getString("CONTENT_TEXT");
            CHANNEL_ID = extras.getString("CHANNEL_ID");
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context.getApplicationContext(), CHANNEL_ID);
        Intent ii = new Intent(context.getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, Integer.parseInt(CHANNEL_ID), ii, PendingIntent.FLAG_UPDATE_CURRENT);


        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.drawable.ic_small_clipboard);
        mBuilder.setContentTitle(CONTENT_TITLE);
        mBuilder.setContentText(CONTENT_TEXT);
        mBuilder.setPriority(Notification.PRIORITY_MAX);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
        }

        mNotificationManager.notify(Integer.parseInt(CHANNEL_ID), mBuilder.build());

    }

}
