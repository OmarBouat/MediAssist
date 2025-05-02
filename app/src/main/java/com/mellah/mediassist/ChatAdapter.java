package com.mellah.mediassist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import io.noties.markwon.Markwon;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private final List<ChatMessage> messages;
    private final Markwon markwon;
    private final Context context;

    public ChatAdapter(Context context, List<ChatMessage> messages) {
        this.context  = context;
        this.messages = messages;
        // Create a single Markwon instance
        this.markwon  = Markwon.create(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = viewType == 0
                ? R.layout.item_chat_user
                : R.layout.item_chat_ai;
        View v = LayoutInflater.from(context)
                .inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        ChatMessage msg = messages.get(pos);

        // Render markdown rather than plain text
        markwon.setMarkdown(holder.tv, msg.text);

        // (Optional) adjust text color programmatically if needed:
        // holder.tv.setTextColor(
        //     ContextCompat.getColor(context,
        //         msg.isUser ? R.color.white : R.color.black)
        // );
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        // user = 0, AI = 1
        return messages.get(position).isUser ? 0 : 1;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tv;
        ViewHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tvChat);
        }
    }
}
