package com.lynlab.gallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;


public class MainActivity extends ActionBarActivity {

    private static int idx = 0;
    private static TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String path = Environment.getExternalStorageDirectory().toString()+"/ciplab-photo";
        File f = new File(path);
        final File imagelist[] = f.listFiles();

        status = (TextView) findViewById(R.id.status);

        Button prev = (Button) findViewById(R.id.prev);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                --idx;
                showImage(imagelist[idx%imagelist.length]);
                status.setText(idx%imagelist.length + " / " + imagelist.length);
            }
        });

        Button next = (Button) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ++idx;
                showImage(imagelist[idx%imagelist.length]);
                status.setText(idx%imagelist.length + " / " + imagelist.length);
            }
        });

    }

    private void showImage(File image) {
        Bitmap myBitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
        ImageView myImage = (ImageView) findViewById(R.id.image);
        myImage.setImageBitmap(myBitmap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
