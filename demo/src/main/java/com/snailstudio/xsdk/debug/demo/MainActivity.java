package com.snailstudio.xsdk.debug.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.snailstudio.xsdk.debug.demo.database.CarDBHelper;
import com.snailstudio.xsdk.debug.demo.database.ContactDBHelper;
import com.snailstudio.xsdk.debug.demo.database.ExtTestDBHelper;
import com.snailstudio.xsdk.debug.demo.utils.Utils;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Set<String> stringSet = new HashSet<>();
        stringSet.add("SetOne");
        stringSet.add("SetTwo");
        stringSet.add("SetThree");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences prefsOne = getSharedPreferences("countPrefOne", Context.MODE_PRIVATE);
        SharedPreferences prefsTwo = getSharedPreferences("countPrefTwo", Context.MODE_PRIVATE);

        sharedPreferences.edit().putString("testOne", "one").commit();
        sharedPreferences.edit().putInt("testTwo", 2).commit();
        sharedPreferences.edit().putLong("testThree", 100000L).commit();
        sharedPreferences.edit().putFloat("testFour", 3.01F).commit();
        sharedPreferences.edit().putBoolean("testFive", true).commit();
        sharedPreferences.edit().putStringSet("testSix", stringSet).commit();

        prefsOne.edit().putString("testOneNew", "one").commit();

        prefsTwo.edit().putString("testTwoNew", "two").commit();

        ContactDBHelper contactDBHelper = new ContactDBHelper(getApplicationContext());
        if (contactDBHelper.count() == 0) {
            for (int i = 0; i < 100; i++) {
                String name = "name_" + i;
                String phone = "phone_" + i;
                String email = "email_" + i;
                String street = "street_" + i;
                String place = "place_" + i;
                contactDBHelper.insertContact(name, phone, email, street, null);
            }
        }

        CarDBHelper carDBHelper = new CarDBHelper(getApplicationContext());
        if (carDBHelper.count() == 0) {
            for (int i = 0; i < 50; i++) {
                String name = "name_" + i;
                String color = "RED";
                float mileage = i + 10.45f;
                carDBHelper.insertCar(name, color, mileage);
            }
        }

        ExtTestDBHelper extTestDBHelper = new ExtTestDBHelper(getApplicationContext());
        if (extTestDBHelper.count() == 0) {
            for (int i = 0; i < 20; i++) {
                String value = "value_" + i;
                extTestDBHelper.insertTest(value);
            }
        }

        Utils.setCustomDatabaseFiles(getApplicationContext());

    }

    public void showDebugDbAddress(View view) {
        Utils.showDebugDBAddressLogToast(getApplicationContext());
    }

}
