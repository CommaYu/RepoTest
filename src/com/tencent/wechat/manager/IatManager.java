package com.tencent.wechat.manager;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.thirdparty.n;
import com.iflytek.utils.log.Logging;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.utils.JsonParser;

/**
 * Author: congqin <br>
 * Data:2016/12/2.<br>
 * Description: 在线听写功能，依赖云平台的msc<br>
 * Note: 从RecordManager 获取音频，送往云端转写成文字<br>
 */
public class IatManager {

    private static final String TAG = IatManager.class.getSimpleName();

    private SpeechRecognizer mIat;

    private Context mContext;

    private static IatManager mInstance;

    private StringBuffer resultBuffer;
    private String result = "";
    private String resultPre = "";// 之前的
    private String resultCur = "";// 现在的

    private IatManager(Context c) {
        mContext = c;
        mIat = SpeechRecognizer.createRecognizer(mContext, mInitListener);
        setParam();
        RecordManager.getInstance().registerRecordListener(mRecordListener);
    }

    public static IatManager getInstance(Context c) {
        if (mInstance == null) {
            mInstance = new IatManager(c);
        }
        return mInstance;
    }

    public static IatManager getInstance() {
        if (mInstance == null) {
            mInstance = new IatManager(WeChatApplication.getContext());
        }
        return mInstance;
    }

