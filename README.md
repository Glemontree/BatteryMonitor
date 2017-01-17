# BatteryMonitor
## 监听电池电量

### 监听电池变化的方法

Battert Manager会通过一个Intent广播所有电池和充电详情，包含充电状态。Android开发者可以利用BroadcastReceiver机制获取电池电量的变化，通过监听电池电量的变化包含以下几个步骤：

- 创建一个监听ACTION_BATTERY_CHAGRED事件的 intentFilter
- 创建一个BroadcastReceiver对象，该对象可以接收broadcase intent
- 注册BroadcastReceiver对象来监听ACTION_BATTERY_CHANGED事件
- 在BroadcastReceiver对象中，重写onReceive方法，在onReceive方法的传入参数intent中获取电池的状态
- 切记的是在生命周期结束时，及时取消BroadcastReceiver的注册，否则出现内存泄漏

### 具体实现

通常有两种思路，第一种就是自定义一个BroadcastReceiver继承自BroadcastReceiver，并且重写onReceive()方法，在onReceive()方法中根据方法传入的Intent来判断电池的状态，这种方法需要主要的是需要在Manifest文件中注册广播接收器，添加过滤条件；还有一种方法是直接在Activity中定义一个BroadcastReceiver对象并重写onReceive()方法，并且需要定义IntentFilter，同时需要调用registerReceiver方法来将BroadcastReceiver和IntentFilter进行绑定。

这里就第二种方法给出一个例子，代码如下：

```java
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
```

### 代码分析

上面这段代码中，首先在onCreate()方法中定义了一个BroadcastReceiver，并且重写了其onReceive方法，在onReceive()方法中根据传入的Intent，获得电池的一些信息：

```java
int rawLevel = intent.getIntExtra("level", -1);
int scale = intent.getIntExtra("scale", -1);
int status = intent.getIntExtra("status", -1);
int health = intent.getIntExtra("health", -1);
```

其中，level表示电池的当前剩余电量，scale表示电池的最大值，status表示电池的状态，health则表示电池的健康状态。

在方法的最后定义了IntentFilter用来定义消息过滤，这里过滤的是```Intent.ACTION_BATTERY_CHANGED```，表示BroadcastReceiver监听电池电量的变化，最后通过registerReceiver方法将BroadcastReceiver和IntentFilter进行绑定。

需要注意的是一定在Activity的onDestroy()方法中调用unRegisterReceiver()方法来取消注册Receiver，当然本例子是在onCreate()方法中调用registerReceiver()来绑定BroadcastReceiver和IntentFilter，你也可以在onResume()方法中进行绑定，甚至于在View中在onAttatchedWindow()方法中进行绑定！

### 总结

本文主要对Android中电池电量的监听进行了简短的介绍，更加详细的介绍可以上网查询，这里仅仅了做个笔记，以便后面查询回忆！

### 说明

本文转自[博客](http://blog.csdn.net/sheldon4090/article/details/8109605)



