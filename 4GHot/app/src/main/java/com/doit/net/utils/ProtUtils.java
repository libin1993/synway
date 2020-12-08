package com.doit.net.utils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;


public class ProtUtils {


	public static int readIntHtonl(byte[] data, int pos)
	{
		byte[] tmp=new byte[4];
		System.arraycopy(data, pos, tmp, 0, 4);
		return ByteUtils.htonl(((tmp[3]&0xFF)<<24) | ((tmp[2]&0xFF)<<16) | ((tmp[1]&0xFF)<<8) | (tmp[0]&0xFF));
	}

	public static int readInt(byte[] data, int pos)
	{
		byte[] tmp=new byte[4];
		System.arraycopy(data, pos, tmp, 0, 4);
		return ((tmp[3]&0xFF)<<24) | ((tmp[2]&0xFF)<<16) | ((tmp[1]&0xFF)<<8) | (tmp[0]&0xFF);
	}
	
	public static int swapShort(int port)
	{
		return ((port&0xff)<<8) | (port>>8);
	}
	
	public static String getIpV4Str(byte[] data, int pos)
	{
		return String.format("%d.%d.%d.%d", data[pos]&0xff, data[pos+1]&0xff, data[pos+2]&0xff, data[pos+3]&0xff);
	}

	/**
	 * IP地址转byte数组
	 * @param ip
	 * @return
     */
	public static byte[] ipToBytes(String ip){
		try {
			String[] ipArr = ip.split("\\.");
			byte[] ipBits = new byte[4];
			for(int i=0;i<ipArr.length;i++){
                ipBits[i] = (byte)Integer.parseInt(ipArr[i]);
            }
			return ipBits;
		} catch (Exception e) {
			LogUtils.log("IP地址转换异常"+e);
		}
		return new byte[4];
	}

    /**
     * 数字字符串转为byte数据
     * @param str
     * @return
     */
	public static byte[] intStrToBytes(String str){
		try {
			char[] ipArr = str.toCharArray();
			byte[] ipBits = new byte[ipArr.length];
			for(int i=0;i<ipArr.length;i++){
				ipBits[i] = (byte)Integer.parseInt(String.valueOf(ipArr[i]));
			}
			return ipBits;
		} catch (Exception e) {
			LogUtils.log("转换异常"+e);
		}
		return new byte[4];
	}
	
	public static int readShortHtons(byte[] data, int pos)
	{
		return ByteUtils.htons((short)readShort(data,pos));
		//先去掉short的字节序，2016-03-24
//		return (short)(((tmp[1]&0xFF)<<8) | (tmp[0]&0xFF));
	}

	public static int readShort(byte[] data, int pos)
	{
		byte[] tmp=new byte[2];
		System.arraycopy(data, pos, tmp, 0, 2);
		return (short)(((tmp[1]&0xFF)<<8) | (tmp[0]&0xFF));
		//先去掉short的字节序，2016-03-24
//		return (short)(((tmp[1]&0xFF)<<8) | (tmp[0]&0xFF));
	}
	
	public static byte[] int2Byte(int data)
	{
		byte[] tmp=new byte[4];
		for(int i=0; i<4; i++)
			tmp[i]=(byte)((data>>(8*i))&0xFF);
		return tmp;
	}
	
	public static byte[] short2Byte(int data)
	{
		byte[] tmp=new byte[2];
		tmp[1]=(byte)(data&0xFF);
		tmp[0]=(byte)((data>>8)&0xFF);
		return tmp;
	}
	
	static public void arraycopy(byte[] dest, int destPos, String src)
	{
		try {
			byte[] data=src.getBytes(StandardCharsets.UTF_8);
			int copylen=Math.min(dest.length-destPos, data.length);
			System.arraycopy(data, 0, dest, destPos, copylen);
		} catch (Exception e) {
			LogUtils.log(e.getMessage());
		}
		
	}

	public static String getHexByString(byte[] bytes){
		String s = "";
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(bytes[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			s += hex+ " ";
		}
		return s.toUpperCase();
	}
	
	static public String getString(byte[] data)
	{
		int len=data.length;
		int i=0;
		for(i=0; i<len; i++)
		{
			if(data[i]==0)
				break;
		}
		if(i==0)
			return null;
		return new String(data, 0, i);
	}
	
	static public String getString(byte[] data, String charset)
	{
		try {
			int len=data.length;
			int i=0;
			for(i=0; i<len; i++)
			{
				if(data[i]==0)
					break;
			}
			if(i==0)
				return null;
			return new String(data, 0, i, charset);
        } catch (Exception e) {
			LogUtils.log(e.getMessage());
        }
		return null;
	}
	
	static public void delay(long ms)
	{
		try {
			Thread.sleep(ms);
		} catch (Exception e) {
			LogUtils.log(e.getMessage());
		}
	}
	
	public static void getRand(byte[] data, int len)
	{
		for(int i=0; i<len; i++)
		{
			int b=(int)(Math.random()*62);
			if(b<10)
				data[i]=(byte)(b+'0');
			else if(b>=10 && b<36)
				data[i]=(byte)(b-10+'a');
			else if(b>=36 && b<62)
				data[i]=(byte)(b-36+'A');
		}
	}
	
	static class BigIntegerEx extends BigInteger {

		public BigIntegerEx(int signum, byte[] magnitude) {
			super(signum, magnitude);
		}

		@Override
		public String toString(int radix) {
			String ret = super.toString(radix);
			if (ret.length() != 32) {
				ret = "00000000000000000000000000000000".substring(0, 32 - ret.length()) + ret;
			}
			return ret;
		}
	}
	
	public static String getMD5Str(byte[] data)
	{
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("MD5");
			digest.update(data, 0, data.length);
			BigIntegerEx bigInt = new BigIntegerEx(1, digest.digest());
			return bigInt.toString(16);
        } catch (Exception e) {
			LogUtils.log(e.getMessage());
        }
		return null;
	}
	
	public static byte[] getMD5Byte(byte[] data)
	{
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("MD5");
			digest.update(data, 0, data.length);
			return digest.digest();
        } catch (Exception e) {
			LogUtils.log(e.getMessage());
        }
		return null;
	}
	
	public static String getFileMD5(File file) {
		if (!file.isFile()) {
			return null;
		}
		MessageDigest digest = null;
		FileInputStream in = null;
		byte[] buffer = new byte[10240];
		int len;
		try {
			digest = MessageDigest.getInstance("MD5");
			in = new FileInputStream(file);
			DataInputStream din = new DataInputStream(in);
			while ((len = din.read(buffer, 0, 10240)) > 0) {
				digest.update(buffer, 0, len);
			}
			din.close();
			in.close();
		} catch (Exception e) {
			LogUtils.log(e.getMessage());
			return null;
		}
		BigInteger bigInt = new BigInteger(1, digest.digest());
		return bigInt.toString(16);
	}

	public static void main(String[] args) {
//		PrintUtils.printHex(ipToBytes("192.168.1.232"));
		System.out.println(getHexByString(new byte[]{61,62,63}));
	}
}
