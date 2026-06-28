package com.mantao.star;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

/**
 * Ring progress sederhana berbasis Canvas (drawArc), dipakai untuk menampilkan XP.
 * Tidak butuh library tambahan.
 */
public class CircularProgressView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();

    private float progressFraction = 0f; // 0..1
    private final float strokeWidthPx;

    public CircularProgressView(Context context) {
        this(context, null);
    }

    public CircularProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        strokeWidthPx = context.getResources().getDisplayMetrics().density * 12;

        // Wajib software layer biar shadowLayer (efek glow) render dengan benar di semua device
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        int trackColor = ContextCompat.getColor(context, R.color.on_surface_variant);
        int progressColor = ContextCompat.getColor(context, R.color.primary);

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokeWidthPx);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setColor(ColorUtils.setAlphaComponent(trackColor, 40));

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidthPx);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(progressColor);
        progressPaint.setShadowLayer(16f, 0f, 0f, ColorUtils.setAlphaComponent(progressColor, 110));
    }

    /** @param fraction 0..1, porsi lingkaran yang terisi */
    public void setProgress(float fraction) {
        this.progressFraction = Math.max(0f, Math.min(1f, fraction));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float padding = strokeWidthPx / 2f + 6f;
        arcBounds.set(padding, padding, getWidth() - padding, getHeight() - padding);

        canvas.drawArc(arcBounds, -90, 360, false, trackPaint);
        if (progressFraction > 0f) {
            canvas.drawArc(arcBounds, -90, 360 * progressFraction, false, progressPaint);
        }
    }
}