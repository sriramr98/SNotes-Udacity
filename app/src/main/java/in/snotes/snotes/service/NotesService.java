package in.snotes.snotes.service;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import in.snotes.snotes.model.Note;
import in.snotes.snotes.utils.AppConstants;
import in.snotes.snotes.utils.FirebaseUtils;
import timber.log.Timber;

public class NotesService extends IntentService {

    private static final String TAG = "NotesService";
    public static final String ACTION_REGISTER_USER = "register-user";
    public static final String ACTION_LOGIN_SYNC = "login-sync";
    public static final String ACTION_CANCEL_REMAINDER = "cancel-remainder";
    private static final String ACTION_REMAINDER_NOTE = "remainder-note";
    private static String REMAINDER_EXTRA = "remainder-extra-id";

    public NotesService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if (intent == null) {
            Timber.e("Intent is null in service");
            return;
        }

        String action = intent.getAction();

        if (ACTION_REGISTER_USER.equals(action)) {
            NotesServiceUtils.registerUserWithDb(intent);
        } else if (ACTION_LOGIN_SYNC.equals(action)) {
            NotesServiceUtils.syncPrefsAfterLogin();
        } else if (ACTION_REMAINDER_NOTE.equals(action)) {
            String id = intent.getStringExtra(REMAINDER_EXTRA);
            FirebaseUtils.showRemainder(this, id);
        } else if (ACTION_CANCEL_REMAINDER.equals(action)) {
            Note n = intent.getParcelableExtra(AppConstants.NOTE_EXTRA);
            n.setRemainderSet(false);
            n.setRemainderTime(0);
            FirebaseUtils.updateNote(this, n);
        }
    }

    public static PendingIntent getNotesRemainderPendingIntent(Context context, String id) {
        Intent action = new Intent(context, NotesService.class);
        action.setAction(ACTION_REMAINDER_NOTE);
        action.putExtra(REMAINDER_EXTRA, id);
        return PendingIntent.getService(context, 0, action, PendingIntent.FLAG_UPDATE_CURRENT);

    }

}
