package in.snotes.snotes.service;

import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import in.snotes.snotes.utils.AppConstants;
import in.snotes.snotes.utils.SharedPrefsUtils;
import timber.log.Timber;

public final class NotesServiceUtils {

    private static final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private NotesServiceUtils() {
    }

    public static void registerUserWithDb(Intent intent) {
        String userUid = intent.getStringExtra("user-uid");
        String userName = intent.getStringExtra("user-name");

        if (userUid == null || userName == null) {
            Timber.e("Error in either user uid or name");
            return;
        }

        String password = AppConstants.DEFAULT_PIN;
        HashMap<String, Object> user = new HashMap<>();
        user.put("name", userName);
        user.put("pin", password);

        SharedPrefsUtils.setPin(password);

        mDatabase.getReference(AppConstants.REFERENCE_USERS).child(userUid)
                .setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Timber.i("Success in register service");
                    } else {
                        Timber.e("Error in register servicez");
                    }
                });

        // adding the user name to the
        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(userName)
                .build();

        if (mAuth.getCurrentUser() == null) {
            Timber.e("Error getting current user in NotesService.");
            return;
        }
        mAuth.getCurrentUser().updateProfile(userProfileChangeRequest).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Timber.d("User registration completed");
            } else {
                Timber.e("Error adding user name to profile with error %s", task.getException().getMessage());
            }
        });

    }

    public static void syncPrefsAfterLogin() {

        if (mAuth.getCurrentUser() == null) {
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        mDatabase.getReference(AppConstants.REFERENCE_USERS)
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("pin").exists()) {
                            String pin = dataSnapshot.child("pin").getValue().toString();
                            SharedPrefsUtils.setPin(pin);
                        } else {
                            Timber.e("Pin doesn't exist in snapshot");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Timber.e(databaseError.toException());
                    }
                });

    }

}
