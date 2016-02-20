package com.lyn.colorpickcamera;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by @HelloDHLyn on 1/13/16.
 */
public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.btn_start);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (checkCameraHardware(MainActivity.this)) {
                    EditText frequency = (EditText) findViewById(R.id.edit_freq);
                    EditText shotNumber = (EditText) findViewById(R.id.edit_shot_num);
                    Bundle bundle = new Bundle();
                    bundle.putInt("Frequency", Integer.parseInt(frequency.getText().toString()));
                    bundle.putInt("ShotNumber", Integer.parseInt(shotNumber.getText().toString()));

                    Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                else {
                    Toast toast = Toast.makeText(MainActivity.this,
                            "오류가 발생했습니다", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            Log.e("ColorPickCamera", "카메라가 발견되지 않았습니다");
            return false;
        }
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
