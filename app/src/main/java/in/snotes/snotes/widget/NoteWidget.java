package in.snotes.snotes.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.widget.RemoteViews;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import in.snotes.snotes.R;
import in.snotes.snotes.model.Note;
import in.snotes.snotes.utils.AppConstants;
import in.snotes.snotes.utils.FirebaseUtils;
import in.snotes.snotes.view.notesmain.NotesMainActivity;
import timber.log.Timber;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link NoteWidgetConfigureActivity NoteWidgetConfigureActivity}
 */
public class NoteWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Timber.i("Updating widget");

        //intent for on widget clicked
        Intent i = new Intent(context, NotesMainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, 0);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.note_widget);
        views.setOnClickPendingIntent(R.id.widget_parent, pendingIntent);

        String noteId = NoteWidgetConfigureActivity.getNoteIdFromPref(appWidgetId);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        if (auth.getCurrentUser() == null) {
            return;
        }

        database.getReference(AppConstants.REFERENCE_USERS)
                .child(auth.getCurrentUser().getUid())
                .child(AppConstants.USER_NOTES)
                .child(noteId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Timber.i("Data snapshot from widget is %s", dataSnapshot.toString());
                        Note note = FirebaseUtils.getNoteFromSnapshot(dataSnapshot);

                        if (note == null) {
                            Timber.e("Note is null");
                            return;
                        }

                        // setting the background of the widget
                        views.setInt(R.id.widget_parent, "setBackgroundColor", note.getColorOfNote());
                        views.setTextViewText(R.id.widget_title, note.getTitle());
                        views.setTextViewText(R.id.widget_desc, Html.fromHtml(note.getContent()));
                        // Instruct the widget manager to update the widget
                        appWidgetManager.updateAppWidget(appWidgetId, views);
                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            NoteWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

