package in.snotes.snotes.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.snotes.snotes.R;
import in.snotes.snotes.model.Note;
import in.snotes.snotes.utils.AppConstants;
import in.snotes.snotes.utils.FirebaseUtils;
import in.snotes.snotes.utils.SharedPrefsUtils;
import timber.log.Timber;

/**
 * The configuration screen for the {@link NoteWidget NoteWidget} AppWidget.
 */
public class NoteWidgetConfigureActivity extends Activity implements NoteAdapter.WidgetNoteListener {

    private static final String PREFS_NAME = "in.snotes.snotes.widget.NoteWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    @BindView(R.id.pb_widget_configure)
    ProgressBar pbWidgetConfigure;
    @BindView(R.id.rv_widget_selector)
    RecyclerView rvNotes;

    private List<Note> notes;

    public NoteWidgetConfigureActivity() {
        super();
    }

    static void saveNoteIdToPref(int appWidgetId, String noteId) {
        SharedPrefsUtils.saveWidgetDataToPrefs(appWidgetId, noteId);
    }

    static String getNoteIdFromPref(int appWidgetId) {
        return SharedPrefsUtils.getWidgetDataFromPrefs(appWidgetId);
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.note_widget_configure);
        ButterKnife.bind(this);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        NoteAdapter adapter = new NoteAdapter(this, this);
        rvNotes.setLayoutManager(layoutManager);
        rvNotes.setAdapter(adapter);

        notes = new ArrayList<>();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();


        pbWidgetConfigure.setVisibility(View.VISIBLE);
        database.getReference(AppConstants.REFERENCE_USERS).child(auth.getCurrentUser().getUid())
                .child(AppConstants.USER_NOTES)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Timber.i("Note in widget activity %s", dataSnapshot.toString());
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            Note note = FirebaseUtils.getNoteFromSnapshot(snapshot);
                            notes.add(note);
                        }

                        adapter.addNote(notes);
                        pbWidgetConfigure.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Timber.e(databaseError.toException());
                        pbWidgetConfigure.setVisibility(View.GONE);
                    }
                });

    }

    @Override
    public void onNoteClicked(Note note) {

        final Context context = NoteWidgetConfigureActivity.this;

        // When the button is clicked, store the string locally
        saveNoteIdToPref(mAppWidgetId, note.getId());

        // It is the responsibility of the configuration activity to update the app widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        NoteWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

        // Make sure we pass back the original appWidgetId
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }
}

