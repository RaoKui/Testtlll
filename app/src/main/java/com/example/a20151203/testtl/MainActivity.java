package com.example.a20151203.testtl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.turing.androidsdk.HttpRequestListener;
import com.turing.androidsdk.RecognizeListener;
import com.turing.androidsdk.RecognizeManager;
import com.turing.androidsdk.TTSListener;
import com.turing.androidsdk.TTSManager;
import com.turing.androidsdk.TuringManager;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity {

    private final String TAG = MainActivity.class.getSimpleName();

    private TTSManager mTtsManager;

    private RecognizeManager mRecognizerManager;

    private TuringManager mTuringManager;

    private TextView mStatus;

    /**
     * 返回结果，开始说话
     */
    public static final int MSG_SPEECH_START = 0;
    /**
     * 开始识别
     */
    public static final int MSG_RECOGNIZE_RESULT = 1;
    /**
     * 开始识别
     */
    public static final int MSG_RECOGNIZE_START = 2;

    /**
     * 申请的turing的apikey（测试使用）
     **/
    private final String TURING_APIKEY = "819459c1510448ccbc2f9ee247ecfd37";
    /**
     * 申请的secret（测试使用）
     **/
    private final String TURING_SECRET = "f9b80e526ff1105a";
    // 百度key（测试使用）
    private final String BD_APIKEY = "VjG6pji6Owjw8nHbBtjXfDj2";
    // 百度screte（测试使用）
    private final String BD_SECRET = "C9GdM4TIcXtciNUHRHBndVkIyGA1wLeS";

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_SPEECH_START:
                    mStatus.setText("开始讲话：" + (String) msg.obj);
                    mTtsManager.startTTS((String) msg.obj);
                    break;
                case MSG_RECOGNIZE_RESULT:
                    mStatus.setText("识别结果：" + msg.obj);
                    if (msg.obj.equals("打开ppt") || msg.obj.equals("打开视频")) {
                        mTtsManager.startTTS("好的，正在为您" + msg.obj);
                        mStatus.setText("开始识别");
                        mRecognizerManager.startRecognize();
                        Intent intent = getPackageManager().getLaunchIntentForPackage("com.example.a20151203.testvoice");
                        startActivity(intent);
                    } else if (msg.obj.equals("你好")) {
                        mTtsManager.startTTS("我在这");
                        mStatus.setText("开始识别");
                        mRecognizerManager.startRecognize();
                        Intent intent = getPackageManager().getLaunchIntentForPackage("com.example.a20151203.testtl");
                        startActivity(intent);
                    } else {
                        mTuringManager.requestTuring((String) msg.obj);
                    }
                    break;
                case MSG_RECOGNIZE_START:
                    mStatus.setText("开始识别");
                    mRecognizerManager.startRecognize();
                    break;
            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStatus = (TextView) findViewById(R.id.tv);

        init();

    }

    /**
     * 初始化turingSDK、识别和tts
     */
    private void init() {
        /** 支持百度，需自行去相关平台申请appid，并导入相应的jar和so文件 */
        mRecognizerManager = new RecognizeManager(this, BD_APIKEY, BD_SECRET);
        mTtsManager = new TTSManager(this, BD_APIKEY, BD_SECRET);
        mRecognizerManager.setVoiceRecognizeListener(myVoiceRecognizeListener);
        mTtsManager.setTTSListener(myTTSListener);
        mTuringManager = new TuringManager(this, TURING_APIKEY,
                TURING_SECRET);
        mTuringManager.setHttpRequestListener(myHttpConnectionListener);
        mTtsManager.startTTS("你好啊");
    }

    /**
     * 网络请求回调
     */
    HttpRequestListener myHttpConnectionListener = new HttpRequestListener() {

        @Override
        public void onSuccess(String result) {
            if (result != null) {
                try {
                    Log.d(TAG, "result" + result);
                    JSONObject result_obj = new JSONObject(result);
                    if (result_obj.has("text")) {
                        Log.d(TAG, result_obj.get("text").toString());
                        mHandler.obtainMessage(MSG_SPEECH_START,
                                result_obj.get("text")).sendToTarget();
                    }
                } catch (JSONException e) {
                    Log.d(TAG, "JSONException:" + e.getMessage());
                }
            }
        }

        @Override
        public void onFail(int code, String error) {
            Log.d(TAG, "onFail code:" + code + "|error:" + error);
            mHandler.obtainMessage(MSG_SPEECH_START, "网络慢脑袋不灵了").sendToTarget();
        }
    };

    /**
     * 语音识别回调
     */
    RecognizeListener myVoiceRecognizeListener = new RecognizeListener() {

        @Override
        public void onVolumeChange(int volume) {
            // 仅讯飞回调
        }

        @Override
        public void onStartRecognize() {
            // 仅针对百度回调
        }

        @Override
        public void onRecordStart() {

        }

        @Override
        public void onRecordEnd() {

        }

        @Override
        public void onRecognizeResult(String result) {
            Log.d(TAG, "识别结果：" + result);
            if (result == null) {
                mHandler.sendEmptyMessage(MSG_RECOGNIZE_START);
                return;
            }
            mHandler.obtainMessage(MSG_RECOGNIZE_RESULT, result).sendToTarget();
        }

        @Override
        public void onRecognizeError(String error) {
            Log.e(TAG, "识别错误：" + error);
            mHandler.sendEmptyMessage(MSG_RECOGNIZE_START);
        }
    };

    /**
     * TTS回调
     */
    TTSListener myTTSListener = new TTSListener() {

        @Override
        public void onSpeechStart() {
            Log.d(TAG, "onSpeechStart");
        }

        @Override
        public void onSpeechProgressChanged() {

        }

        @Override
        public void onSpeechPause() {
            Log.d(TAG, "onSpeechPause");
        }

        @Override
        public void onSpeechFinish() {
            Log.d(TAG, "onSpeechFinish");
            mHandler.sendEmptyMessage(MSG_RECOGNIZE_START);
        }

        @Override
        public void onSpeechError(int errorCode) {
            Log.d(TAG, "onSpeechError：" + errorCode);
            mHandler.sendEmptyMessage(MSG_RECOGNIZE_START);
        }

        @Override
        public void onSpeechCancel() {
            Log.d(TAG, "TTS Cancle!");
        }
    };
}
