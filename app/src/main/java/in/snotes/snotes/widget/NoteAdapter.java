package in.snotes.snotes.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.snotes.snotes.R;
import in.snotes.snotes.model.Note;


public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {

    private Context context;
    private List<Note> notes;
    private WidgetNoteListener mListener;

    public NoteAdapter(Context context, WidgetNoteListener listener) {
        this.context = context;
        this.notes = new ArrayList<>();
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_note_widget, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.title.setText(note.getTitle());
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void addNote(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    public interface WidgetNoteListener {
        void onNoteClicked(Note note);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.widget_item_title)
        TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> mListener.onNoteClicked(notes.get(getAdapterPosition())));
        }
    }
}
