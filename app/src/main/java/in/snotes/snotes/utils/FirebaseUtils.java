package in.snotes.snotes.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import in.snotes.snotes.R;
import in.snotes.snotes.model.Note;
import in.snotes.snotes.view.addnotes.AddNotesActivity;
import timber.log.Timber;

public final class FirebaseUtils {

    private static FirebaseAuth mAuth;
    private static FirebaseDatabase mDatabase;

    private FirebaseUtils() {
    }

    public static void init() {
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }

        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
        }
    }

    public static void addNoteToUser(Context context, HashMap<String, Object> note) {

        if (mAuth.getCurrentUser() == null) {
            Timber.i("Current user is null");
            return;
        }

        mDatabase.getReference(AppConstants.REFERENCE_USERS)
                .child(mAuth.getCurrentUser().getUid())
                .child(AppConstants.USER_NOTES)
                .push()
                .setValue(note)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Timber.i("Added note successfully");
                        Toast.makeText(context, "Added note successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Timber.e(task.getException());
                    }
                });


    }

    public static Note getNoteFromSnapshot(DataSnapshot dataSnapshot) {

        Note note;
        note = dataSnapshot.getValue(Note.class);
        if (note == null) {
            return null;
        }
        note.setId(dataSnapshot.getKey());
        Timber.i("Note is %s", note.toString());
        return note;
    }

    public static void updateNote(Context context, Note currentNote) {
        Timber.i("Update note called with note %s", currentNote.toString());
        if (mAuth.getCurrentUser() == null) {
            return;
        }
        HashMap<String, Object> noteMap = currentNote.getNoteMap();
        String noteId = currentNote.getId();
        String userId = mAuth.getCurrentUser().getUid();

        Timber.i("Id is %s", noteId);

        mDatabase.getReference(AppConstants.REFERENCE_USERS)
                .child(userId)
                .child(AppConstants.USER_NOTES)
                .child(noteId)
                .setValue(noteMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Timber.i("Note updated successfully");
                        Toast.makeText(context, "Edited note", Toast.LENGTH_SHORT).show();
                    } else {
                        Timber.e("Error updating note %s", task.getException().getMessage());
                        Toast.makeText(context, "Error editing note", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static void deleteNote(Context context, Note note) {
        if (mAuth.getCurrentUser() == null) {
            return;
        }
        String uid = mAuth.getCurrentUser().getUid();
        String noteId = note.getId();

        mDatabase.getReference(AppConstants.REFERENCE_USERS)
                .child(uid)
                .child(AppConstants.USER_NOTES)
                .child(noteId)
                .removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Deleted note successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Error deleting note", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public static void showRemainder(Context context, String id) {
        if (mAuth.getCurrentUser() == null) {
            return;
        }
        mDatabase.getReference(AppConstants.REFERENCE_USERS)
                .child(mAuth.getCurrentUser().getUid())
                .child(AppConstants.USER_NOTES)
                .child(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Note note = FirebaseUtils.getNoteFromSnapshot(dataSnapshot);
                        if (note == null) {
                            Timber.i("Error note is null");
                            return;
                        }

                        showRemainderNotification(context, note);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private static void showRemainderNotification(Context context, Note note) {

        if (!note.getRemainderSet()) {
            Timber.i("Notification has been cancelled by the user");
            return;
        }

        // updating note before setting it to intent
        note.setRemainderTime(0);
        note.setRemainderSet(false);

        // get the intent to open on notification click
        Intent action = new Intent(context, AddNotesActivity.class);
        action.setAction(AppConstants.ACTION_EDIT_NOTE);
        action.putExtra(AppConstants.NOTE_EXTRA, note);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // create the pending intent for the notification
        PendingIntent resultantPendingIntent = PendingIntent.getActivity(context,
                0,
                action,
                PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = AppConstants.REMAINDER_NOTIFICATION_CHANNEL_ID;

        String content = Html.fromHtml(note.getContent()).toString();
        String title = note.getTitle();

        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText("Ping Ping")
                .setSmallIcon(R.drawable.ic_done_white_24dp)
                .setContentIntent(resultantPendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .build();

        if (notificationManager != null) {
            notificationManager.notify(AppConstants.NOTIFICATION_ID, notification);
            Timber.i("Notified");
        }


        // we need to update the realtime database once the remainder is shown so that the remainderSet property is set to false
        FirebaseUtils.updateNote(context, note);

    }
}
