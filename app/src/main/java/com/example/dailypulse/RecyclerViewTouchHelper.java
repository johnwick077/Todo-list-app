package com.example.dailypulse;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewTouchHelper extends ItemTouchHelper.SimpleCallback {

    private ToDoAdapter adapter;
    private Drawable editIcon;
    private Drawable deleteIcon;
    private final ColorDrawable editBackground;
    private final ColorDrawable deleteBackground;
    private final int intrinsicWidth;
    private final int intrinsicHeight;

    public RecyclerViewTouchHelper(ToDoAdapter adapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
        this.editIcon = ContextCompat.getDrawable(adapter.getContext(), R.drawable.edit);
        this.deleteIcon = ContextCompat.getDrawable(adapter.getContext(), R.drawable.delete);
        this.editBackground = new ColorDrawable(ContextCompat.getColor(adapter.getContext(), R.color.colorPrimaryDark));
        this.deleteBackground = new ColorDrawable(Color.RED);

        // Initialize intrinsic dimensions if icons are not null
        if (editIcon != null) {
            this.intrinsicWidth = editIcon.getIntrinsicWidth();
            this.intrinsicHeight = editIcon.getIntrinsicHeight();
        } else if (deleteIcon != null) {
            this.intrinsicWidth = deleteIcon.getIntrinsicWidth();
            this.intrinsicHeight = deleteIcon.getIntrinsicHeight();
        } else {
            this.intrinsicWidth = 0;
            this.intrinsicHeight = 0;
        }
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();
        if (direction == ItemTouchHelper.RIGHT) {
            AlertDialog.Builder builder = new AlertDialog.Builder(adapter.getContext());
            builder.setTitle("Delete Task");
            builder.setMessage("Are You Sure?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    adapter.deleteTask(position);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    adapter.notifyItemChanged(position);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            adapter.editItem(position);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        int itemViewHeight = viewHolder.itemView.getBottom() - viewHolder.itemView.getTop();
        int iconMargin = (itemViewHeight - intrinsicHeight) / 2;
        int iconTop = viewHolder.itemView.getTop() + iconMargin;
        int iconBottom = iconTop + intrinsicHeight;

        if (dX > 0) { // Swiping to the right
            deleteBackground.setBounds(viewHolder.itemView.getLeft(), viewHolder.itemView.getTop(), (int) dX, viewHolder.itemView.getBottom());
            deleteBackground.draw(c);

            if (deleteIcon != null) {
                int iconLeft = viewHolder.itemView.getLeft() + iconMargin;
                int iconRight = iconLeft + intrinsicWidth;
                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                deleteIcon.draw(c);
            }
        } else if (dX < 0) { // Swiping to the left
            editBackground.setBounds(viewHolder.itemView.getRight() + (int) dX, viewHolder.itemView.getTop(), viewHolder.itemView.getRight(), viewHolder.itemView.getBottom());
            editBackground.draw(c);

            if (editIcon != null) {
                int iconRight = viewHolder.itemView.getRight() - iconMargin;
                int iconLeft = iconRight - intrinsicWidth;
                editIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                editIcon.draw(c);
            }
        }
    }
}
