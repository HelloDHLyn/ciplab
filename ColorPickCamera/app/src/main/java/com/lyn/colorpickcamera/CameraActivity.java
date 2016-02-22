package com.lyn.colorpickcamera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

/**
 * Created by @HelloDHLyn on 1/14/16.
 */
public class CameraActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private static View colorPreview;
    private static FrameLayout preview;
    private static SurfaceView overlayView;
    private static SurfaceHolder holderTransparent;

    private static Boolean started = false;
    private static Boolean save = false;
    private static String fileName = "RGBImage";
    private Handler handler = new Handler();
    private static int interval = -1;
    private static long start_time = -1;
    private static int shots_number = -1;
    private static int taken_number = 0;

    private static TextView progressText;

    private String path;
    private File filePath;
    private String csvFile;
    private FileWriter csvWriter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Get parameter values
        Intent myIntent = getIntent();
        interval = myIntent.getIntExtra("Frequency", 3);
        shots_number = myIntent.getIntExtra("ShotNumber", 0);
        if (myIntent.getIntExtra("Save", 0) == 1) save = true;
        else save = false;
        shots_number = myIntent.getIntExtra("ShotNumber", 0);
        fileName = myIntent.getStringExtra("Filename");

        progressText = (TextView) findViewById(R.id.text_progress);
        overlayView = (SurfaceView)findViewById(R.id.overlay_preview);

        // Set file IO
        path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ciplab";

        filePath = new File(path);
        if (filePath.exists() == false) {
            if (filePath.mkdir() == false) {
                Log.e("ColorPickCamera", "Error making directory");
                return;
            }
        }

        csvFile = path + "/" + fileName + ".csv";
        try {
            csvWriter= new FileWriter(csvFile);
            csvWriter.append("timestamp,R,G,B");
            csvWriter.append('\n');
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set color preview
        colorPreview = (RelativeLayout) findViewById(R.id.color_preview);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        // Draw preview rectangle
        Box box = new Box(this, (LinearLayout) findViewById(R.id.activity_container), (SurfaceView) findViewById(R.id.overlay_preview));
        addContentView(box, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        Button button_focus = (Button) findViewById(R.id.btn_focus);
        button_focus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mCamera.autoFocus(autoFocusCallback);
            }
        });

        Button button_shot = (Button) findViewById(R.id.btn_shot);
        button_shot.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                taken_number = 0;
                start_time = System.currentTimeMillis();
                start();
            }
        });
    }

    @Override
    public void onDestroy() {
        mCamera.release();
        try {
            csvWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(started) handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mCamera.takePicture(null, null, pictureCallback);

            if (started) {
                start();
            }
        }
    };

    public void stop() {
        started = false;
        handler.removeCallbacks(runnable);

        Toast toast = Toast.makeText(CameraActivity.this, "완료", Toast.LENGTH_SHORT);
        toast.show();

        finish();
    }

    public void start() {
        started = true;
        long delay = 0;
        if (taken_number > 0) {
            delay = start_time + 1000 * interval * (taken_number + 2) - System.currentTimeMillis();
        } else {
            delay = 1000 * interval;
        }

        Log.i("ColorPickCamera", Long.toString(delay));

        if (delay > 0) {
            handler.postDelayed(runnable, delay);
        } else {
            handler.post(runnable);
        }
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.e("ColorPickCamera", "카메라를 여는 중 오류가 발생했습니다: " + e.getMessage());
        }
        return c; // returns null if camera is unavailable
    }

    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {

        }
    };

    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            final byte[] imagedata = data;
            Thread thread = new Thread() {
                public void run() {
                    Calendar calendar = Calendar.getInstance();
                    String time = String.format("%04d%02d%02d-%02d:%02d:%02d",
                            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
                    String fileName = time + ".jpg";

                    if (save) {
                        File pictureFile = new File(path + "/" + fileName);
                        if (pictureFile == null) {
                            Log.e("ColorPickCamera", "Error creating media file, check storage permissions");
                            return;
                        }

                        try {
                            FileOutputStream fos = new FileOutputStream(pictureFile);
                            fos.write(imagedata);
                            fos.flush();
                            fos.close();
                        } catch (FileNotFoundException e) {
                            Log.e("ColorPickCamera", "File not found: " + e.getMessage());
                        } catch (IOException e) {
                            Log.e("ColorPickCamera", "Error accessing file: " + e.getMessage());
                        }
                    }

                    Bitmap bitmap = BitmapFactory.decodeByteArray(imagedata, 0, imagedata.length);

                    int redColors = 0;
                    int greenColors = 0;
                    int blueColors = 0;
                    int pixelCount = 0;
                    int size = 250;

                    int width = 5312;
                    int height = 2988;

                    for (int y = height / 2 - size; y < height / 2 + size; y++) {
                        for (int x = width / 2 - size; x < width / 2 + size; x++) {
                            int c = bitmap.getPixel(x, y);
                            pixelCount++;
                            redColors += Color.red(c);
                            greenColors += Color.green(c);
                            blueColors += Color.blue(c);
                        }
                    }

                    // calculate average of bitmap r,g,b values
                    final float red = (float)redColors / (float)pixelCount;
                    final float green = (float)greenColors / (float)pixelCount;
                    final float blue = (float)blueColors / (float)pixelCount;

                    try {
                        csvWriter.append(time + "," + red + "," + green + "," + blue);
                        csvWriter.append('\n');

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                colorPreview.setBackgroundColor(Color.argb(255, (int) red, (int) green, (int) blue));
                                Log.i("ColorPickCamera", red + ", " + green + ", " + blue);
                                colorPreview.invalidate();
                            }
                        });

                        if (taken_number % 200 == 0) {
                            csvWriter.flush();
                        }
                    } catch (FileNotFoundException e) {
                        Log.e("ColorPickCamera", "File not found: " + e.getMessage());
                    } catch (IOException e) {
                        Log.e("ColorPickCamera", "Error accessing file: " + e.getMessage());
                    }
                }
            };

            thread.start();

            taken_number++;
            progressText.setText(taken_number + " / " + shots_number + "\n");

            if (taken_number == shots_number) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    csvWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                stop();
            }
            else {
                mCamera.startPreview();
            }
        }
    };

}

class Box extends View {
    private Paint paint = new Paint();
    private LinearLayout container;
    private SurfaceView overlay;

    Box(Context context, LinearLayout container, SurfaceView overlay) {
        super(context);
        this.container = container;
        this.overlay = overlay;
    }

    @Override
    protected void onDraw(Canvas canvas) { // Override the onDraw() Method
        super.onDraw(canvas);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(3);

        //center
        int x0 = container.getWidth() - (int) (overlay.getX() + overlay.getWidth() / 2);
        int y0 = container.getHeight() - (int) (overlay.getY() + overlay.getHeight() / 2);
        int dx = 50;
        int dy = 50;

        Log.i("ColorPickCamera", x0 + ", " + y0);

        //draw guide box
        canvas.drawRect(x0-dx, y0-dy, x0+dx, y0+dy, paint);
    }
}