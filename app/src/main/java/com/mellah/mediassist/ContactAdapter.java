package com.mellah.mediassist;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    public interface OnContactActionListener {
        void onEdit(int contactId, String name, String phone, String relation);
        void onDelete(int contactId);
    }

    private final Context context;
    private Cursor cursor;
    private final OnContactActionListener listener;

    public ContactAdapter(Context context, Cursor cursor, OnContactActionListener listener) {
        this.context = context;
        this.cursor = cursor;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        if (!cursor.moveToPosition(pos)) return;

        int id       = cursor.getInt(cursor.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_EC_ID));
        String name  = cursor.getString(cursor.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_EC_NAME));
        String phone = cursor.getString(cursor.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_EC_PHONE));
        String rel   = cursor.getString(cursor.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_EC_RELATION));

        holder.tvName.setText(name);
        holder.tvPhone.setText(phone);

        // Call
        holder.btnCall.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phone));
            context.startActivity(callIntent);
        });
        // Edit
        holder.btnEdit.setOnClickListener(v ->
                listener.onEdit(id, name, phone, rel)
        );
        // Delete
        holder.btnDelete.setOnClickListener(v ->
                listener.onDelete(id)
        );
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone;
        ImageButton btnCall, btnEdit, btnDelete;
        ViewHolder(View itemView) {
            super(itemView);
            tvName    = itemView.findViewById(R.id.tvContactName);
            tvPhone   = itemView.findViewById(R.id.tvContactPhone);
            btnCall   = itemView.findViewById(R.id.btnCall);
            btnEdit   = itemView.findViewById(R.id.btnEditContact);
            btnDelete = itemView.findViewById(R.id.btnDeleteContact);
        }
    }
}