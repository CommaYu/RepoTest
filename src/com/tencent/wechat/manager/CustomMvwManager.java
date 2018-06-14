package com.tencent.wechat.manager;

import android.content.Context;
import android.util.Log;

import com.iflytek.sdk.interfaces.ISvwUiListener;
import com.iflytek.sdk.manager.FlySvwManager;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.manager.MvwManager.MvwManagerListener;

/**
 * Author: congqin <br>
 * Data:<br>
 * Description: 自定义唤醒功能（通过助理实现）<br>
 * Note:在会话界面，支持语音“发微信 发位置” 等效于点击按钮<br>
 */
public class CustomMvwManager {

    private static final String TAG = "CustomMvwManager";

    private Context mContext;

    private static CustomMvwManager mInstance;

    private FlySvwManager mvwSession;

    /* 会话界面的唤醒词 发消息 */
    private static final String MVW_MESSAGE_ID_0 = "发消息";

    /* 会话界面的唤醒词 发位置 */
    private static final String MVW_LACATION_ID_1 = "发位置";
    /* 会话界面的唤醒词 导航 */
    private static final String MVW_NAVIGATE_ID_2 = "导航";
    /* 会话界面的唤醒词 返回 */
    private static final String MVW_BACK_ID_3 = "返回";

    /* 录音界面的唤醒词 发送发送 */
    private static final String MVW_SEND_ID_0 = "发送发送";
    /* 录音界面的唤醒词 取消取消 */
    private static final String MVW_CANCEL_ID_1 = "取消取消";

    /** 会话界面的唤醒 **/
    private CustomMvwManagerListener mListener;

    /** 录音界面的唤醒 **/
    private MvwManagerListener mRecordListener;

    private String keywords = "";

    private final String SCENCEKEY_SESSION = "wechat-session";

    private final String SCENCEKEY_RECORD = "wechat-record";

    private CustomMvwManager(Context c) {
        mContext = c;
        mvwSession = FlySvwManager.getInstance();
        keywords = MVW_MESSAGE_ID_0 + "," + MVW_LACATION_ID_1 + ","
                + MVW_NAVIGATE_ID_2 + "," + MVW_BACK_ID_3;
    }

    public static CustomMvwManager getInstance(Context c) {
        if (mInstance == null) {
            mInstance = new CustomMvwManager(c);
        }
        return mInstance;
    }

    public static CustomMvwManager getInstance() {
        if (mInstance == null) {
            mInstance = new CustomMvwManager(WeChatApplication.getContext());
        }
        return mInstance;
    }

    private ISvwUiListener myListener = new ISvwUiListener() {

        @Override
        public void onError(int errorid) {

        }

        @Override
        public void onCusWakeup(String scene, int nMvwId, String result,
                int score) {

            if (SCENCEKEY_SESSION.equals(scene)) {
                if (nMvwId == 0 && mListener != null) {
                    Log.d(TAG, "onWakeupResult: 发消息");
                    mListener.onMessage();
                } else if (nMvwId == 1 && mListener != null) {
                    Log.d(TAG, "onWakeupResult: 发位置");
                    mListener.onLocation();
                } else if (nMvwId == 2 && mListener != null) {
                    Log.d(TAG, "onWakeupResult: 导航");
                    mListener.onNavigate();
                } else if (nMvwId == 3 && mListener != null) {
                    Log.d(TAG, "onWakeupResult: 返回");
                    mListener.onBack();
                }
            } else if (SCENCEKEY_RECORD.equals(scene)) {
                if (MVW_SEND_ID_0.equals(result) && null != mRecordListener) {
                    Log.d(TAG, "onWakeupResult: 发送发送");
                    mRecordListener.onConfirm();
                } else if (MVW_CANCEL_ID_1.equals(result)
                        && null != mRecordListener) {
                    Log.d(TAG, "onWakeupResult: 取消取消");
                    mRecordListener.oncancle();
                }
            } else {
                Log.e(TAG, "onCusWakeup() scen is not equals SCENCEKEY");
            }
        }
    };

