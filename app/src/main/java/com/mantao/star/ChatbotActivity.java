package com.mantao.star;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ChatbotActivity extends AppCompatActivity {

    // ─── Chat views ───
    private ScrollView chatScrollView;
    private LinearLayout chatContainer;
    private EditText etMessage;
    private TextView btnSend, btnAttach;
    private TextView chip1, chip2, chip3;

    // ─── Bottom nav views ───
    private LinearLayout navHome, navLocate, navEco, navHistory;

    // ─── State ───
    private final List<ChatMessage> history = new ArrayList<>();
    private GroqApiClient apiClient;
    private boolean isSending = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.chatbot_activity);

        apiClient = new GroqApiClient();

        initViews();
        setupBottomNav();
        setupInput();
        setupSuggestions();
    }

    // ════════════════════════════════════════
    //  Init Views
    // ════════════════════════════════════════

    private void initViews() {
        chatScrollView = findViewById(R.id.chatScrollView);
        chatContainer  = findViewById(R.id.chatContainer);
        etMessage      = findViewById(R.id.etMessage);
        btnSend        = findViewById(R.id.btnSend);
        btnAttach      = findViewById(R.id.btnAttach);
        chip1          = findViewById(R.id.chip1);
        chip2          = findViewById(R.id.chip2);
        chip3          = findViewById(R.id.chip3);

        navHome    = findViewById(R.id.navHome);
        navLocate  = findViewById(R.id.navLocate);
        navEco     = findViewById(R.id.navEco);
        navHistory = findViewById(R.id.navHistory);
    }

    // ════════════════════════════════════════
    //  Input & Suggestions
    // ════════════════════════════════════════

    private void setupInput() {
        btnSend.setOnClickListener(v -> trySendCurrentInput());

        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                trySendCurrentInput();
                return true;
            }
            return false;
        });

        btnAttach.setOnClickListener(v ->
                Toast.makeText(this, "Fitur lampiran belum tersedia", Toast.LENGTH_SHORT).show());
    }

    private void setupSuggestions() {
        chip1.setOnClickListener(v -> sendMessage("Bagaimana cara memilah sampah plastik dengan benar?"));
        chip2.setOnClickListener(v -> sendMessage("Kapan jadwal pengangkutan sampah hari ini?"));
        chip3.setOnClickListener(v -> sendMessage("Di mana lokasi bank sampah terdekat?"));
    }

    private void trySendCurrentInput() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;
        etMessage.setText("");
        sendMessage(text);
    }

    // ════════════════════════════════════════
    //  Kirim pesan ke Groq API
    // ════════════════════════════════════════

    private void sendMessage(String text) {
        if (isSending) {
            Toast.makeText(this, "Tunggu balasan sebelumnya selesai dulu", Toast.LENGTH_SHORT).show();
            return;
        }

        appendBubble(text, true);

        isSending = true;
        View typingIndicator = appendTypingIndicator();

        apiClient.sendMessage(history, text, new GroqApiClient.ApiCallback() {
            @Override
            public void onSuccess(String reply) {
                isSending = false;
                chatContainer.removeView(typingIndicator);
                appendBubble(reply, false);

                // Simpan ke history HANYA setelah sukses, biar konteks gak duplikat/rusak
                history.add(new ChatMessage(text, true));
                history.add(new ChatMessage(reply, false));
            }

            @Override
            public void onError(String message) {
                isSending = false;
                chatContainer.removeView(typingIndicator);
                appendBubble("Maaf, terjadi kendala: " + message, false);
            }
        });
    }

    // ════════════════════════════════════════
    //  Render bubble chat
    // ════════════════════════════════════════

    private void appendBubble(String text, boolean fromUser) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View bubbleView = inflater.inflate(
                fromUser ? R.layout.item_chat_user : R.layout.item_chat_bot,
                chatContainer, false);

        TextView tvMessage = bubbleView.findViewById(R.id.tvMessage);
        tvMessage.setText(text);

        chatContainer.addView(bubbleView);
        scrollToBottom();
    }

    private View appendTypingIndicator() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View bubbleView = inflater.inflate(R.layout.item_chat_bot, chatContainer, false);
        TextView tvMessage = bubbleView.findViewById(R.id.tvMessage);
        tvMessage.setText("Oasis sedang mengetik...");
        chatContainer.addView(bubbleView);
        scrollToBottom();
        return bubbleView;
    }

    private void scrollToBottom() {
        chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
    }

    // ════════════════════════════════════════
    //  Bottom Navigation (struktur sama dengan ScanActivity)
    // ════════════════════════════════════════

    private void setupBottomNav() {
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        navLocate.setOnClickListener(v ->
                startActivity(new Intent(this, LocateActivity.class)));

        navEco.setOnClickListener(v ->
                startActivity(new Intent(this, EcoActivity.class)));

        navHistory.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));

        View navScanView = findViewById(R.id.navScan);
        if (navScanView != null) {
            navScanView.setOnClickListener(v ->
                    startActivity(new Intent(this, ScanActivity.class)));
        }
    }
}