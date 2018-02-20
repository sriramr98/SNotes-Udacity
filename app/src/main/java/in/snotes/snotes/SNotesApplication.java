package in.snotes.snotes;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.google.firebase.database.FirebaseDatabase;

import in.snotes.snotes.utils.AppConstants;
import in.snotes.snotes.utils.FirebaseUtils;
import in.snotes.snotes.utils.SharedPrefsUtils;
import timber.log.Timber;

public class SNotesApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());

        // initialising pref utils
        SharedPrefsUtils.instantiate(this);

        // initialising firebase utils
        FirebaseUtils.init();

        // enabling offlie strorage for Firebase Database
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

//        creating the notification channel if it doesn't exist already
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            if (manager.getNotificationChannel(AppConstants.REMAINDER_NOTIFICATION_CHANNEL_ID) == null) {
                String displayChannelName = getString(R.string.notification_channel_display_title);
                NotificationChannel channel = new NotificationChannel(AppConstants.REMAINDER_NOTIFICATION_CHANNEL_ID, displayChannelName, NotificationManager.IMPORTANCE_HIGH);
                channel.enableLights(true);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

                manager.createNotificationChannel(channel);
            }

        }


    }
}
