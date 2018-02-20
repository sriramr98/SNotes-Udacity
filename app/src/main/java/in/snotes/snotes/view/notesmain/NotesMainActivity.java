package in.snotes.snotes.view.notesmain;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import butterknife.OnClick;
import in.snotes.snotes.R;
import in.snotes.snotes.model.Note;
import in.snotes.snotes.settings.PreferenceActivity;
import in.snotes.snotes.utils.AppConstants;
import in.snotes.snotes.utils.FirebaseUtils;
import in.snotes.snotes.utils.SharedPrefsUtils;
import in.snotes.snotes.utils.Utils;
import in.snotes.snotes.view.about.AboutActivity;
import in.snotes.snotes.view.addnotes.AddNotesActivity;
import in.snotes.snotes.view.auth.AuthActivity;
import in.snotes.snotes.view.protected_and_starred.ProtectedActivity;
import timber.log.Timber;

public class NotesMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, NotesAdapter.NotesListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.pb_notes)
    ProgressBar pbNotes;
    @BindView(R.id.rv_notes_list)
    RecyclerView rvNotesList;
    @BindView(R.id.layout_empty)
    LinearLayout layoutEmpty;
    @BindView(R.id.fab_add_notes)
    FloatingActionButton fabAddNotes;

    private boolean isTablet = false;
    private NotesAdapter adapter;

    private ArrayList<Note> notes = new ArrayList<>();

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference users = FirebaseDatabase.getInstance().getReference(AppConstants.REFERENCE_USERS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        if (mAuth.getCurrentUser() == null) {
            goToAuthActivity();
            finish();
            return;
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        TextView tvUserName = headerView.findViewById(R.id.tv_nav_user_name);
        TextView tvUserEmail = headerView.findViewById(R.id.tv_nav_user_email);

        tvUserName.setText(mAuth.getCurrentUser().getDisplayName());
        tvUserEmail.setText(mAuth.getCurrentUser().getEmail());

        isTablet = getResources().getBoolean(R.bool.isTablet);

        adapter = new NotesAdapter(this, this);

        StaggeredGridLayoutManager layoutManager;

        if (isTablet) {
            layoutManager = new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);
        } else {
            layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        }

        rvNotesList.setLayoutManager(layoutManager);
        rvNotesList.setAdapter(adapter);

        //TODO load data from REALTIME DATABASE
        String uid = mAuth.getCurrentUser().getUid();

        pbNotes.setVisibility(View.VISIBLE);
        users.child(uid)
                .child(AppConstants.USER_NOTES)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (pbNotes != null) {
                            pbNotes.setVisibility(View.GONE);
                        }
                        if (dataSnapshot.getValue() == null) {
                            return;
                        }

                        Timber.i("Data is %s", dataSnapshot.getValue().toString());
                        Note note = FirebaseUtils.getNoteFromSnapshot(dataSnapshot);
                        if (note == null) {
                            return;
                        }
                        notes.add(note);

                        if (notes.isEmpty()) {
                            layoutEmpty.setVisibility(View.VISIBLE);
                            rvNotesList.setVisibility(View.GONE);
                            pbNotes.setVisibility(View.GONE);
                        } else {
                            layoutEmpty.setVisibility(View.GONE);
                            rvNotesList.setVisibility(View.VISIBLE);
                        }

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
                            Timber.i("Adding new note");
                            notes.add(note);
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

                            if (notes.isEmpty()) {
                                layoutEmpty.setVisibility(View.VISIBLE);
                                rvNotesList.setVisibility(View.GONE);
                                pbNotes.setVisibility(View.GONE);
                            } else {
                                layoutEmpty.setVisibility(View.GONE);
                                rvNotesList.setVisibility(View.VISIBLE);
                            }

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


        // This is to manage the fact that if the notes path doesn't contain any notes, no callback from Firebase is Fired.
        if (notes.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvNotesList.setVisibility(View.GONE);
            pbNotes.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvNotesList.setVisibility(View.VISIBLE);
        }

    }

    @OnClick(R.id.fab_add_notes)
    void onFabAddNotesClicked() {
        Intent i = new Intent(this, AddNotesActivity.class);
        i.setAction(AppConstants.ACTION_ADD_NEW_NOTE);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            goToSettingsActivity();
        }
        if (id == R.id.action_logout) {
            SharedPrefsUtils.clearPrefs();
            mAuth.signOut();
            goToAuthActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void goToAuthActivity() {
        Intent i = new Intent(this, AuthActivity.class);
        startActivity(i);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_locked:
                goToLockedActivity(ProtectedActivity.ACTION_PROTECTED);
                break;
            case R.id.nav_favourites:
                goToLockedActivity(ProtectedActivity.ACTION_STARRED);
                break;
            case R.id.nav_settings:
                goToSettingsActivity();
                break;
            case R.id.nav_about:
                goToAbout();
                break;
            case R.id.nav_feedback:
                Utils.sendFeedback(this);
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void goToLockedActivity(String action) {
        Intent protectedIntent = new Intent(this, ProtectedActivity.class);
        protectedIntent.setAction(action);
        startActivity(protectedIntent);
    }

    private void goToAbout() {
        Intent i = new Intent(this, AboutActivity.class);
        startActivity(i);
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

    private void goToSettingsActivity() {
        Intent i = new Intent(this, PreferenceActivity.class);
        startActivity(i);
    }

    private void showPinErrorDialog() {
        new MaterialDialog.Builder(this)
                .title("Error")
                .content("The pin entered was wrong. Please try again")
                .neutralText("Ok")
                .show();
    }


    private void goToAddNotesOnEdit(Note note) {
        Intent i = new Intent(this, AddNotesActivity.class);
        i.setAction(AppConstants.ACTION_EDIT_NOTE);
        i.putExtra(AppConstants.NOTE_EXTRA, note);
        startActivity(i);
    }

}
