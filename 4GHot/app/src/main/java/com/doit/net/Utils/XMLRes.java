package com.doit.net.Utils;

import android.content.Context;
import android.content.res.Resources;

public class XMLRes {
	
	public static int getResID(Context context,String name, String type){
		int resId = 0;
		try {
			Resources res=context.getResources();
			resId = res.getIdentifier(name,type,context.getPackageName());
			return resId;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public static int getLayoutID(Context context,String name){
		return getResID(context,name,"layout");
	}
	
	public static int getDrawableID(Context context,String name){
		return getResID(context,name,"drawable");
	}
	
	public static int getID(Context context,String name){
		return getResID(context,name,"id");
	}
	
	public static int getStringID(Context context,String name){
		return getResID(context,name,"string");
	}
	
	public static int getStyleID(Context context,String name){
		return getResID(context,name,"style");
	}
	
	public static int getMenuID(Context context,String name){
		return getResID(context,name,"menu");
	}
	
}
