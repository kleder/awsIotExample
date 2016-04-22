package co.kleder.homesecurity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicBoolean;

import co.kleder.homesecurity.shadow.AlarmStatus;
import co.kleder.homesecurity.shadow.GetShadowTask;
import co.kleder.homesecurity.shadow.IotStatusAware;
import co.kleder.homesecurity.shadow.UpdateShadowTask;

public class MainActivity extends AppCompatActivity implements IotStatusAware {

    private static int requestedLock = 2;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private AlarmStatus registredState;
    private long mAlarmTimestamp = 0;
    private Object alarmDone = new Object();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        setContentView(R.layout.main_layout);
        final SharedPreferences prefs = this.getSharedPreferences("co.kleder.homesecurity_preferences", Context.MODE_PRIVATE);
        new Thread(new Runnable() {
            public void run() {
                while (running.get()) {
                    getShadows();
                    String text = prefs.getString("sync_frequency", "-1");
                    Long i = Long.parseLong(text);
                    if (i > 0) {
                        SystemClock.sleep(i);
                    } else {
                        running.set(false);
                    }
                }
            }
        }).start();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
    }


    private void getShadows() {
        SharedPreferences prefs = this.getSharedPreferences("co.kleder.homesecurity_preferences", Context.MODE_PRIVATE);
        String deviceId = prefs.getString("device_id", "");
        GetShadowTask getStatusShadowTask = new GetShadowTask(deviceId, getApplicationContext(), this);
        getStatusShadowTask.execute();
    }

    public void openSetting(View view) {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    public void lock(View view) {
        SharedPreferences prefs = this.getSharedPreferences("co.kleder.homesecurity_preferences", Context.MODE_PRIVATE);
        String deviceId = prefs.getString("device_id", "");
        Button lockButton = (Button) findViewById(R.id.lock_button);
        lockButton.setEnabled(false);
        UpdateShadowTask updateShadowTask = new UpdateShadowTask(deviceId, getApplicationContext());
        String newState;
        if (registredState == null) {
            newState = String.format("{\"state\":{\"desired\":{\"lock\":%d}}}", 1);
            requestedLock = 1;
        } else {
            newState = String.format("{\"state\":{\"desired\":{\"lock\":%d}}}", (registredState.state.reported.lock - 1) * -1);
            requestedLock = (registredState.state.reported.lock - 1) * -1;
        }
        Log.i("APP", newState);
        updateShadowTask.setState(newState);
        updateShadowTask.execute();

    }

    public void resetAlarm(View view) {
        if (registredState.state.reported.alarm == 0) return;
        SharedPreferences prefs = this.getSharedPreferences("co.kleder.homesecurity_preferences", Context.MODE_PRIVATE);
        String deviceId = prefs.getString("device_id", "");
        Button lockButton = (Button) findViewById(R.id.reset_button);
        lockButton.setEnabled(false);
        UpdateShadowTask updateShadowTask = new UpdateShadowTask(deviceId, getApplicationContext());

        String newState = String.format("{\"state\":{\"desired\":{\"alarm_reset\":%d}}}", 1);
        Log.i("APP", newState);
        updateShadowTask.setState(newState);
        updateShadowTask.execute();
    }

    private void doAlarm(AlarmStatus status) {
        synchronized(alarmDone) {
            if (status.state.reported.alarm == 0) return;
            Long current = System.currentTimeMillis() / 1000;
            if (current < mAlarmTimestamp) return;
            mAlarmTimestamp = System.currentTimeMillis() / 1000 + 60;
            Intent intent = new Intent(this, AlarmActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void setLastStatus(AlarmStatus lastStatus) {
        this.registredState = lastStatus;
        if (requestedLock == registredState.state.reported.lock || requestedLock == 2) {
            Button lockButton = (Button) findViewById(R.id.lock_button);
            lockButton.setEnabled(true);
        }
        TextView windowStatus = (TextView) findViewById(R.id.title_window_status_xyzasd);
        TextView alarmStatus = (TextView) findViewById(R.id.title_alarm_status);
        TextView lockStatus = (TextView) findViewById(R.id.title_lock_status);

        if (registredState.state.reported.window == 0) {
            windowStatus.setText("Open");
            Log.i("APP", "OPEN");
        } else {
            windowStatus.setText("Closed");
        }
        if (registredState.state.reported.alarm == 0) {
            alarmStatus.setText("OFF");
            Button lockButton = (Button) findViewById(R.id.reset_button);
            lockButton.setEnabled(false);
        } else {
            Log.i("APP", "ALARM");
            alarmStatus.setText("ON");
            if (registredState.state.reported.alarm_reset == 0) {
                Button lockButton = (Button) findViewById(R.id.reset_button);
                lockButton.setEnabled(true);
            }
        }

        if (registredState.state.reported.lock == 0) {
            lockStatus.setText("Disarmed");
        } else {
            Log.i("APP", "LOCK");
            lockStatus.setText("Armed");
        }

        doAlarm(registredState);

        if (registredState.state.reported.alarm_reset == 2) {
            SharedPreferences prefs = this.getSharedPreferences("co.kleder.homesecurity_preferences", Context.MODE_PRIVATE);
            String deviceId = prefs.getString("device_id", "");
            UpdateShadowTask updateShadowTask = new UpdateShadowTask(deviceId, getApplicationContext());
            String newState = String.format("{\"state\":{\"desired\":{\"alarm_reset\":%d}}}", 0);
            Log.i("APP", newState);
            updateShadowTask.setState(newState);
            updateShadowTask.execute();
            registredState.state.reported.alarm_reset = 0;
        }
    }
}
