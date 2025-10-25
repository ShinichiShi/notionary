package com.collab.productivity;

import android.app.Application;
import com.collab.productivity.utils.Logger;
import com.collab.productivity.data.database.DatabaseProvider;
import com.collab.productivity.data.database.AppDatabase;

public class NotionaryApp extends Application {
    private AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.init(getFilesDir());
        Logger.i("App", "Application started");

        // Initialize database
        database = DatabaseProvider.getDatabase(this);
        Logger.i("App", "Database initialized");
    }

    public AppDatabase getDatabase() {
        return database;
    }
}
