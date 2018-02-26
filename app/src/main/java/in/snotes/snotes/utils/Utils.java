package in.snotes.snotes.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import in.snotes.snotes.BuildConfig;
import in.snotes.snotes.R;
import in.snotes.snotes.model.AboutDescModel;
import in.snotes.snotes.model.AboutModel;
import in.snotes.snotes.model.Note;
import in.snotes.snotes.service.NotesService;

public class Utils {

    private Utils() {
    }

    public static String getDate(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return DateFormat.format("d MMM yyyy HH:mm:ss", calendar).toString();
    }

    public static void scheduleAlarm(Context context, Note note) {
        AlarmManager alarmManager = AlarmManagerProvider.getAlarmManager(context);

        PendingIntent operation = NotesService.getNotesRemainderPendingIntent(context, note.getId());

        alarmManager.setExact(AlarmManager.RTC, note.getRemainderTime(), operation);
    }

    public static List<Object> getAboutItems(Context context) {

        List<Object> aboutItems = new ArrayList<>();

        aboutItems.add(new AboutDescModel(context.getString(R.string.version_title), BuildConfig.VERSION_NAME, R.drawable.ic_info));
        aboutItems.add(new AboutDescModel(context.getString(R.string.rate_title), context.getString(R.string.rate_desc), R.drawable.ic_star_closed));
        aboutItems.add(new AboutModel(context.getString(R.string.licenses), R.drawable.ic_note));
        aboutItems.add(new AboutModel(context.getString(R.string.share_title), R.drawable.ic_share));
        aboutItems.add(new AboutDescModel(context.getString(R.string.author_name), context.getString(R.string.author_location), R.drawable.ic_person));
        aboutItems.add(new AboutModel(context.getString(R.string.follow_on_github), R.drawable.ic_github));
        aboutItems.add(new AboutDescModel(context.getString(R.string.report_title), context.getString(R.string.report_desc), R.drawable.ic_report));

        return aboutItems;
    }

    public static void sendFeedback(Context context) {
        Uri uri = Uri.parse("mailto: something@gmail.com")
                .buildUpon()
                .appendQueryParameter("subject", "Feedback for SNotes")
                .build();

        Intent feedback = new Intent(Intent.ACTION_SENDTO);
        feedback.setData(uri);
        context.startActivity(Intent.createChooser(feedback, context.getString(R.string.share_feedback)));
    }


}
