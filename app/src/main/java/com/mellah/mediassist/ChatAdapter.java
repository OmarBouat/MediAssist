package com.mellah.mediassist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter
        extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private final List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = viewType == 0
                ? R.layout.item_chat_user
                : R.layout.item_chat_ai;
        View v = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        holder.tv.setText(messages.get(pos).text);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser ? 0 : 1;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv;
        ViewHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tvChat);
        }
    }
}
