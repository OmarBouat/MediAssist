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
 * AI Doctor chat UI backed by Gemini 2.0 Flash via the Vertex AI generateContent API.
 */
public class AiDoctorActivity extends AppCompatActivity {
    private static final String GEMINI_API_KEY  = BuildConfig.GEMINI_API_KEY;
    private static final String GEMINI_ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + "gemini-2.0-flash:generateContent?key="
                    + GEMINI_API_KEY;

    private RecyclerView       rvChat;
    private EditText           etMessage;
    private Button             btnSend;
    private ChatAdapter        adapter;
    private final List<ChatMessage> messages = new ArrayList<>();
    private final OkHttpClient http     = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_doctor);

        rvChat    = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend   = findViewById(R.id.btnSend);

        adapter = new ChatAdapter(this, messages);
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

    /** Append a message (user or AI) to the chat and scroll. */
    private void appendMessage(String txt, boolean isUser) {
        messages.add(new ChatMessage(txt, isUser));
        adapter.notifyItemInserted(messages.size() - 1);
        rvChat.scrollToPosition(messages.size() - 1);
    }

    /** Kick off the network call (with retries if you like). */
    private void callGemini(String userText) {
        // Build the request body, including "role":"USER"
        JsonObject part = new JsonObject();
        part.addProperty("text", userText);

        JsonArray partsArray = new JsonArray();
        partsArray.add(part);

        JsonObject contentEntry = new JsonObject();
        contentEntry.addProperty("role", "USER");       // ← important!
        contentEntry.add("parts", partsArray);

        JsonArray contentsArray = new JsonArray();
        contentsArray.add(contentEntry);

        JsonObject body = new JsonObject();
        body.add("contents", contentsArray);

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

                // Parse the JSON response
                JsonObject root = JsonParser
                        .parseString(res.body().string())
                        .getAsJsonObject();

                JsonArray candidates = root.getAsJsonArray("candidates");
                if (candidates == null || candidates.size() == 0) {
                    runOnUiThread(() ->
                            Toast.makeText(AiDoctorActivity.this,
                                    "No response from AI",
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
                    return;
                }

                JsonObject candidate = candidates.get(0).getAsJsonObject();
                JsonObject contentObj = candidate.getAsJsonObject("content");
                JsonArray aiParts    = contentObj.getAsJsonArray("parts");

                String aiText = "";
                if (aiParts != null && aiParts.size() > 0) {
                    aiText = aiParts
                            .get(0).getAsJsonObject()
                            .get("text").getAsString();
                } else {
                    // fallback if schema changes
                    aiText = contentObj.toString();
                }

                String finalText = aiText.trim();
                runOnUiThread(() -> appendMessage(finalText, false));
            }
        });
    }
}
