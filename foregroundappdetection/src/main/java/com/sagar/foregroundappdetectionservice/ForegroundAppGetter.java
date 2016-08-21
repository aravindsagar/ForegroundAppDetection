package com.sagar.foregroundappdetectionservice;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by aravi on 5/8/2016.
 */
public class ForegroundAppGetter {

    private ActivityManager mActivityManager;
    private UsageStatsManager mUsageStatsManager;

    public ForegroundAppGetter(Context context) {
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        }
    }

    public String getForegroundAppName() {
        String foregroundTaskPackageName = "";
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            long time = System.currentTimeMillis();
            List<UsageStats> appList = mUsageStatsManager
                    .queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*100, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (!mySortedMap.isEmpty()) {
                    foregroundTaskPackageName = mySortedMap
                            .get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            List<ActivityManager.RunningAppProcessInfo> tasks =
                    mActivityManager.getRunningAppProcesses();
            foregroundTaskPackageName = tasks.get(0).processName;
        } else {
            ActivityManager.RunningTaskInfo foregroundTaskInfo = mActivityManager.getRunningTasks(1).get(0);
            foregroundTaskPackageName = foregroundTaskInfo.topActivity.getPackageName();
        }
        return foregroundTaskPackageName;
    }
}
