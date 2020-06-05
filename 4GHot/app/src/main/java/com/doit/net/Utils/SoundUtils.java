package com.doit.net.Utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.HashMap;
/// 用来播放游戏音效的类，建议只放小于7S并且经常播放的声音
public class SoundUtils {
    public enum PlayState{
        STOP,
        PLAYING,
        PAUSED
     }

    private final int mSoundNumMax = 4;
	private SoundPool soundPool = null; // 声明SoundPool的引用
	private HashMap<String, SoundDetail> hashMapSound = new HashMap<String, SoundDetail>(); // 创建HashMap对象
	private AudioManager audioManger = null;
	private Context SoundContext = null;
	private  boolean bSoundOff = false;
	private  float curVol = 1.0f;//取值0.0 -1.0f

	// /初始化声音池 iSoundNumMax 声音池最大声音数量 ，建议只放小于7S并且经常播放的声音 格式最好为ogg
	public SoundUtils(Context context) {
		if (context != null) {
			SoundContext = context;
		} else {
			LogUtils.log("initSoundPool context==NULL!");
			return;
		}
		if (soundPool == null) {
			soundPool = new SoundPool(mSoundNumMax, AudioManager.STREAM_MUSIC,0); // 创建SoundPool对象
		}
	}
	// / SoundName 声音的名字，SoundResId资源 id ，添加失败，返回零
	public int addSound(String SoundName, int SoundResId) {
		if (hashMapSound.get(SoundName) == null) {// /不存在这个名字的声音		
			SoundDetail sDetail = new SoundDetail();
			sDetail.SoundName = SoundName;
			sDetail.iSoundId = soundPool.load(SoundContext, SoundResId, 0);// 加载声音文件
			sDetail.iPlaytate = PlayState.STOP;
			sDetail.loopTimes = 0;
			sDetail.iStreamId = 0;
			hashMapSound.put(SoundName,sDetail); 
			if (sDetail.iSoundId <= 0 || sDetail.iSoundId > 255) {
                LogUtils.log("warning: addSound the sound error! name = " + SoundName);
                return 0;
            }else{
				LogUtils.log( "addSound the sound name = "
                        + SoundName + "soundId = " + sDetail.iSoundId);
            }
			return 1;
		} else {
			LogUtils.log("warning: addSound the same sound name = "
					+ SoundName);
			return 0;
		}
	}
    // /移除失败 返回 零
	public int removeSound(String SoundName) {
		SoundDetail sDetail = hashMapSound.get(SoundName);
		if (sDetail.iSoundId <= 0) {
			LogUtils.log("warning: removeSound no exist the sound name = "
							+ SoundName);
			return 0;
		}
		soundPool.unload(sDetail.iSoundId);
		hashMapSound.remove(SoundName);
		return 1;
	}
	/**
	 * 
	 * 设置游戏音效音量
	 *
	 * @param vol 取值为0.0 - 1.0f 表示取当前媒体音量的幅度
	 */
	public void setVolume(float vol) {
		LogUtils.log( "setVolume vol =" + vol);
	    
	    if(vol < 0){
	        this.curVol = 0f;
	    }
	        
	    if(vol > 1){
	        this.curVol = 1.0f;
	    }
	    
	    this.curVol  =   vol;
	}
	
	///bSoundState = true 游戏静音，默认 false
	public void setSilence(boolean bSoundState){	    
		bSoundOff = bSoundState;
	}
	/**
	 * 
	 * 获取当前音量 
	 *
	 * @return  0.0 - 1.0f 表示当前音量是当前 媒体音量的幅度
	 */
	public float getVolume() {
		return this.curVol;
	}
	// /获取最大的音量
	public int getMaxVolume() {
		if (audioManger != null) {
			return audioManger.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		} else {
			LogUtils.log("getMaxVolume audioManger not init!");
			return 15;
		}
	}
	
	/**
	 * 在play之前调用
	 * 设置循环播放次数（默认为： 0，播放一次）
	 * 设置为 -1，无限循环
	 * @param loopTimes  循环次数
	 */
	public void setLoop(String name,int loopTimes){
	    if(bSoundOff){
			LogUtils.log("setLoop soundPool sound off!"+ " name = " + name);
        }
        
        SoundDetail sDetail = hashMapSound.get(name);
        
        if(sDetail == null){
			LogUtils.log("setLoop sDetail==null! name = " + name+ " name = " + name);
        }
        
        sDetail.loopTimes = loopTimes;
        hashMapSound.put(name, sDetail);
	}
	
	/**
     * 
     * 播放声音
     *
     * @param soundName 声音的名字
     * @return 失败 0，成功 大于零
     */
    public int play(String soundName) {
        return play(soundName,0,true);
    }
    
