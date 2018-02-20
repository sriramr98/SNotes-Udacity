package in.snotes.snotes.utils;

import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.MenuItem;
import android.widget.ImageView;

public final class AddNotesUtils {

    private AddNotesUtils() {
    }

    public static void tintMenuIcon(MenuItem item, int color) {
        Drawable normalDrawable = item.getIcon();
        Drawable wrapDrawable = DrawableCompat.wrap(normalDrawable);
        DrawableCompat.setTint(wrapDrawable, color);

        item.setIcon(wrapDrawable);
    }

    public static void tintIcon(ImageView view, int color) {
        Drawable normalDrawable = view.getDrawable();
        Drawable wrapDrawable = DrawableCompat.wrap(normalDrawable);

        DrawableCompat.setTint(wrapDrawable, color);
        view.setImageDrawable(wrapDrawable);
    }


}
