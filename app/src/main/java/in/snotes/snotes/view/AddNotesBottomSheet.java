package in.snotes.snotes.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import in.snotes.snotes.R;
import in.snotes.snotes.model.Note;
import in.snotes.snotes.utils.AppConstants;
import in.snotes.snotes.utils.Utils;

public class AddNotesBottomSheet extends BottomSheetDialogFragment {

    Unbinder unbinder;
    @BindView(R.id.sheet_tv_remaider)
    TextView sheetTvRemaider;
    private BottomSheetListener mListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_notes_bottom_sheet, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Note note = getArguments().
                getParcelable("note");

        if (note == null) {
            return;
        }

        if (note.getRemainderSet()) {
            String remainderSetOn = "Remainder set for" + Utils.getDate(note.getRemainderTime());
            sheetTvRemaider.setText(remainderSetOn);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.layout_remainder, R.id.layout_delete, R.id.layout_copy, R.id.layout_share})
    public void onItemClicked(View view) {
        String tag;
        switch (view.getId()) {
            case R.id.layout_remainder:
                tag = AppConstants.TAG_REMAINDER;
                break;
            case R.id.layout_delete:
                tag = AppConstants.TAG_DELETE;
                break;
            case R.id.layout_copy:
                tag = AppConstants.TAG_COPY;
                break;
            case R.id.layout_share:
                tag = AppConstants.TAG_SHARE;
                break;
            default:
                tag = AppConstants.TAG_DEFAULT;
        }
        mListener.onBottomSheetItemClicked(tag);
    }

    public static AddNotesBottomSheet newInstance(Note note) {
        AddNotesBottomSheet sheet = new AddNotesBottomSheet();

        Bundle b = new Bundle();
        b.putParcelable("note", note);

        sheet.setArguments(b);

        return sheet;
    }

    public interface BottomSheetListener {
        void onBottomSheetItemClicked(String tag);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BottomSheetListener) {
            mListener = (BottomSheetListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement BottomSheetListener");
        }
    }
}
