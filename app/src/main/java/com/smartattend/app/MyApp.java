package com.smartattend.app;

import android.app.Application;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dbersolup");
        config.put("api_key", "629764496681777");

        MediaManager.init(this, config);
    }
}
