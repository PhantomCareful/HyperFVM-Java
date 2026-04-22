package com.careful.HyperFVM.Service;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.careful.HyperFVM.utils.ForDashboard.NotificationManager.TileTaskHelper;
import com.careful.HyperFVM.utils.ForDashboard.NotificationManager.TileTaskNotificationManager;

public class AutoTaskTileService extends TileService {
    @Override
    public void onClick() {
        super.onClick();

        TileTaskNotificationManager tileTaskNotificationManager = new TileTaskNotificationManager(getApplicationContext());
        TileTaskHelper.executeTileTasks(getApplicationContext(), tileTaskNotificationManager);

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
