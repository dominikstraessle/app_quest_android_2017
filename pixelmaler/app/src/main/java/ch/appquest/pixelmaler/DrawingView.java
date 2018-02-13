package ch.appquest.pixelmaler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Die DrawingView ist für die Darstellung und Verwaltung der Zeichenfläche
 * zuständig.
 */
public class DrawingView extends View {

    private static final int GRID_SIZE = 13;

    private Path drawPath = new Path();
    private Paint drawPaint = new Paint();
    private Paint linePaint = new Paint();
    private Paint rectPaint = new Paint();
    private boolean isErasing = false;
    private PixelRaster pixelRaster;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(20);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        linePaint.setColor(0xFF666666);
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(1.0f);
        linePaint.setStyle(Paint.Style.STROKE);

        pixelRaster = new PixelRaster();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int maxX = canvas.getWidth();
        final int maxY = canvas.getHeight();

        final int stepSizeX = (int) Math.ceil((double) maxX / GRID_SIZE);
        final int stepSizeY = (int) Math.ceil((double) maxY / GRID_SIZE);

        pixelRaster.setPixelWidth(stepSizeX);
        pixelRaster.setPixelHeight(stepSizeY);


        for (Pixel pixel : pixelRaster.getPixelList()) {
            rectPaint.setColor(pixel.getColor());
            float left = pixel.getX() * stepSizeX;
            float top = pixel.getY() * stepSizeY;

            float right = (pixel.getX() + 1) * stepSizeX;
            float bottom = (pixel.getY() + 1) * stepSizeY;
            canvas.drawRect(left, top, right, bottom, rectPaint);
        }


        // TODO Zeichne das Gitter
        for (int i = 0; i < 13; i++) {
            canvas.drawLine(i * stepSizeX, 0, i * stepSizeX, maxY, linePaint);
        }
        for (int i = 0; i < 13; i++) {
            canvas.drawLine(0, i * stepSizeY, maxX, i * stepSizeY, linePaint);
        }
        // Zeichnet einen Pfad der dem Finger folgt
        canvas.drawPath(drawPath, drawPaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);

                // TODO wir müssen uns die berührten Punkte zwischenspeichern
                moveDraw(touchX, touchY);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);

                // TODO wir müssen uns die berührten Punkte zwischenspeichern
                moveDraw(touchX, touchY);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:

                // TODO Jetzt können wir die zwischengespeicherten Punkte auf das
                // Gitter umrechnen und zeichnen, bzw. löschen, falls wir isErasing
                // true ist (optional)
                moveDraw(touchX, touchY);
                invalidate();
                drawPath.reset();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    private void moveDraw(float touchX, float touchY) {
        pixelRaster.addPixel(touchX, touchY, drawPaint.getColor());
    }


    public void startNew() {

        // TODO Gitter löschen
        pixelRaster.setNew();
        invalidate();
    }

    public void setErase(boolean isErase) {
        isErasing = isErase;
//        if (isErasing) {
//            drawPaint.setAlpha(0xFF);
//            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//        } else {
//            drawPaint.setXfermode(null);
//        }
        pixelRaster.setErasing(isErase);
    }

    public boolean isErasing() {
        return isErasing;
    }

    public void setColor(String color) {
        invalidate();
        drawPaint.setColor(Color.parseColor(color));
    }

    // erstelle JSON
    public JSONObject createJSON() throws JSONException {
        JSONArray arrayLog = new JSONArray();
        for (int j = 0; j < 13; j++) {
            for (int i = 0; i < 13; i++) {

                Pixel pixel = pixelRaster.getPixelList().get(pixelRaster.getPixelList().indexOf(new Pixel(i, j, Color.WHITE)));
                if (pixel.getColor() != Color.WHITE) {
                    JSONObject line = new JSONObject();
                    line.put("y", pixel.getY());
                    line.put("x", pixel.getX());
                    line.put("color", String.format("#%06XFF", (0xFFFFFF & pixel.getColor())));
//                line.put("color", pixel.getColor());
                    arrayLog.put(line);
                }
            }
        }
        JSONObject json = new JSONObject();
        json.put("task", "Pixelmaler");
        json.put("pixels", arrayLog);
        return json;
    }


    //loggen
    public void logJSON(JSONObject jsonObject, MainActivity activity) {
        if (jsonObject == null) {
            Toast.makeText(activity, "Keine Logs", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent("ch.appquest.intent.LOG");

        if (activity.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) { // -> Logbuch App nicht installiert.
            Toast.makeText(activity, "Logbuch nicht installiert", Toast.LENGTH_LONG).show(); //Toast ist ein kleiner Aufgepoppert hinweis.
            return;
        }
        intent.putExtra("ch.appquest.logmessage", jsonObject.toString());

        activity.startActivity(intent);
    }
}
