package com.mellah.mediassist;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

public class PrescriptionAdapter extends RecyclerView.Adapter<PrescriptionAdapter.ViewHolder> {
    private final Context context;
    private Cursor cursor;

    public PrescriptionAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRx;
        TextView tvDesc;
        public ViewHolder(View itemView) {
            super(itemView);
            ivRx   = itemView.findViewById(R.id.ivPrescription);
            tvDesc = itemView.findViewById(R.id.tvRxDesc);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_prescription, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (!cursor.moveToPosition(position)) return;
        String path = cursor.getString(cursor.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_RX_IMAGE_PATH));
        String desc = cursor.getString(cursor.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_RX_DESCRIPTION));
        holder.tvDesc.setText(desc != null ? desc : "");
        holder.ivRx.setImageBitmap(BitmapFactory.decodeFile(path));
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (cursor != null) cursor.close();
        cursor = newCursor;
        notifyDataSetChanged();
    }
}