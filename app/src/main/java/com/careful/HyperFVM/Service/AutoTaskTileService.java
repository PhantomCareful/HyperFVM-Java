package com.careful.HyperFVM.Service;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.careful.HyperFVM.utils.ForDashboard.NotificationManager.AutoTaskNotificationManager;

public class AutoTaskTileService extends TileService {
    @Override
    public void onClick() {
        super.onClick();

        executeAutoTask();
    }

    public void executeAutoTask() {
        AutoTaskNotificationManager autoTaskNotificationManager = new AutoTaskNotificationManager(getApplicationContext());
        autoTaskNotificationManager.createNotificationChannel();
        autoTaskNotificationManager.sendAutoTaskNotification(0, "执行中⏳");
        autoTaskNotificationManager.sendAutoTaskNotification(50, "执行中⏳");
        autoTaskNotificationManager.sendAutoTaskNotification(100, "执行完成🎉");
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setState(Tile.STATE_ACTIVE); // 或 STATE_ACTIVE，表示可点击
            tile.updateTile();
        }
    }

}
