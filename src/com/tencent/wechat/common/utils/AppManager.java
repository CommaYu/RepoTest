package com.tencent.wechat.common.utils;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.util.Log;
import android.widget.PopupWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by Administrator on 2016/7/4.
 */
public final class AppManager {

    private static final String LOG_TAG = AppManager.class.getSimpleName();

    private static Stack<Activity> activityStack = new Stack<Activity>();

    private static Stack<Service> serviceStack;

    private static Stack<PopupWindow> popupWindowStack;

    private static AppManager mInstance;

    private static Application mApplication;

    private AppManager() {
    }

    /**
     * 获取唯一实例
     *
     * @return
     * @author xhyin
     * @date 2014-5-6
     * @since 1.0
     */
    public synchronized static AppManager getInstance() {
        if (mInstance == null) {
            mInstance = new AppManager();
        }
        return mInstance;
    }

    /**
     * 绑定application实例
     *
     * @param application 绑定指定的全局应用实例
     * @author xhyin
     * @date 2014-5-6
     * @since 1.0
     */
    public void bindApplication(Application application) {
        if (application == null) {
            throw new NullPointerException("Application cannot be null");
        }

        if (mApplication == null) {
            mApplication = application;
        }
    }

    /**
     * 获取绑定的application实例
     *
     * @return 获取指定的全局应用实例
     * @author xhyin
     * @date 2014-5-6
     * @since 1.0
     */
    public synchronized Application getApplication() {
        if (mApplication == null) {
            throw new NullPointerException("Application cannot be null");
        }
        return mApplication;
    }

