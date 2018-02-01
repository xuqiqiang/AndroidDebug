/*
 *
 *  *    Copyright (C) 2016 Snailstudio
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
package com.snailstudio.xsdk.debug.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;

import com.snailstudio.xsdk.debug.service.KeepAliveService;
import com.snailstudio.xsdk.debug.utils.LogUtils;

/**
 * Created by xuqiqiang on 2017/04/17.
 */
public class DebugInitProvider extends ContentProvider {
    private static final String TAG = DebugInitProvider.class.getSimpleName();

    public DebugInitProvider() {
    }

    @Override
    public boolean onCreate() {
        LogUtils.dLog(TAG, "onCreate");
        Context context = getContext();
        if (context != null) {
            context.startService(new Intent(context, KeepAliveService.class));
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public void attachInfo(Context context, ProviderInfo providerInfo) {
        if (providerInfo == null) {
            throw new NullPointerException("DebugInitProvider ProviderInfo cannot be null.");
        }
        // So if the authorities equal the library internal ones, the developer forgot to set his applicationId
        if ("com.snailstudio.xsdk.debug.provider.DebugInitProvider".equals(providerInfo.authority)) {
            throw new IllegalStateException("Incorrect provider authority in manifest. Most likely due to a "
                    + "missing applicationId variable in application\'s build.gradle.");
        }
        super.attachInfo(context, providerInfo);
    }

}
