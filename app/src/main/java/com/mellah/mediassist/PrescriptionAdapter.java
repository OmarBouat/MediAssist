package com.mellah.mediassist;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

public class PrescriptionAdapter
        extends RecyclerView.Adapter<PrescriptionAdapter.ViewHolder> {

    public interface OnPrescriptionActionListener {
        void onEdit(int rxId, String imagePath, String description);
        void onDelete(int rxId);
    }

    private final Context context;
    private Cursor cursor;
    private final OnPrescriptionActionListener listener;

    public PrescriptionAdapter(Context context,
                               Cursor cursor,
                               OnPrescriptionActionListener listener) {
        this.context = context;
        this.cursor = cursor;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_prescription, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (!cursor.moveToPosition(position)) return;

        int id = cursor.getInt(
                cursor.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_RX_ID)
        );
        String path = cursor.getString(
                cursor.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_RX_IMAGE_PATH)
        );
        String desc = cursor.getString(
                cursor.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_RX_DESCRIPTION)
        );

        holder.tvDesc.setText(desc != null ? desc : "");

        // load via Glide using a real URI
        if (path != null && !path.isEmpty()) {
            Uri imageUri = Uri.parse(path);
            Glide.with(context)
                    .load(imageUri)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.ivRx);

            // **full-screen on tap**
            holder.ivRx.setOnClickListener(v -> {
                Intent i = new Intent(context, FullScreenImageActivity.class);
                i.putExtra("imageUri", path);
                context.startActivity(i);
            });
        } else {
            holder.ivRx.setImageResource(R.drawable.placeholder);
        }

        holder.btnEdit.setOnClickListener(v ->
                listener.onEdit(id, path, desc)
        );
        holder.btnDelete.setOnClickListener(v ->
                listener.onDelete(id)
        );
    }

    @Override
    public int getItemCount() {
        return (cursor == null) ? 0 : cursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (cursor != null) cursor.close();
        cursor = newCursor;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRx;
        TextView tvDesc;
        Button btnEdit, btnDelete;
        ViewHolder(View itemView) {
            super(itemView);
            ivRx      = itemView.findViewById(R.id.ivPrescription);
            tvDesc    = itemView.findViewById(R.id.tvRxDesc);
            btnEdit   = itemView.findViewById(R.id.btnEditRx);
            btnDelete = itemView.findViewById(R.id.btnDeleteRx);
        }
    }
}