    RecordManager.RecordListener mRecordListener = new RecordManager.RecordListener() {
        @Override
        public void onRecordData(byte[] data, int length) {

            // /** 双通道 情况下 修改 */
            // byte[] leftBuffer = new byte[length / 2];
            // int j = 0;
            // for (int i = 0; i < length; i++) {
            // if (i % 4 >= 2) {
            // leftBuffer[j++] = data[i];
            // }
            // }

            // 防止引擎对此进行修改
            final byte[] writeData = data;

            if (null != writeData) {
                sendRecordBuffer(writeData);
            }

        }

        @Override
        public void onRecordStart() {
            resultBuffer = new StringBuffer();
            result = "";
            resultPre = "";
            resultCur = "";
            int ret = mIat.startListening(mRecognizerListener);
            Log.e(TAG, "识别失败,错误码：" + ret);
            if (ret != ErrorCode.SUCCESS) {
                Log.e(TAG, "识别失败,错误码：" + ret);
            } else {
                Log.d(TAG, "识别开始：ret=" + ret);
            }
        }

        @Override
        public void onRecordStop() {
            mIat.stopListening();
        }

        @Override
        public void onRecordCancel() {
            mIat.cancel();
        }

        @Override
        public void onFileSaved(String path) {

        }
    };

    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 引擎类型
        String mEngineType = SpeechConstant.TYPE_CLOUD;
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        String lag = "mandarin";
        if (lag.equals("en_us")) {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
        } else {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        }

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "20000");
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "3000");
        // 设置短信语言
        mIat.setParameter(SpeechConstant.ISE_ENT, "sms16k");
        // 设置实时出识别结果
        mIat.setParameter(SpeechConstant.ASR_DWA, "wpgs");
        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
                Environment.getExternalStorageDirectory() + "/msc/iat.wav");

        // 设置识别输入为外部音频流
        mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
    }

    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            Log.d(TAG, "开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            Logging.d(TAG, "SpeechError:" + error);
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            // showTip(error.getPlainDescription(true));
        }

        @Override
        public void onEndOfSpeech() {
            Log.d(TAG, "========>onEndOfSpeech");
            if (!mIatListenerList.isEmpty()) {
                for (IatListener listener : mIatListenerList) {
                    listener.onEndOfSpeech();
                }
            }
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d(TAG, results.getResultString());
            Log.d(TAG,
                    "onResult: Thread id =" + Thread.currentThread().getId());
            String text = JsonParser.parseIatResult(results.getResultString());
            resultBuffer.append(text);

            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(results.getResultString());
                String pgs = jsonObject.optString("pgs");
                if (pgs.equalsIgnoreCase("apd")) {
                    resultPre = resultPre + resultCur;
                }
                resultCur = JsonParser
                        .parseIatResult(results.getResultString());
                // mResultText.setText(resultPre + resultCur);

                result = resultPre + resultCur;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            
            if (mIatListener != null) {
                mIatListener.onRecognizeResult(result);
            }

            // 删除转写结果末尾，包含“发送发送”
//            if (isLast && !mIatListenerList.isEmpty()) {
//                String res = resultBuffer.toString();
//                Logging.d(TAG, "onResult: " + res);
//                int index = res.lastIndexOf("发送发送");
//                if (index == 0 && res.length() == index + 5) {
//                    return;
//                }
//                if (index > 0) {
//                    res = res.substring(0, index)
//                            + res.substring(index + 4, res.length());
//                }
//                res = res.replace("，。", "。");
//                res = res.replace("？。", "？");
//                if (res.contains("取消取消")) {
//                    Log.d(TAG, "onResult contain 取消取消!");
//                    return;
//                }
//                for (IatListener listener : mIatListenerList) {
//                    listener.onRecognizeResult(res);
//                }
//            }
            
            if (isLast && !mIatListenerList.isEmpty()) {
                String res = result;
                Logging.d(TAG, "onResult: " + res);
                int index = res.lastIndexOf("发送发送");
                if (index == 0 && res.length() == index + 5) {
                    return;
                }
                if (index > 0) {
                    res = res.substring(0, index)+ res.substring(index + 4, res.length());
                }
                res = res.replace("，。", "。");
                res = res.replace("？。", "？");
                if (res.contains("取消取消")) {
                    Log.d(TAG, "onResult contain 取消取消!");
                    return;
                }
                for (IatListener listener : mIatListenerList) {
                    listener.onRecognizeResult(res);
                }
                if (mIatListener != null) {
                    mIatListener.onRecognizeResult(result);
                }
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            Log.d(TAG, "返回音频数据：" + data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };

    /************************ 增加识别处理线程 ******************************/

    private HandlerThread recordHandlerThread;
    private Handler recordHandler;

    /**
     * 初始化处理线程
     */
    public void initRecordHandlerThread() {
        recordHandlerThread = new HandlerThread("iat-recordHandlerThread");
        recordHandlerThread.start();
        if (recordHandlerThread != null) {
            recordHandler = new Handler(recordHandlerThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {

                    super.handleMessage(msg);

                    Bundle bundle = msg.getData();
                    // long startTime = bundle.getLong("START_TIME");
                    byte[] buffer = bundle.getByteArray("RECORD_BUFFER");
                    if (null != buffer && null != mIat) {
                        mIat.writeAudio(buffer, 0, buffer.length);
                    }
                }
            };

            Log.d(TAG, "initRecordHandlerThread init finish--->");
        } else {
            Log.d(TAG, "initRecordHandlerThread init fail--->");
        }
    }

    /**
     * 发送录音给识别引擎
     * 
     * @param buffer
     */
    public void sendRecordBuffer(byte[] buffer) {

        Bundle bundle = new Bundle();
        bundle.putByteArray("RECORD_BUFFER", buffer);
        Message msg = new Message();
        msg.setData(bundle);

        try {
            if (recordHandler == null || recordHandlerThread == null) {
                initRecordHandlerThread();
            }
            recordHandler.sendMessage(msg);
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /******************** 增加唤醒音频处理线程 end *************************/

    private List<IatListener> mIatListenerList = new ArrayList<IatListener>();
    
    private IatListener mIatListener;

    public void registerIatListener(IatListener listener) {
        if (!mIatListenerList.contains(listener)) {
            mIatListenerList.add(listener);
        }
    }

    public void unRegisterIatListener(IatListener listener) {
        if (mIatListenerList.contains(listener)) {
            mIatListenerList.remove(listener);
        }
    }
    
    public void registerRealTimeListener(IatListener listener) {
        mIatListener = listener;
    }
    
    public void unRegisterRealTimeListener() {
        mIatListener = null;
    }

    public interface IatListener {
        /**
         * 当识别结果返回
         * 
         * @param result
         *            识别结果（即转写的文字）
         */
        void onRecognizeResult(String result);

        /**
         * 检测到语音结束
         */
        void onEndOfSpeech();
    }

    /* 转写功能的初始化监听器 */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Log.e(TAG, "初始化失败，错误码：" + code);
            }
        }
    };
}