    // private ICustomMvwCallback callback = new ICustomMvwCallback() {
    //
    // public void initCallback(boolean state, int errId) {
    // if (state) {
    // Log.d(TAG, "服务初始化完成");
    // } else {
    // Log.d(TAG, "服务初始化失败");
    // }
    // }
    //
    // public void onWakeupResult(int nMvwId, int nMvwScore) {
    // Log.d(TAG, "onWakeupResult: nMvwId=" + nMvwId);
    // if (nMvwId == 0 && mListener != null) {
    // Log.d(TAG, "onWakeupResult: 发消息");
    // mListener.onMessage();
    // } else if (nMvwId == 1 && mListener != null) {
    // Log.d(TAG, "onWakeupResult: 发位置");
    // mListener.onLocation();
    // }
    // }
    //
    // public void initMvwCallback(boolean state, int errId) {
    // if (state) {
    // Log.d(TAG, "initMvwCallback: 唤醒会话初始化成功");
    // } else {
    // Log.d(TAG, "initMvwCallback: 唤醒会话初始化失败");
    // }
    // }
    // };

    /**
     * 
     * startWakeup_session:(打开对话场景的唤醒). <br/>
     * (方法详述) <br/>
     * void
     */
    public void startWakeup_session() {
        if (mvwSession != null) {
            keywords = MVW_MESSAGE_ID_0 + "," + MVW_LACATION_ID_1 + ","
                    + MVW_NAVIGATE_ID_2 + "," + MVW_BACK_ID_3;
            int errid = FlySvwManager.getInstance().start(SCENCEKEY_SESSION,
                    keywords, myListener);
            Log.d(TAG, "startWakeup_session: errid = " + errid
                    + " | keywords = " + keywords);
        }
    }

    /**
     * 
     * stopWakeup_session:(关闭对话场景的唤醒). <br/>
     * (方法详述) <br/>
     * void
     */
    public void stopWakeup_session() {
        if (mvwSession != null) {
            mvwSession.stop(SCENCEKEY_SESSION);
        }
    }

    /**
     * 
     * startWakeup_session:(打开录音场景的唤醒). <br/>
     * (方法详述) <br/>
     * void
     */
    public void startWakeup_record() {
        if (mvwSession != null) {
            keywords = MVW_SEND_ID_0 + "," + MVW_CANCEL_ID_1;
            int errid = FlySvwManager.getInstance().start(SCENCEKEY_RECORD,
                    keywords, myListener);
            Log.d(TAG, "startWakeup_record: errid = " + errid
                    + " | keywords = " + keywords);
        }
    }

    /**
     * 
     * stopWakeup_session:(关闭录音场景的唤醒). <br/>
     * (方法详述) <br/>
     * void
     */
    public void stopWakeup_record() {
        if (mvwSession != null) {
            mvwSession.stop(SCENCEKEY_RECORD);
        }
    }

    /**
     * 
     * setCustomMvwListener:(会话界面的唤醒回调). <br/>
     * (方法详述) <br/>
     * 
     * @param listener
     *            void
     */
    public void setCustomMvwListener(CustomMvwManagerListener listener) {
        mListener = listener;
    }

    /**
     * setRecordMvwListener:(录音界面的唤醒回调). <br/>
     * (方法详述) <br/>
     * 
     * @param listener
     *            void
     */
    public void setRecordMvwListener(MvwManagerListener listener) {
        mRecordListener = listener;
    }

    public interface CustomMvwManagerListener {
        /**
         * 唤醒—-发消息
         **/
        void onMessage();

        /**
         * 唤醒—-发位置
         **/
        void onLocation();

        /**
         * 唤醒—-导航
         */
        void onNavigate();

        /**
         * 唤醒—-返回
         */
        void onBack();

    }
}
