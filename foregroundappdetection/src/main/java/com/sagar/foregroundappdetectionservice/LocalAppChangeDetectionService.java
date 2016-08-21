package com.sagar.foregroundappdetectionservice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.io.Serializable;

public class LocalAppChangeDetectionService extends Service {
    private static final String PACKAGE_NAME = LocalAppChangeDetectionService.class.getPackage().getName();
    private static final String LOG_TAG = LocalAppChangeDetectionService.class.getSimpleName();

    /*public static final String ACTION_CLEAR_PREVIOUS_PACKAGE = PACKAGE_NAME + ".CLEAR_PREVIOUS_PACKAGE";
    public static final String ACTION_START_SERVICE = PACKAGE_NAME + ".START_SERVICE";
    public static final String ACTION_STOP_SERVICE = PACKAGE_NAME + ".STOP_SERVICE";*/

    private String previousPackageName = "";

    private OnForegroundAppChangedListener mOnForegroundAppChangedListener;

    private AppLockThread mAppLockThread;

    private ForegroundAppGetter mForegroundAppGetter;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        public LocalAppChangeDetectionService getService() {
            return LocalAppChangeDetectionService.this;
        }
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    public LocalAppChangeDetectionService() {}

    /*public static Intent getServiceIntent(Context context, final String action){
        Intent intent = new Intent(context, LocalAppChangeDetectionService.class);
        if(action != null && !action.isEmpty()) {
            intent.setAction(action);
        }
        return intent;
    }*/

    public void registerListener(OnForegroundAppChangedListener listener) {
        mOnForegroundAppChangedListener = listener;
        startThread();
    }

    public void clearPreviousPackage() {
        previousPackageName = "";
    }

    public void unregisterListener() {
        mOnForegroundAppChangedListener = null;
    }

    /*@Override
    public void onCreate() {
        startThread();
        mForegroundAppGetter = new ForegroundAppGetter(this);
        super.onCreate();
    }*/

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /*@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            String action = intent.getAction();
            if (action != null && !action.isEmpty()) {
                if (action.equals(ACTION_CLEAR_PREVIOUS_PACKAGE)) {
                    previousPackageName = "";
                } else if (action.equals(ACTION_START_SERVICE)) {
                    Log.d(LOG_TAG, "Starting app detection");
                    startThread();
                } else if (action.equals(ACTION_STOP_SERVICE)) {
                    stopThread();
                }
            }
        }
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }*/

    private void stopThread(){
        if(mAppLockThread != null) {
            mAppLockThread.interrupt();
            mAppLockThread = null;
        }
    }

    private void startThread(){
        if (mForegroundAppGetter == null) {
            mForegroundAppGetter = new ForegroundAppGetter(this);
        }
        stopThread();
        mAppLockThread = new AppLockThread();
        mAppLockThread.start();
    }

    private class AppLockThread extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

                String foregroundTaskPackageName = mForegroundAppGetter.getForegroundAppName();

                if(foregroundTaskPackageName.equals("") ||
                        foregroundTaskPackageName.equals(previousPackageName)){
                    continue;
                }

                previousPackageName = foregroundTaskPackageName;

                if(mOnForegroundAppChangedListener != null){
                    mOnForegroundAppChangedListener.onForegroundAppChanged(
                            foregroundTaskPackageName);
                }
            }
        }
    }

    public abstract static class OnForegroundAppChangedListener implements Serializable {
        public abstract void onForegroundAppChanged(String packageName);

    }

}
