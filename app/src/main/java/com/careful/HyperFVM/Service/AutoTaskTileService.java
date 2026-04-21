package com.careful.HyperFVM.Service;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.widget.Toast;

import com.careful.HyperFVM.utils.ForDashboard.NotificationManager.PersistentServiceNotificationManager;

public class AutoTaskTileService extends TileService {
    @Override
    public void onClick() {
        super.onClick();
        PersistentServiceNotificationManager persistentManager = new PersistentServiceNotificationManager(getApplicationContext());
        persistentManager.sendForegroundNotification();

        Log.d("Tile", "SUCCESS!");

        showToast("SUCCESS!");
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

    private void showToast(String message) {
        getMainExecutor().execute(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }
}
