package com.tencent.wechat.manager;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author: congqin<br>
 * Data: 2017/1/5<br>
 * Description:<br>
 * Note:<br>
 */
public class Dispatch {

    private static Dispatch mInstance;

    private Handler uiHandler;


    private HandlerThread mWorkThread;
    private Handler mWorkHandler;
    private HandlerThread mExecutorsThread;
    private Handler mExecutorsHandler;

    public static Dispatch getInstance() {
        if (mInstance == null) {
            mInstance = new Dispatch();
        }
        return mInstance;
    }

    private Dispatch() {

        uiHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                MessageEntity messageEntity= (MessageEntity) msg.obj;
                Message message=messageEntity.message;
                HandleListener handleListener=messageEntity.handleListener;
                handleListener.handleMessage(message);
            }
        };

        mWorkThread = new HandlerThread("work Thread");
        mWorkThread.start();
        mWorkHandler = new Handler(mWorkThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                MessageEntity messageEntity= (MessageEntity) msg.obj;
                Message message=messageEntity.message;
                HandleListener handleListener=messageEntity.handleListener;
                handleListener.handleMessage(message);
            }
        };

        mExecutorsThread = new HandlerThread("ExecutorsThread");
        mExecutorsThread.start();
        mExecutorsHandler = new Handler(mExecutorsThread.getLooper());
    }

    /**
     * 非ui线程执行
     *
     * @param message
     * @param delayMilli
     * @param handleListener
     */
    public void sendMessageDelay(Message message, long delayMilli, HandleListener handleListener) {
        Message msg = uiHandler.obtainMessage();
        msg.obj = new MessageEntity(message, handleListener);
        mWorkHandler.sendMessageDelayed(msg, delayMilli);
    }

    /**
     * ui线程执行
     *
     * @param message
     * @param delayMilli
     * @param handleListener
     */
    public void sendMessageDelayUiThread(Message message, long delayMilli, HandleListener handleListener) {
        Message msg = uiHandler.obtainMessage();
        msg.obj = new MessageEntity(message, handleListener);
        uiHandler.sendMessageDelayed(msg, delayMilli);
    }

    /**
     * 在UI线程中延迟调用 <br/>
     *
     * @param r
     * @param delayMillis
     */
    public void postDelayedByUIThread(Runnable r, long delayMillis){
        uiHandler.postDelayed(r, delayMillis);
    }

    public void postRunnableByExecutors(Runnable r, long delayMillis){
        mExecutorsHandler.postDelayed(r, delayMillis);
    }

    public void removeRunnableByExecutors(Runnable r){
        mExecutorsHandler.removeCallbacks(r);
    }



    private class MessageEntity {
        private Message message;
        private HandleListener handleListener;

        public MessageEntity(Message message, HandleListener handleListener) {
            this.message = message;
            this.handleListener = handleListener;
        }
    }

    public interface HandleListener {
        void handleMessage(Message msg);
    }

}
