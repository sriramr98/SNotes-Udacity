package in.snotes.snotes.view.addnotes;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.snotes.snotes.R;
import in.snotes.snotes.model.Note;
import in.snotes.snotes.service.NotesService;
import in.snotes.snotes.utils.AppConstants;
import in.snotes.snotes.utils.FirebaseUtils;
import in.snotes.snotes.utils.Utils;
import in.snotes.snotes.view.AddNotesBottomSheet;
import io.github.mthli.knife.KnifeText;
import timber.log.Timber;

import static in.snotes.snotes.utils.AddNotesUtils.tintIcon;
import static in.snotes.snotes.utils.AddNotesUtils.tintMenuIcon;

public class AddNotesActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback, AddNotesBottomSheet.BottomSheetListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    @BindView(R.id.toolbar_notes_add)
    Toolbar toolbarNotesAdd;
    @BindView(R.id.edt_content)
    KnifeText edtContent;
    @BindView(R.id.title_notes_add)
    EditText titleNotesAdd;
    @BindView(R.id.undo)
    ImageButton undo;
    @BindView(R.id.redo)
    ImageButton redo;
    @BindView(R.id.bold)
    ImageButton bold;
    @BindView(R.id.italic)
    ImageButton italic;
    @BindView(R.id.underline)
    ImageButton underline;
    @BindView(R.id.strikethrough)
    ImageButton strikethrough;
    @BindView(R.id.bullet)
    ImageButton bullet;
    @BindView(R.id.quote)
    ImageButton quote;
    @BindView(R.id.link)
    ImageButton link;
    @BindView(R.id.clear)
    ImageButton clear;
    @BindView(R.id.tools)
    HorizontalScrollView tools;

    private static final int defaultColor = -16777216;
    private Menu menu;
    private static String CURRENT_ACTION;

    private Note currentNote;

    private AddNotesBottomSheet bottomSheetFragment;

    private Calendar remainder = Calendar.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notes);
        ButterKnife.bind(this);

        setSupportActionBar(toolbarNotesAdd);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        // for responsive bottom bar
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        Intent i = getIntent();

        if (i == null) {
            Timber.e("Intent is null");
            finish();
            return;
        }

        CURRENT_ACTION = i.getAction();

        if (AppConstants.ACTION_ADD_NEW_NOTE.equals(CURRENT_ACTION)) {
            changeColorBottomBar(defaultColor);
            currentNote = new Note();
            currentNote.setColorOfNote(defaultColor);
        } else if (AppConstants.ACTION_EDIT_NOTE.equals(CURRENT_ACTION)) {
            this.currentNote = i.getParcelableExtra(AppConstants.NOTE_EXTRA);
            changeColorBottomBar(currentNote.getColorOfNote());
            titleNotesAdd.setText(currentNote.getTitle());
            edtContent.fromHtml(currentNote.getContent());
        }

    }

    @Override
    public void onBackPressed() {
        String title = titleNotesAdd.getText().toString().trim();
        String content = edtContent.toHtml().trim();
        currentNote.setTitle(title);
        currentNote.setContent(content);

        if (CURRENT_ACTION.equals(AppConstants.ACTION_ADD_NEW_NOTE)) {
            currentNote.setTimestamp(System.currentTimeMillis());
            // if the either the title or content is present, then add to database.
            if (!TextUtils.isEmpty(title) || !TextUtils.isEmpty(content)) {
                FirebaseUtils.addNoteToUser(this, currentNote.getNoteMap());
            }
        } else if (CURRENT_ACTION.equals(AppConstants.ACTION_EDIT_NOTE)) {
            // if both title and the content are empty, then delete the note
            if (TextUtils.isEmpty(title) && TextUtils.isEmpty(content)) {
                FirebaseUtils.deleteNote(this, currentNote);
            } else {
                // else update the note
                FirebaseUtils.updateNote(this, currentNote);
            }
        }
        super.onBackPressed();
    }

    @OnClick({R.id.bold, R.id.italic, R.id.underline, R.id.strikethrough, R.id.bullet, R.id.quote, R.id.link, R.id.clear, R.id.undo, R.id.redo})
    public void onFormatClicked(View view) {
        switch (view.getId()) {
            case R.id.bold:
                edtContent.bold(!edtContent.contains(KnifeText.FORMAT_BOLD));
                break;
            case R.id.italic:
                edtContent.italic(!edtContent.contains(KnifeText.FORMAT_ITALIC));
                break;
            case R.id.underline:
                edtContent.underline(!edtContent.contains(KnifeText.FORMAT_UNDERLINED));
                break;
            case R.id.strikethrough:
                edtContent.strikethrough(!edtContent.contains(KnifeText.FORMAT_STRIKETHROUGH));
                break;
            case R.id.bullet:
                edtContent.bullet(!edtContent.contains(KnifeText.FORMAT_BULLET));
                break;
            case R.id.quote:
                edtContent.quote(!edtContent.contains(KnifeText.FORMAT_QUOTE));
                break;
            case R.id.link:
                showLinkDialog();
                break;
            case R.id.clear:
                edtContent.clearFormats();
                break;
            case R.id.undo:
                edtContent.undo();
                break;
            case R.id.redo:
                edtContent.redo();
                break;
        }

    }

    private void showLinkDialog() {
        final int start = edtContent.getSelectionStart();
        final int end = edtContent.getSelectionEnd();
        new MaterialDialog.Builder(this)
                .title("Enter a link")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("www.google.com", "", (dialog, input) -> {
                    // Do something
                    if (TextUtils.isEmpty(input.toString())) {
                        return;
                    }
                    edtContent.link(input.toString(), start, end);
                }).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notes, menu);
        this.menu = menu;

        int colorToSet;

        MenuItem lock = menu.findItem(R.id.action_lock);
        MenuItem star = menu.findItem(R.id.action_star);
        MenuItem color = menu.findItem(R.id.action_color_choser);
        MenuItem archive = menu.findItem(R.id.action_archive);
        MenuItem menuBtn = menu.findItem(R.id.action_menu);

        if (AppConstants.ACTION_ADD_NEW_NOTE.equals(CURRENT_ACTION)) {
            archive.setVisible(false);
            menuBtn.setVisible(false);
            colorToSet = defaultColor;
        } else if (AppConstants.ACTION_EDIT_NOTE.equals(CURRENT_ACTION)) {
            archive.setVisible(true);
            menuBtn.setVisible(true);
            if (currentNote.getLocked()) {
                lock.setIcon(R.drawable.ic_lock_black);
            } else {
                lock.setIcon(R.drawable.ic_lock_open);
            }
            if (currentNote.getStarred()) {
                star.setIcon(R.drawable.ic_star_closed);
            } else {
                star.setIcon(R.drawable.ic_star_open);
            }
            colorToSet = currentNote.getColorOfNote();
        } else {
            // this is just so that we won't get an error.
            colorToSet = defaultColor;
        }

        tintMenuIcon(star, colorToSet);
        tintMenuIcon(lock, colorToSet);
        tintMenuIcon(color, colorToSet);
        tintMenuIcon(archive, colorToSet);
        tintMenuIcon(menuBtn, colorToSet);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_lock:
                lockNote(item);
                break;
            case R.id.action_archive:
                deleteNote();
                break;
            case R.id.action_color_choser:
                showColorChooser();
                break;
            case R.id.action_star:
                starNote(item);
                break;
            case R.id.action_menu:
                showMenuSheet();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMenuSheet() {
        bottomSheetFragment = AddNotesBottomSheet.newInstance(currentNote);
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
    }

    private void starNote(MenuItem starItem) {
        if (currentNote.getStarred()) {
            starItem.setIcon(R.drawable.ic_star_open);
        } else {
            starItem.setIcon(R.drawable.ic_star_closed);
        }

        tintMenuIcon(starItem, currentNote.getColorOfNote());
        currentNote.alternateStar();
        Timber.i(currentNote.toString());
    }

    private void showColorChooser() {
        new ColorChooserDialog.Builder(this, R.string.color_title)
                .titleSub(R.string.colors)  // title of dialog when viewing shades of a color
                .doneButton(R.string.md_done_label)  // changes label of the done button
                .cancelButton(R.string.md_cancel_label)  // changes label of the cancel button
                .backButton(R.string.md_back_label)  // changes label of the back button
                .dynamicButtonColor(true)  // defaults to true, false will disable changing action buttons' color to currently selected color
                .show(this); // an AppCompatActivity which implements ColorCallback

    }

    private void deleteNote() {
        FirebaseUtils.deleteNote(this, currentNote);
        finish();
    }

    private void lockNote(MenuItem lockItem) {
        if (currentNote.getLocked()) {
            lockItem.setIcon(R.drawable.ic_lock_open);
        } else {
            lockItem.setIcon(R.drawable.ic_lock_black);
        }

        tintMenuIcon(lockItem, currentNote.getColorOfNote());
        currentNote.alternateLock();

        Timber.i(currentNote.toString());
    }


    public void changeColorBottomBar(int color) {

        ImageView[] views = {
                findViewById(R.id.undo),
                findViewById(R.id.redo),
                findViewById(R.id.bold),
                findViewById(R.id.italic),
                findViewById(R.id.underline),
                findViewById(R.id.strikethrough),
                findViewById(R.id.bullet),
                findViewById(R.id.quote),
                findViewById(R.id.link),
                findViewById(R.id.clear)
        };

        for (ImageView view : views) {
            tintIcon(view, color);
        }

        EditText edt_title = findViewById(R.id.title_notes_add);
        edt_title.setTextColor(color);
        edt_title.setHintTextColor(color);

    }


    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, int selectedColor) {
        currentNote.setColorOfNote(selectedColor);
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
        if (menu == null) {
            return;
        }

        int color = currentNote.getColorOfNote();
        tintMenuIcon(menu.findItem(R.id.action_archive), color);
        tintMenuIcon(menu.findItem(R.id.action_color_choser), color);
        tintMenuIcon(menu.findItem(R.id.action_lock), color);
        tintMenuIcon(menu.findItem(R.id.action_star), color);
        tintMenuIcon(menu.findItem(R.id.action_menu), color);

        changeColorBottomBar(currentNote.getColorOfNote());

        titleNotesAdd.setTextColor(color);
        titleNotesAdd.setHintTextColor(color);

    }

    @Override
    public void onBottomSheetItemClicked(String tag) {
        switch (tag) {
            case AppConstants.TAG_DELETE:
                deleteNote();
                break;
            case AppConstants.TAG_REMAINDER:
                setRemainder();
                break;
            case AppConstants.TAG_COPY:
                copyNote();
                break;
            case AppConstants.TAG_SHARE:
                shareNote();
                break;
        }

        bottomSheetFragment.dismiss();
    }

    private void shareNote() {

        String contentToSend = "Title :" + titleNotesAdd.getText().toString().trim() + " \nContent :" + (Html.fromHtml(edtContent.toHtml().trim()).toString());
        Intent i = new Intent();
        i.setAction(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_TEXT, contentToSend);
        i.setType("text/plain");
        startActivity(Intent.createChooser(i, getResources().getText(R.string.send_to)));
    }

    private void copyNote() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(titleNotesAdd.getText().toString().trim(), Html.fromHtml(edtContent.toHtml().trim()));
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(clip);
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Could'nt copy to clipboard", Toast.LENGTH_SHORT).show();
        }
    }

    private void setRemainder() {
        if (currentNote.getRemainderSet()) {
            showRemainderCancelDialog();
        } else {
            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    this,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );
            dpd.show(getFragmentManager(), "Datepickerdialog");
        }
    }

    private void showRemainderCancelDialog() {
        new MaterialDialog.Builder(this)
                .title("Remainder set")
                .content("Remainder already set for " + Utils.getDate(currentNote.getRemainderTime()) + ". Do you want to cancel the remainder?")
                .positiveText("Yes")
                .negativeText("No")
                .onPositive((dialog, which) -> {
                    // updating local note
                    currentNote.setRemainderSet(false);
                    currentNote.setRemainderTime(0);

                    // updating the cloud
                    Intent cancelService = new Intent(this, NotesService.class);
                    cancelService.setAction(NotesService.ACTION_CANCEL_REMAINDER);
                    cancelService.putExtra(AppConstants.NOTE_EXTRA, currentNote);
                    Toast.makeText(this, "Remainder cancelled", Toast.LENGTH_SHORT).show();
                }).show();
    }


    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        remainder.set(year, monthOfYear, dayOfMonth);
        showTimeDialog();
    }

    private void showTimeDialog() {
        Calendar now = Calendar.getInstance();
        TimePickerDialog dpd = TimePickerDialog.newInstance(
                AddNotesActivity.this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                now.get(Calendar.SECOND),
                false
        );
        dpd.show(getFragmentManager(), "Datepickerdialog");
    }


    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        remainder.set(Calendar.HOUR_OF_DAY, hourOfDay);
        remainder.set(Calendar.MINUTE, minute);
        remainder.set(Calendar.SECOND, 0);

        currentNote.setRemainderSet(true);
        currentNote.setRemainderTime(remainder.getTimeInMillis());

        Utils.scheduleAlarm(this, currentNote);

        Toast.makeText(this, "Remainder set", Toast.LENGTH_SHORT).show();

        FirebaseUtils.updateNote(this, currentNote);
    }

}