    /**
     * Activity进栈
     *
     * @param activity 指定的activity
     * @author xhyin
     * @date 2014-5-6
     * @since 1.0
     */
    public synchronized void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        activityStack.add(activity);
    }

    /**
     * 将指定service推到Service栈中
     *
     * @param service 指定的service
     * @author xhyin
     * @date 2013-8-27
     * @since 1.0
     */
    public synchronized void addService(Service service) {
        if (serviceStack == null) {
            serviceStack = new Stack<Service>();
        }
        serviceStack.add(service);
    }

    /**
     * 将指定的popupwindow推入PopupWindow栈中
     *
     * @param popupWindow
     * @author xhyin
     * @date 2014-9-24
     * @since 1.0
     */
    public synchronized void addPopupWindow(PopupWindow popupWindow) {
        if (popupWindowStack == null) {
            popupWindowStack = new Stack<PopupWindow>();
        }
        popupWindowStack.add(popupWindow);
    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     *
     * @return 当前Activity
     * @author xhyin
     * @date 2013-8-27
     * @since 1.0
     */
    public Activity currentActivity() {
        if (activityStack.isEmpty()) {
            return null;
        }
        return activityStack.lastElement();
    }

    /**
     * 结束当前Activity（堆栈中最后一个压入的）
     *
     * @author xhyin
     * @date 2013-8-27
     * @since 1.0
     */
    public void finishActivity() {
        Activity activity = activityStack.lastElement();
        finishActivity(activity);
    }

    /**
     * 销毁指定的Activity
     *
     * @param activity 指定的activity
     * @author xhyin
     * @date 2013-8-27
     * @since 1.0
     */
    public void finishActivity(Activity activity) {
        if (activity != null) {
            activityStack.remove(activity);
            activity.finish();
            activity = null;
        }
    }

    /**
     * 销毁指定的类别的Acivtiy，可能一个activity会在应用中存在多个实例
     *
     * @param cls 指定类型的Activity
     * @author xhyin
     * @date 2013-8-27
     * @since 1.0
     */
    public void finishActivity(Class<?> cls) {
        // 由于list使用iterator遍历的时候不能remove掉
        List<Activity> removeList = new ArrayList<Activity>();
        for (Activity activity : activityStack) {
            if (activity.getClass().equals(cls)) {
                removeList.add(activity);
                // finishActivity(activity);
            }
        }
        activityStack.removeAll(removeList);
        for (Activity activity : removeList) {
            if (activity != null) {
                activity.finish();
                activity = null;
            }
        }
    }

    /**
     * 销毁除去此类型之外的Activity对象
     *
     * @param cls 不删除指定类型的Activity实例
     * @author xhyin
     * @date 2013-10-12
     * @since 1.0
     */
    public void finishWithOut(Class<?> cls) {
        // 由于list使用iterator遍历的时候不能remove掉
        List<Activity> removeList = new ArrayList<Activity>();
        for (Activity activity : activityStack) {
            if (!activity.getClass().equals(cls)) {
                removeList.add(activity);
            }
        }
        activityStack.removeAll(removeList);
        for (Activity activity : removeList) {
            if (activity != null) {
                activity.finish();
                activity = null;
            }
        }
    }

    /**
     * 获取指定类型的Activity，仅适用于栈中只有一个该类型Activity实例
     *
     * @param cls 获取指定类型的Activity
     * @return Activity
     */
    public Activity findActivity(Class<?> cls) {
        for (Activity activity : activityStack) {
            if (activity.getClass().equals(cls)) {
                return activity;
            }
        }
        return null;
    }

    /**
     * 销毁除去此对象之外的Activity对象
     *
     * @param withOutActivity 不删除的类型
     * @author xhyin
     * @date 2013-10-12
     * @since 1.0
     */
    public void finishWithOut(Activity withOutActivity) {
        // 由于list使用iterator遍历的时候不能remove掉
        List<Activity> removeList = new ArrayList<Activity>();
        for (Activity activity : activityStack) {
            if (activity != withOutActivity) {
                removeList.add(activity);
            }
        }
        activityStack.removeAll(removeList);
        for (Activity activity : removeList) {
            if (activity != null) {
                activity.finish();
                activity = null;
            }
        }
    }

    /**
     * 销毁当前应用中所有的activity
     *
     * @author xhyin
     * @date 2013-8-27
     * @since 1.0
     */
    public void finishAllActivity() {
        if (activityStack == null) {
            return;
        }
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (null != activityStack.get(i)) {
                activityStack.get(i).finish();
            }
        }
        activityStack.clear();
    }

    /**
     * 销毁当前应用中的Service
     *
     * @author xhyin
     * @date 2013-8-27
     * @since 1.0
     */
    public void finishAllService() {
        if (serviceStack == null)
            return;
        for (int i = 0, size = serviceStack.size(); i < size; i++) {
            if (null != serviceStack.get(i)) {
                serviceStack.get(i).stopSelf();
            }
        }
        serviceStack.clear();
    }

    /**
     * 清除所有通知消息
     *
     * @param context 指定的context
     * @author xhyin
     * @date 2013-8-27
     * @since 1.0
     */
    public void finishAllNotification(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }

    /**
     * 销毁指定的PopupWindow
     *
     * @param popupWindow 指定的popupWindow
     * @author xhyin
     * @date 2013-8-27
     * @since 1.0
     */
    public void finishPopupWindow(PopupWindow popupWindow) {
        if (popupWindow != null) {
            popupWindowStack.remove(popupWindow);
            popupWindow.dismiss();
            popupWindow = null;
        }
    }

    /**
     * 清除所有的popupWindow
     *
     * @author xhyin
     * @date 2014-9-24
     * @since 1.0
     */
    public void finishAllPopupWindow() {
        if (popupWindowStack == null)
            return;
        for (int i = 0, size = popupWindowStack.size(); i < size; i++) {
            if (null != popupWindowStack.get(i)) {
                popupWindowStack.get(i).dismiss();
            }
        }
        popupWindowStack.clear();
    }

    /**
     * 销毁所有popupwindow
     *
     * @author xhyin
     * @date 2014-10-21
     * @since 1.0
     */
    public void dismissAllPopupWindow() {
        if (popupWindowStack == null)
            return;
        int i = 0;
        for (int size = popupWindowStack.size(); i < size; i++)
            if (popupWindowStack.get(i) != null) {
                ((PopupWindow) popupWindowStack.get(i)).dismiss();
            }
    }

    /**
     * 退出程序
     *
     * @param context 指定的context
     * @author xhyin
     * @date 2013-8-27
     * @since 1.0
     */
    public void AppExit(Context context) {
        try {
            finishAllNotification(context);
            finishAllActivity();
            finishAllService();
            finishAllPopupWindow();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        } catch (Exception e) {
            Log.e(LOG_TAG, "App Exit Exception", e);
        }
    }
}