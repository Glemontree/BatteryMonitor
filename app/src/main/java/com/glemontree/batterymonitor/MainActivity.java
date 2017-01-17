package com.glemontree.batterymonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView batteryLevel;
    private BroadcastReceiver batteryLevelReceiver;
    private IntentFilter batteryLevelFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        batteryLevel = (TextView) findViewById(R.id.batteryLevel);
        monitorBatteryState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryLevelReceiver);
    }

    private void monitorBatteryState() {
        batteryLevelReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                StringBuilder sb = new StringBuilder();
                int rawLevel = intent.getIntExtra("level", -1);
                int scale = intent.getIntExtra("scale", -1);
                int status = intent.getIntExtra("status", -1);
                int health = intent.getIntExtra("health", -1);
                int level = -1;
                if (rawLevel >= 0 && scale > 0) {
                    level = (rawLevel * 100) / scale;
                }
                sb.append("The phone ");
                if (BatteryManager.BATTERY_HEALTH_OVERHEAT == health) {
                    sb.append("s battery feels very hot!");
                } else {
                    switch (status) {
                        case BatteryManager.BATTERY_STATUS_UNKNOWN:
                            sb.append("no battery.");
                            break;
                        case BatteryManager.BATTERY_STATUS_CHARGING:
                            sb.append("s battery");
                            if (level <= 33) {
                                sb.append("is charging, battery level is low" + "[" + level + "]");
                            } else if (level <= 84) {
                                sb.append("is charging." + "[" + level + "]");
                            } else {
                                sb.append("will be fully charged.");
                            }
                            break;
                        case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                            if (level == 0) {
                                sb.append("needs charging right away.");
                            } else if (level > 0 && level <= 33) {
                                sb.append("is about ready to be recharged, battery level is low"
                                    + "[" + level + "]");
                            } else {
                                sb.append("s battery level is" + "[" + level + "]");
                            }
                            break;
                        case BatteryManager.BATTERY_STATUS_FULL:
                            sb.append("is fully charged.");
                            break;
                        default:
                            sb.append("s battery is indescribable!");
                            break;
                    }
                }
                sb.append("");
                batteryLevel.setText(sb.toString());
            }
        };
        batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelReceiver, batteryLevelFilter);
    }
}
