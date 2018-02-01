/*
 *
 *  *    Copyright (C) 2016 Amit Shekhar
 *  *    Copyright (C) 2011 Android Open Source Project
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.snailstudio.xsdk.debug;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.snailstudio.xsdk.debug.serv.req.database.NetworkUtils;
import com.snailstudio.xsdk.debug.service.KeepAliveService;
import com.snailstudio.xsdk.debug.utils.LogUtils;

import java.io.File;
import java.util.HashMap;

/**
 * Created by xuqiqiang on 2017/04/17.
 */
public class DebugConfig {

    private static final String TAG = DebugConfig.class.getSimpleName();

    private DebugConfig() {
        // This class in not publicly instantiable
    }

    public static String getAddressLog(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return NetworkUtils.getAddressLog(context,
                sharedPreferences.getInt(Constants.SHARED_PREF_KEY_PORT, Constants.Config.PORT));
    }

    public static void setCustomDatabaseFiles(Context context, HashMap<String, File> customDatabaseFiles) {
        LogUtils.dLog(TAG, "setCustomDatabaseFiles");
        if (customDatabaseFiles == null)
            customDatabaseFiles = new HashMap<>();
        Intent intent = new Intent(context, KeepAliveService.class);
        intent.putExtra(KeepAliveService.KEY_CUSTOM_DATABASE, customDatabaseFiles);
        context.startService(intent);

    }

}
