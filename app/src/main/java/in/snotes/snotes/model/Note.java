package in.snotes.snotes.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

import static in.snotes.snotes.utils.AppConstants.NOTE_COLOR;
import static in.snotes.snotes.utils.AppConstants.NOTE_CONTENT;
import static in.snotes.snotes.utils.AppConstants.NOTE_CREATED_TIMESTAMP;
import static in.snotes.snotes.utils.AppConstants.NOTE_IS_LOCKED;
import static in.snotes.snotes.utils.AppConstants.NOTE_IS_REMAINDE_SET;
import static in.snotes.snotes.utils.AppConstants.NOTE_IS_STARRED;
import static in.snotes.snotes.utils.AppConstants.NOTE_REMAINDER_TIME;
import static in.snotes.snotes.utils.AppConstants.NOTE_TITLE;

public class Note implements Parcelable {

    private String id;
    private String title;
    private String content;
    private boolean locked;
    private int colorOfNote;
    private long timestamp;
    private boolean starred;
    private boolean remainderSet;
    private long remainderTime;

    public Note(String title, String content, boolean isLocked, int colorOfNote, long timestamp, boolean isStarred, boolean isRemainderSet, long remainderTime) {
        this.title = title;
        this.content = content;
        this.locked = isLocked;
        this.colorOfNote = colorOfNote;
        this.timestamp = timestamp;
        this.starred = isStarred;
        this.remainderSet = isRemainderSet;
        this.remainderTime = remainderTime;
    }

    public Note() {
        // default black color for every note
        this.colorOfNote = -16777216;
        this.locked = false;
        this.starred = false;
        this.remainderSet = false;
        this.remainderTime = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean getLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public int getColorOfNote() {
        return colorOfNote;
    }

    public void setColorOfNote(int colorOfNote) {
        this.colorOfNote = colorOfNote;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean getStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    public boolean getRemainderSet() {
        return remainderSet;
    }

    public void setRemainderSet(boolean remainderSet) {
        this.remainderSet = remainderSet;
    }

    public long getRemainderTime() {
        return remainderTime;
    }

    public void setRemainderTime(long remainderTime) {
        this.remainderTime = remainderTime;
    }



    public void alternateStar() {
        starred = !starred;
    }

    public void alternateLock() {
        locked = !locked;
    }

    public HashMap<String, Object> getNoteMap() {
        HashMap<String, Object> note = new HashMap<>();
        note.put(NOTE_TITLE, title);
        note.put(NOTE_CONTENT, content);
        note.put(NOTE_IS_LOCKED, locked);
        note.put(NOTE_IS_STARRED, starred);
        note.put(NOTE_IS_REMAINDE_SET, remainderSet);
        note.put(NOTE_REMAINDER_TIME, remainderTime);
        note.put(NOTE_CREATED_TIMESTAMP, timestamp);
        note.put(NOTE_COLOR, colorOfNote);

        return note;
    }

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id + '\'' +
                ",title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", getisLocked=" + locked +
                ", colorOfNote=" + colorOfNote +
                ", timestamp=" + timestamp +
                ", starred=" + starred +
                ", remainderSet=" + remainderSet +
                ", remainderTime=" + remainderTime +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.title);
        dest.writeString(this.content);
        dest.writeByte(this.locked ? (byte) 1 : (byte) 0);
        dest.writeInt(this.colorOfNote);
        dest.writeLong(this.timestamp);
        dest.writeByte(this.starred ? (byte) 1 : (byte) 0);
        dest.writeByte(this.remainderSet ? (byte) 1 : (byte) 0);
        dest.writeLong(this.remainderTime);
    }

    protected Note(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.content = in.readString();
        this.locked = in.readByte() != 0;
        this.colorOfNote = in.readInt();
        this.timestamp = in.readLong();
        this.starred = in.readByte() != 0;
        this.remainderSet = in.readByte() != 0;
        this.remainderTime = in.readLong();
    }

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel source) {
            return new Note(source);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };
}