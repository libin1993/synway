package com.doit.net.Utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.doit.net.Utils.Logger.getLogger;

/**
 * Created by wiker on 2016/4/29.
 */
public class BaiduAudio {
    private final static Logger log = getLogger(BaiduAudio.class);

    private Context mContext;
    public BaiduAudio(Context context){
        mContext = context;
        /*
        try {
            long start = System.currentTimeMillis();
            initialEnv();
            log.info("initialEnv cost:"+(System.currentTimeMillis()-start)+" ms");
            start = System.currentTimeMillis();
            initialTts();
            log.info("initialTts cost:"+(System.currentTimeMillis()-start)+" ms");
        } catch (Exception e) {
            log.error("初始化语音异常");
        }
        */
    }


    private void initialTts() {
        /*
        this.mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        this.mSpeechSynthesizer.setContext(mContext);
//        this.mSpeechSynthesizer.setSpeechSynthesizerListener(mContext);
        // 文本模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setCommonParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, mSampleDirPath + "/"
                + TEXT_MODEL_NAME);
        // 声学模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setCommonParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, mSampleDirPath + "/"
                + SPEECH_FEMALE_MODEL_NAME);
        // 本地授权文件路径,如未设置将使用默认路径.设置临时授权文件路径，LICENCE_FILE_NAME请替换成临时授权文件的实际路径，仅在使用临时license文件时需要进行设置，如果在[应用管理]中开通了离线授权，不需要设置该参数，建议将该行代码删除（离线引擎）
//        this.mSpeechSynthesizer.setCommonParam(SpeechSynthesizer.PARAM_TTS_LICENCE_FILE, mSampleDirPath + "/"
//                + LICENSE_FILE_NAME);
        // 请替换为语音开发者平台上注册应用得到的App ID (离线授权)
        this.mSpeechSynthesizer.setAppId("3324485");
        // 请替换为语音开发者平台注册应用得到的apikey和secretkey (在线授权)
        this.mSpeechSynthesizer.setApiKey("i9gPLt8VaAHtORzdjW3KXHHL", "nGyqrlgwfoGpBtrUhq76fanIlxBAefYG");
        // 发音人（在线引擎），可用参数为0,1,2,3。。。（服务器端会动态增加，各值含义参考文档，以文档说明为准。0--普通女声，1--普通男声，2--特别男声，3--情感男声。。。）
        this.mSpeechSynthesizer.setCommonParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置Mix模式的合成策略
        this.mSpeechSynthesizer.setCommonParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI);
        this.mSpeechSynthesizer.setCommonParam(SpeechSynthesizer.PARAM_VOCODER_OPTIM_LEVEL, "2");
        this.mSpeechSynthesizer.setCommonParam(SpeechSynthesizer.PARAM_SPEED, "8");
        this.mSpeechSynthesizer.setCommonParam(SpeechSynthesizer.PARAM_PITCH, "8");
        // 授权检测接口(可以不使用，只是验证授权是否成功)
//        AuthInfo authInfo = this.mSpeechSynthesizer.auth(TtsMode.MIX);
//        if (authInfo.isSuccess()) {
//            log.info("auth success");
//        } else {
//            String errorMsg = authInfo.getTtsError().getDetailMessage();
//            log.error("auth failed errorMsg=" + errorMsg);
//        }
        // 初始化tts
        mSpeechSynthesizer.initTts(TtsMode.MIX);
        // 加载离线英文资源（提供离线英文合成功能）
//        int result = mSpeechSynthesizer.loadEnglishModel(mSampleDirPath + "/" + ENGLISH_TEXT_MODEL_NAME, mSampleDirPath  + "/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
//        toPrint("loadEnglishModel result=" + result);

*/
    }

    public void pause() {
        /*
        this.mSpeechSynthesizer.pause();
        */
    }

    public void setVolume(String volume){
        /*
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME,volume);
        */
    }
    public void release(){
        /*
        mSpeechSynthesizer.release();
        */
    }

    public void resume() {
        /*
        this.mSpeechSynthesizer.resume();
        */
    }

    public void stop() {
        /*
        this.mSpeechSynthesizer.stop();
        */
    }
    public void speak(String text) {
        /*
        int result = this.mSpeechSynthesizer.speak(text);
        if (result < 0) {
            log.error("error,please look up error code in doc or URL:http://yuyin.baidu.com/docs/tts/122 ");
        }
        */
    }
//    private SpeechSynthesizer mSpeechSynthesizer;
    private String mSampleDirPath;
    private static final String SAMPLE_DIR_NAME = ".ucsi";
    private static final String SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female.dat";
    private static final String SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male.dat";
    private static final String TEXT_MODEL_NAME = "bd_etts_text.dat";
    private static final String LICENSE_FILE_NAME = "temp_license_2016-04-29.txt";
    private static final String ENGLISH_SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female_en.dat";
    private static final String ENGLISH_SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male_en.dat";
    private static final String ENGLISH_TEXT_MODEL_NAME = "bd_etts_text_en.dat";

    private void initialEnv() {
        if (mSampleDirPath == null) {
            String sdcardPath = Environment.getExternalStorageDirectory().toString();
            mSampleDirPath = sdcardPath + "/" + SAMPLE_DIR_NAME;
        }
        makeDir(mSampleDirPath);
        copyFromAssetsToSdcard(false, SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/" + SPEECH_FEMALE_MODEL_NAME);
//        copyFromAssetsToSdcard(false, SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/" + SPEECH_MALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, TEXT_MODEL_NAME, mSampleDirPath + "/" + TEXT_MODEL_NAME);
        copyFromAssetsToSdcard(false, LICENSE_FILE_NAME, mSampleDirPath + "/" + LICENSE_FILE_NAME);
//        copyFromAssetsToSdcard(false, "english/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/"
//                + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
//        copyFromAssetsToSdcard(false, "english/" + ENGLISH_SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/"
//                + ENGLISH_SPEECH_MALE_MODEL_NAME);
//        copyFromAssetsToSdcard(false, "english/" + ENGLISH_TEXT_MODEL_NAME, mSampleDirPath + "/"
//                + ENGLISH_TEXT_MODEL_NAME);
    }

    private void makeDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 将sample工程需要的资源文件拷贝到SD卡中使用（授权文件为临时授权文件，请注册正式授权）
     *
     * @param isCover 是否覆盖已存在的目标文件
     * @param source
     * @param dest
     */
    private void copyFromAssetsToSdcard(boolean isCover, String source, String dest) {
        File file = new File(dest);
        if (isCover || (!isCover && !file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = mContext.getResources().getAssets().open(source);
                String path = dest;
                fos = new FileOutputStream(path);
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
