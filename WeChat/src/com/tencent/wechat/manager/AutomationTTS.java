package com.tencent.wechat.manager;

import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.tencent.wechat.common.Constant;
import com.tencent.wechat.http.entity.ReceiveMsgVO;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * 一个能自动播报TTS文本和语音内容的工具 并且最好还能动态的将 聊天框 的文字 进行显示
 *
 * @author li
 */

public class AutomationTTS {

    private static final String TAG = "AutomationTTS";

    /*缓存未读消息，其线程安全*/
    private LinkedBlockingQueue<ReceiveMsgVO> revicerMsgQueue;

    /*是否正在播报消息*/
    private boolean isTTSPlaying = false;

    /*子线程handler，负责消息的播报*/
    private TtsHandler mTtsHandler;

    private AutomationTTSListenr automationTTSListenr;

    public AutomationTTS(LinkedBlockingQueue<ReceiveMsgVO> revicerMsgQueue, AutomationTTSListenr l) {
        super();
        this.revicerMsgQueue = revicerMsgQueue;
        automationTTSListenr = l;

        SpeakMessageManager.getInstance().registerListener(mSpeakMessageListener);
        AudioWrapperManager.getInstance().registerListener(mOnAudioFocusChangeListener);

        HandlerThread ttsThread = new HandlerThread("tts");
        ttsThread.start();
        mTtsHandler = new TtsHandler(ttsThread.getLooper());
    }

    /*任务，负责从播报队列中取出播报消息，回调会话界面使消息条目向下滚动，调用ttshandler 去播报消息*/
    private Runnable animRunnable = new Runnable() {
        @Override
        public void run() {
            /* 没有数据的时候 立即停止take ，take方法是阻塞的 */
            if (revicerMsgQueue.size() == 0) {
                isTTSPlaying = false;
                Log.d(TAG, "startPlay: revicerMsgQueue.size() == 0");
                AudioWrapperManager.getInstance().abandonAudioFocus();
            } else {
                isTTSPlaying = true;
                try {
                    ReceiveMsgVO currentMsg = revicerMsgQueue.take();
                    if (automationTTSListenr != null) {
                        automationTTSListenr.onPlay(currentMsg);
                    }
                    Message message = mTtsHandler.obtainMessage();
                    message.obj = currentMsg;
                    mTtsHandler.sendMessageDelayed(message, 500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    isTTSPlaying = false;
                    revicerMsgQueue.clear();
                    Dispatch.getInstance().postDelayedByUIThread(animRunnable, 0);
                }
            }
        }
    };

    private class TtsHandler extends Handler {
        public TtsHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            ReceiveMsgVO message = (ReceiveMsgVO) msg.obj;
            isTTSPlaying = true;
            if (message.getMsgType() == Constant.MSGTYPE_TEXT
                    || message.getMsgType() == Constant.MSGTYPE_VOICE
                    || message.getMsgType() == Constant.MSGTYPE_IMAGE
                    || message.getMsgType() == Constant.MSGTYPE_GET_LOCATION
                    || message.getMsgType() == Constant.MSGTYPE_VIDEO
                    || message.getMsgType() == Constant.MSGTYPE_MICROVIDEO
                    || message.getMsgType() == Constant.MSGTYPE_EMOTICON) {
                AudioWrapperManager.getInstance().requestAudioFocus();
                SpeakMessageManager.getInstance().startPlay(message);
            } else {
                /* 其他媒体类型直接忽略 */
                Dispatch.getInstance().postDelayedByUIThread(animRunnable, 0);
            }
        }
    }

    private SpeakMessageManager.SpeakMessageListener mSpeakMessageListener = new SpeakMessageManager.SpeakMessageListener() {
        @Override
        public void onStartPlay(ReceiveMsgVO msg) {

        }

        @Override
        public void onStopPlay(ReceiveMsgVO msg) {
            Log.d(TAG, "onStopPlay: ");
            isTTSPlaying = false;
            Dispatch.getInstance().postDelayedByUIThread(animRunnable, 0);
        }
    };

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                revicerMsgQueue.clear();
                SpeakMessageManager.getInstance().stopPlay();
            }
        }
    };

    /**
     * 添加消息消息到播报队列， 若当前不在播报，则开始播报
     */
    public void addMsg(ReceiveMsgVO msg) {
        revicerMsgQueue.add(msg);
        if (!isTTSPlaying) {
            isTTSPlaying = true;
            Log.d(TAG, "start AutomationTTS");
            Dispatch.getInstance().postDelayedByUIThread(animRunnable, 0);
        }
    }

    /*清理工作，解注册监听器*/
    public void doClearOperation() {
        SpeakMessageManager.getInstance().unRegisterListener(mSpeakMessageListener);
        AudioWrapperManager.getInstance().unRegisterListener(mOnAudioFocusChangeListener);
    }

    public interface AutomationTTSListenr {
        /**
         * 自动播报队列开始播报消息
         *
         * @param currentMsg 被播报的消息
         */
        void onPlay(ReceiveMsgVO currentMsg);
    }

}
