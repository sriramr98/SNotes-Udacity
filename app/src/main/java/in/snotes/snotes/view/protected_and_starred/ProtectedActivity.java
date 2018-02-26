package in.snotes.snotes.view.protected_and_starred;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.snotes.snotes.R;
import in.snotes.snotes.model.Note;
import in.snotes.snotes.utils.AppConstants;
import in.snotes.snotes.utils.FirebaseUtils;
import in.snotes.snotes.utils.SharedPrefsUtils;
import in.snotes.snotes.view.addnotes.AddNotesActivity;
import in.snotes.snotes.view.notesmain.NotesAdapter;
import timber.log.Timber;

// even tough this is named ProtectedActivity, since the code for both
public class ProtectedActivity extends AppCompatActivity implements NotesAdapter.NotesListener {

    public static final String ACTION_PROTECTED = "protected";
    public static final String ACTION_STARRED = "starred";

    @BindView(R.id.toolbar_locked)
    Toolbar toolbarLocked;
    @BindView(R.id.rv_protected)
    RecyclerView rvProtected;
    @BindView(R.id.pbLocked)
    ProgressBar pbLocked;

    private boolean isTablet;

    private ArrayList<Note> notes = new ArrayList<>();

    private NotesAdapter adapter;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference users = FirebaseDatabase.getInstance().getReference(AppConstants.REFERENCE_USERS);

    private String action;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locked);
        ButterKnife.bind(this);

        setSupportActionBar(toolbarLocked);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent i = getIntent();
        if (i == null) {
            Timber.e("Error no input intent");
            return;
        }

        action = i.getAction();

        if (getSupportActionBar() != null) {
            String title;
            if (ACTION_PROTECTED.equals(action)) {
                title = getString(R.string.toolbar_title_protected);
            } else {
                title = getString(R.string.toolbar_title_starred);
            }
            getSupportActionBar().setTitle(title);
        }

        isTablet = getResources().getBoolean(R.bool.isTablet);

        adapter = new NotesAdapter(this, this);

        StaggeredGridLayoutManager layoutManager;

        if (isTablet) {
            layoutManager = new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);
        } else {
            layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        }

        rvProtected.setLayoutManager(layoutManager);
        rvProtected.setAdapter(adapter);

        getDataFromDatabase();


    }

    private void getDataFromDatabase() {
        if (mAuth.getCurrentUser() == null) {
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        pbLocked.setVisibility(View.VISIBLE);
        users.child(uid)
                .child(AppConstants.USER_NOTES)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        pbLocked.setVisibility(View.GONE);
                        if (dataSnapshot.getValue() == null) {
                            return;
                        }

                        Timber.i("Data is %s", dataSnapshot.getValue().toString());
                        Note note = FirebaseUtils.getNoteFromSnapshot(dataSnapshot);
                        if (note == null) {
                            return;
                        }

                        addNoteToList(note);
                        adapter.setNotes(notes);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.getValue() == null) {
                            return;
                        }
                        Timber.i("Note changed %s", dataSnapshot.getValue().toString());
                        Timber.i("Changed string s is %s", s);
                        Note note = FirebaseUtils.getNoteFromSnapshot(dataSnapshot);
                        Note noteToRemove = null;
                        if (note == null) {
                            return;
                        }
                        for (Note n : notes) {
                            if (n.getTimestamp() == note.getTimestamp()) {
                                noteToRemove = n;
                            }
                        }

                        if (noteToRemove != null) {
                            Timber.i("Removing note");
                            notes.remove(noteToRemove);
                            addNoteToList(note);
                            adapter.setNotes(notes);
                        }

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() == null) {
                            Timber.e("Error on child removed");
                            return;
                        }
                        Note note = FirebaseUtils.getNoteFromSnapshot(dataSnapshot);
                        if (note == null) {
                            return;
                        }
                        Note noteToRemove = null;
                        for (Note n : notes) {
                            if (n.getTimestamp() == note.getTimestamp()) {
                                noteToRemove = n;
                            }
                        }

                        if (noteToRemove != null) {
                            notes.remove(noteToRemove);
                            adapter.setNotes(notes);
                            Timber.i("Note removed %s", dataSnapshot.getValue().toString());
                        }
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void addNoteToList(Note note) {
        if (Objects.equals(action, ACTION_PROTECTED)) {
            if (note.getLocked()) {
                notes.add(note);
            }
        } else {
            if (note.getStarred()) {
                notes.add(note);
            }
        }
    }

    @Override
    public void onNoteClicked(Note note) {
        if (note.getLocked()) {
            new MaterialDialog.Builder(this)
                    .title(R.string.title_pin_dialog)
                    .inputType(InputType.TYPE_CLASS_NUMBER)
                    .input(null, null, (dialog, input) -> {
                        // Do something
                        String pin = String.valueOf(SharedPrefsUtils.getPin());
                        if (Objects.equals(input.toString(), pin)) {
                            goToAddNotesOnEdit(note);
                        } else {
                            showPinErrorDialog();
                        }
                    }).show();
        } else {
            goToAddNotesOnEdit(note);
        }
    }

    private void showPinErrorDialog() {
        new MaterialDialog.Builder(this)
                .title(getString(R.string.error))
                .content(getString(R.string.wrong_pin))
                .neutralText(getString(R.string.ok))
                .show();
    }


    private void goToAddNotesOnEdit(Note note) {
        Intent i = new Intent(this, AddNotesActivity.class);
        i.setAction(AppConstants.ACTION_EDIT_NOTE);
        i.putExtra(AppConstants.NOTE_EXTRA, note);
        startActivity(i);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
