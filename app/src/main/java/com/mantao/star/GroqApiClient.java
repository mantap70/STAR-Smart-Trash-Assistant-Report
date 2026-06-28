package com.mantao.star;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Klien untuk Groq API (gratis, kompatibel format OpenAI chat completions).
 * Daftar API key gratis di https://console.groq.com (tanpa kartu kredit).
 *
 * Topik dibatasi ke seputar sampah & lingkungan lewat SYSTEM_PROMPT di bawah.
 */
public class GroqApiClient {

    private static final String ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";

    private static final String SYSTEM_PROMPT =
            "Kamu adalah Oasis, asisten AI dalam aplikasi STAR. Tugasmu HANYA membantu pengguna " +
                    "seputar topik sampah dan lingkungan: cara memilah sampah organik/anorganik/B3, " +
                    "jadwal pengangkutan sampah, lokasi bank sampah, daur ulang, jejak karbon, dan gaya " +
                    "hidup berkelanjutan. Jika pengguna bertanya di luar topik ini, tolak dengan sopan dan " +
                    "arahkan kembali ke topik sampah/lingkungan. Jawab singkat (maksimal 4-5 kalimat), jelas, " +
                    "ramah, dan gunakan Bahasa Indonesia.";

    private final OkHttpClient client;

    public GroqApiClient() {
        client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public interface ApiCallback {
        void onSuccess(String reply);
        void onError(String message);
    }

    /**
     * @param history          riwayat percakapan sebelumnya (sudah dikonfirmasi sukses), untuk konteks
     * @param newUserMessage   pesan baru dari pengguna
     * @param callback         dipanggil di main thread
     */
    public void sendMessage(List<ChatMessage> history, String newUserMessage, ApiCallback callback) {
        Handler mainHandler = new Handler(Looper.getMainLooper());

        try {
            JSONArray messages = new JSONArray();

            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", SYSTEM_PROMPT);
            messages.put(systemMsg);

            for (ChatMessage msg : history) {
                JSONObject m = new JSONObject();
                m.put("role", msg.isFromUser() ? "user" : "assistant");
                m.put("content", msg.getText());
                messages.put(m);
            }

            JSONObject newMsg = new JSONObject();
            newMsg.put("role", "user");
            newMsg.put("content", newUserMessage);
            messages.put(newMsg);

            JSONObject body = new JSONObject();
            body.put("model", MODEL);
            body.put("messages", messages);
            body.put("max_tokens", 500);
            body.put("temperature", 0.5);

            RequestBody requestBody = RequestBody.create(
                    body.toString(), MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url(ENDPOINT)
                    .addHeader("Authorization", "Bearer " + BuildConfig.GROQ_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mainHandler.post(() -> callback.onError("Gagal terhubung: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";

                    if (!response.isSuccessful()) {
                        final String snippet = responseBody.length() > 200
                                ? responseBody.substring(0, 200) : responseBody;
                        mainHandler.post(() -> callback.onError(
                                "Server error (" + response.code() + "): " + snippet));
                        return;
                    }

                    try {
                        JSONObject json = new JSONObject(responseBody);
                        String reply = json.getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");
                        mainHandler.post(() -> callback.onSuccess(reply.trim()));
                    } catch (Exception e) {
                        mainHandler.post(() -> callback.onError("Gagal membaca respons AI"));
                    }
                }
            });

        } catch (Exception e) {
            mainHandler.post(() -> callback.onError("Terjadi kesalahan: " + e.getMessage()));
        }
    }
}