package collado.fernando.todoapp.helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
 * Code is a mix from answers:
 * https://stackoverflow.com/a/23440985/6648597
 * https://stackoverflow.com/a/16448278/6648597
 */

public class AlarmReceiver extends BroadcastReceiver {

    private static String BIG_CONTENT_TITLE = "";
    private static String BIG_TEXT          = "";
    private static String SUMMARY_TEXT      = "";
    private static final String CONTENT_TITLE     = "TODO App notification";
    private static final String CONTENT_TEXT      = "Remember TODO stuff ;)";
    private static final String CHANNEL_ID        = "notify_001";
    private static final String CHANNEL_NAME      = "Channel human readable title";

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Bundle extras = notificationIntent.getExtras();
        BIG_CONTENT_TITLE = extras.getString("BIG_CONTENT_TITLE");
        BIG_TEXT = extras.getString("BIG_TEXT");
        SUMMARY_TEXT = extras.getString("SUMMARY_TEXT");

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context.getApplicationContext(), CHANNEL_ID);
        Intent ii = new Intent(context.getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, ii, 0);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(BIG_TEXT);
        bigText.setBigContentTitle(BIG_CONTENT_TITLE);
        bigText.setSummaryText(SUMMARY_TEXT);

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.drawable.ic_small_clipboard);
        mBuilder.setContentTitle(CONTENT_TITLE);
        mBuilder.setContentText(CONTENT_TEXT);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
        }

        mNotificationManager.notify(0, mBuilder.build());

    }

}
