package com.mantao.star;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * Klasifikasi objek menggunakan model pretrained MobileNetV2 (ImageNet, Apache-2.0),
 * lalu dipetakan menjadi kategori "Organik" atau "Anorganik" berdasarkan kata kunci
 * pada label hasil deteksi. Tidak memerlukan training tambahan.
 *
 * File yang wajib ada di app/src/main/assets/:
 *  - mobilenet_v2_1.0_224.tflite
 *  - ImageNetLabels.txt
 */
public class WasteClassifier {

    private static final String MODEL_FILE = "mobilenet_v2_1.0_224.tflite";
    private static final String LABEL_FILE = "ImageNetLabels.txt";
    private static final int IMAGE_SIZE = 224;

    // Kata kunci objek yang biasanya tergolong sampah ORGANIK (sisa makanan/tumbuhan).
    // Selain ini, semua dianggap ANORGANIK (botol, kertas, kaleng, elektronik, dll).
    // Tambah/kurangi daftar ini sambil testing dengan sampah nyata.
    private static final String[] ORGANIC_KEYWORDS = {
            "banana", "orange", "lemon", "fig", "pineapple", "pomegranate",
            "strawberry", "granny smith", "custard apple", "mushroom", "agaric",
            "corn", "ear", "broccoli", "cauliflower", "zucchini", "squash",
            "cucumber", "artichoke", "bell pepper", "cardoon", "cabbage",
            "mashed potato", "guacamole"
    };

    private final Interpreter interpreter;
    private final List<String> labels;

    public WasteClassifier(Context context) throws IOException {
        MappedByteBuffer modelBuffer = loadModelFile(context, MODEL_FILE);
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4);
        interpreter = new Interpreter(modelBuffer, options);
        labels = FileUtil.loadLabels(context, LABEL_FILE);
    }

    private MappedByteBuffer loadModelFile(Context context, String filename) throws IOException {
        AssetFileDescriptor fd = context.getAssets().openFd(filename);
        FileInputStream inputStream = new FileInputStream(fd.getFileDescriptor());
        FileChannel channel = inputStream.getChannel();
        return channel.map(FileChannel.MapMode.READ_ONLY, fd.getStartOffset(), fd.getDeclaredLength());
    }

    /**
     * Jalankan klasifikasi pada satu Bitmap. Aman dipanggil dari background thread
     * (jangan dari main thread, karena inference TFLite cukup berat).
     */
    public Result classify(Bitmap bitmap) {
        TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
        tensorImage.load(bitmap);

        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(IMAGE_SIZE, IMAGE_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                .add(new NormalizeOp(127.5f, 127.5f)) // normalisasi pixel ke rentang [-1, 1]
                .build();
        tensorImage = imageProcessor.process(tensorImage);

        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(
                new int[]{1, labels.size()}, DataType.FLOAT32);

        interpreter.run(tensorImage.getBuffer(), outputBuffer.getBuffer());

        float[] scores = outputBuffer.getFloatArray();
        int bestIdx = 0;
        for (int i = 1; i < scores.length; i++) {
            if (scores[i] > scores[bestIdx]) bestIdx = i;
        }

        String rawLabel = labels.get(bestIdx);
        String category = isOrganic(rawLabel) ? "Organik" : "Anorganik";

        return new Result(rawLabel, category, scores[bestIdx]);
    }

    private boolean isOrganic(String label) {
        String lower = label.toLowerCase();
        for (String keyword : ORGANIC_KEYWORDS) {
            if (lower.contains(keyword)) return true;
        }
        return false;
    }

    public void close() {
        if (interpreter != null) interpreter.close();
    }

    public static class Result {
        public final String rawLabel;   // objek asli yang terdeteksi, misal "water bottle"
        public final String category;   // "Organik" atau "Anorganik"
        public final float confidence;

        Result(String rawLabel, String category, float confidence) {
            this.rawLabel = rawLabel;
            this.category = category;
            this.confidence = confidence;
        }
    }
}