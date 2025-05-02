package com.mellah.mediassist;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.*;

/**
 * AI Doctor chat UI backed by Google's Generative Language (Gemini) API.
 */
public class AiDoctorActivity extends AppCompatActivity {
    private static final String GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY;
    private static final String GEMINI_ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta2/"
                    + "models/chat-bison-001:generateMessage?key="
                    + GEMINI_API_KEY;

    private RecyclerView rvChat;
    private EditText    etMessage;
    private Button      btnSend;
    private ChatAdapter adapter;
    private final List<ChatMessage> messages = new ArrayList<>();

    private final OkHttpClient http = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_doctor);

        rvChat    = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend   = findViewById(R.id.btnSend);

        adapter = new ChatAdapter(messages);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (text.isEmpty()) return;
            etMessage.setText("");
            appendMessage(text, true);
            callGemini(text);
        });
    }

    private void appendMessage(String txt, boolean isUser) {
        messages.add(new ChatMessage(txt, isUser));
        adapter.notifyItemInserted(messages.size() - 1);
        rvChat.scrollToPosition(messages.size() - 1);
    }

    private void callGemini(String userText) {
        // Build messages array exactly as the REST quickstart shows:
        JsonArray msgs = new JsonArray();
        // system instruction
        JsonObject sys = new JsonObject();
        sys.addProperty("content",
                "You are MediDoc, an AI doctor with deep medical knowledge. Answer succinctly and safely."
        );
        msgs.add(sys);
        // user question
        JsonObject usr = new JsonObject();
        usr.addProperty("content", userText);
        msgs.add(usr);

        // wrap into prompt
        JsonObject prompt = new JsonObject();
        prompt.add("messages", msgs);

        // full body
        JsonObject body = new JsonObject();
        body.add("prompt", prompt);
        // optional tuning
        body.addProperty("temperature",    0.2);
        body.addProperty("candidate_count", 1);
        body.addProperty("topP",           0.8);
        body.addProperty("topK",           10);

        RequestBody reqBody = RequestBody.create(
                body.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(GEMINI_ENDPOINT)
                .post(reqBody)
                .build();

        http.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(AiDoctorActivity.this,
                                "AI call failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show()
                );
            }
            @Override public void onResponse(Call call, Response res) throws IOException {
                if (!res.isSuccessful()) {
                    onFailure(call, new IOException("HTTP " + res.code()));
                    return;
                }
                JsonObject root = JsonParser
                        .parseString(res.body().string())
                        .getAsJsonObject();
                String aiText = root
                        .getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()
                        .get("content").getAsString();

                runOnUiThread(() -> appendMessage(aiText, false));
            }
        });
    }
}
