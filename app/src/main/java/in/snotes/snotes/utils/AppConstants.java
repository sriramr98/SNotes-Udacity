package in.snotes.snotes.utils;

public final class AppConstants {

    public static final String USER_NOTES = "notes";

    private AppConstants() {
    }

    // Constants for notifications
    public static final String REMAINDER_NOTIFICATION_CHANNEL_ID = "remaider-notification-channe-id";
    public static final int NOTIFICATION_ID = 98;

    public static final String ACTION_ADD_NEW_NOTE = "add-new-note";


    public static final String EXTRA_USER_UID = "user-uid";
    public static final String EXTRA_USER_NAME = "user-name";

    public static final String REFERENCE_USERS = "users";

    // note fields
    public static final String NOTE_TITLE = "title";
    public static final String NOTE_CONTENT = "content";
    public static final String NOTE_COLOR = "colorOfNote";
    public static final String NOTE_IS_LOCKED = "locked";
    public static final String NOTE_IS_STARRED = "starred";
    public static final String NOTE_IS_REMAINDE_SET = "remainderSet";
    public static final String NOTE_REMAINDER_TIME = "remainderTime";
    public static final String NOTE_CREATED_TIMESTAMP = "timestamp";

    public static final String ACTION_EDIT_NOTE = "action-edit-note";
    public static final String NOTE_EXTRA = "note-extra";

    // constants for AddNotesBottomFragment
    public static final String TAG_REMAINDER = "remainder";
    public static final String TAG_DELETE = "delete";
    public static final String TAG_EXPORT = "export";
    public static final String TAG_COPY = "copy";
    public static final String TAG_SHARE = "share";
    public static final String TAG_DEFAULT = "default";

}