    public int playNS(String soundName) {
        return play(soundName,0,false);
    }
	
	/**
	 * 
	 * 播放声音
	 *
	 * @param soundName 声音的名字
	 * @param times 循环次数 0 只播放一次， -1 无线循环
	 * @return 失败 0，成功 大于零
	 */
	public int play(String soundName,int times,boolean needStop) {
		if(bSoundOff){
			LogUtils.log( "play soundPool sound off!"+ " name = " + soundName);
			return 0;
		}
		
		// 偶现soundPool为Null指针
        if(soundPool == null){
			LogUtils.log("soundPool================================== null");
            return 0;
        }
		
        if(needStop){
            stop(soundName);//声音很短，有可能在stop时候，声音已经播放完毕了
        }
				
		SoundDetail sDetail = hashMapSound.get(soundName);
		
		if (sDetail.iSoundId <= 0) {
			LogUtils.log(
					"play iStreamId error! id=" + sDetail.iSoundId + " name = " + soundName);
			return 0;
		}
		
		float fVolume = curVol;
		
		
		
		// 调用SoundPool的play方法来播放声音文件
		//sDetail.iStreamId = soundPool.play(sDetail.iSoundId, fVolume, fVolume, 1, sDetail.loopTimes, 1.0f);
		sDetail.iStreamId = soundPool.play(sDetail.iSoundId, fVolume, fVolume, 1, times, 1.0f);
		sDetail.iPlaytate = PlayState.PLAYING;//播放中
		hashMapSound.put(soundName, sDetail);
		
		return sDetail.iStreamId;
	}
	public void stop(String soundName) {
		if(bSoundOff){
			LogUtils.log("stop soundPool sound off!"+ " name = " + soundName);
			return;
		}
		
		// 偶现soundPool为Null指针
        if(soundPool == null){
            return;
        }
		SoundDetail sDetail = hashMapSound.get(soundName);
		
		if(sDetail.iStreamId <= 0){
			LogUtils.log("stop iStreamId error! id=" + sDetail.iStreamId + " name = " + soundName);
			return;
		}
				
		if( sDetail.iPlaytate == PlayState.PLAYING || sDetail.iPlaytate == PlayState.PAUSED){
			soundPool.stop(sDetail.iStreamId);
			sDetail.iPlaytate = PlayState.STOP;///停止
			sDetail.iStreamId = 0;
			hashMapSound.put(soundName, sDetail);
		}else{
			LogUtils.log( "stop soundPool invalid iPlayState=" + sDetail.iPlaytate + " name = " + soundName);
		}
	}
	public void pause(String soundName) {		
	    // 偶现soundPool为Null指针
        if(soundPool == null){
            return;
        }
        
		SoundDetail sDetail = hashMapSound.get(soundName);
		
		if(sDetail.iStreamId <= 0){
			LogUtils.log("pause iStreamId error! id=" + sDetail.iStreamId + " name = " + soundName);
		}
			
		if(sDetail.iPlaytate == PlayState.PLAYING){
			soundPool.pause(sDetail.iStreamId);
			sDetail.iPlaytate = PlayState.PAUSED;///暂停
			hashMapSound.put(soundName, sDetail);
		}else{
			LogUtils.log( "pause soundPool invalid iPlayState=" + sDetail.iPlaytate+ " name = " + soundName);
		}
	}
	public void resume(String soundName) {		
	    // 偶现soundPool为Null指针
        if(soundPool == null){
            return;
        }
	    
		SoundDetail sDetail = hashMapSound.get(soundName);
		
		if(sDetail.iStreamId <= 0){
			LogUtils.log("resume iStreamId error! id=" + sDetail.iStreamId + " name = " + soundName);
		}
				
		if(sDetail.iPlaytate == PlayState.PAUSED){
			soundPool.resume(sDetail.iStreamId);
			sDetail.iPlaytate = PlayState.PLAYING;///播放
		}else{
			LogUtils.log( "resume soundPool invalid iPlayState="+ sDetail.iPlaytate+ " name = " + soundName);
		}
	}
	
	class SoundDetail{	     
        public String SoundName;///声音名字
		public int iSoundId;///声音load到soundPool后的id
		public int iStreamId;//soundPool播放时返回的id，用于控制暂停等
		public PlayState iPlaytate;///播放状态  0 停止，1 播放中，2 暂停
		public int loopTimes;//循环播放次数（-1 无限循环）
	}
    public void release() {
        if(soundPool != null){
            soundPool.release();
        }
        soundPool = null;
    }
}
