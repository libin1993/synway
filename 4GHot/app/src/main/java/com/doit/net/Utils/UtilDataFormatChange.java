package com.doit.net.Utils;

import java.io.UnsupportedEncodingException;

public class UtilDataFormatChange 
{
	/**
	 * int 转byte[]
	 * @param tempValue
	 * @return
	 */
	public static byte[] IntToByteArray(int tempValue)
	{
		byte[] result = new byte[4];
		result[0] = (byte) (tempValue >> 24);
		result[1] = (byte) (tempValue >> 16);
		result[2] = (byte) (tempValue >> 8);
		result[3] = (byte) (tempValue >> 0);
		return result;
	}
	
	/**
	 * short 转byte[]
	 * @param tempValue
	 * @return
	 */
	public static byte[] shortToByteArray(short tempValue) 
	{
		  byte[] shortBuf = new byte[2];
		  for(int i=0;i<2;i++) 
		  {
		     int offset = (shortBuf.length - 1 -i)*8;
		     shortBuf[i] = (byte)((tempValue>>>offset)&0xff);
		  }
		  return shortBuf;
	}
	
	/**
	 * byte[] 转short
	 * @param
	 * @return
	 */
	public static short byteToShort(byte[] tempValue) 
	{ 
		 return (short)((tempValue[0] << 8)+ (tempValue[1] & 0xFF));
    }
	
	/**
	 * byte[] 转int
	 * @param tempValue
	 * @return
	 */
	public static int byteToInt(byte[] tempValue)
	{
		int[] tempInt=new int[4];
		tempInt[3]=(tempValue[3] & 0xff)<<0;
		tempInt[2]=(tempValue[2] & 0xff)<<8;
		tempInt[1]=(tempValue[1] & 0xff)<<16;
		tempInt[0]=(tempValue[0] & 0xff)<<24 ;
		return tempInt[0]+tempInt[1]+tempInt[2]+tempInt[3];
	}
	
	
	
	/**
	 * 将字节数组转换为String
	 * @param tempValue
	 * @param index
	 * @return
	 */
	public static String bytesToString(byte[] tempValue, int index)
	{
		StringBuffer result = new StringBuffer("");
		int length = tempValue.length;
		for (int i = index; i < length; i++) 
		{
			result.append((char) (tempValue[i] & 0xff));
		}
		return result.toString();
	}
	
	/**
	 * 将字节数组转换为String
	 * @param tempValue
	 * @param index
	 * @return
	 */
	public static String bytesToString(byte[] tempValue, int index, int count)
	{
		StringBuffer result = new StringBuffer("");
		int length =index+count;
		for (int i = index; i <length; i++) 
		{
			result.append((char) (tempValue[i] & 0xff));
		}
		return result.toString();
	}
	
	
	
	//将字节数组转成十进制的数组合成的字符串
	public static String bytesToTendString(byte[] tempValue, int index)
	{
		StringBuffer result = new StringBuffer("");
		for (int i = index; i < tempValue.length-index; i++) 
		{
			result.append(tempValue[i]);
		}
		return result.toString();
	}
	

	/**
	 * 将传入的字符串转换成byte[]
	 * @param tempValue
	 * @return
	 */
	public static byte[]  stringtoBytesForASCII(String tempValue)
	{
		try 
		{
		   return tempValue.getBytes("US-ASCII");
		} 
		catch (Exception ex) {}
		return null;
	}
	
	/**
	 * byte[] 转16进制
	 * @param tempValue
	 * @return
	 */
	public static String bytesToHexString(byte[] tempValue)
	{   
	    StringBuilder stringBuilder = new StringBuilder("");
	    if (tempValue == null || tempValue.length <= 0) 
	    {   
	        return null;   
	    }   
	    for (int i = 0; i < tempValue.length; i++) 
	    {   
	        int v = tempValue[i] & 0xFF;   
	        String hv = Integer.toHexString(v);
	        if (hv.length() < 2) 
	        {   
	            stringBuilder.append(0);   
	        }   
	        stringBuilder.append(hv);   
	    }   
	    return stringBuilder.toString();   
	} 
	
	
	/**
	 * byte[] 转16进制 显示为0x01 0x02样式
	 * @param tempValue
	 * @return
	 */
	public static String bytesToHexString2(byte[] tempValue, int index, int count)
	{   
	    StringBuilder stringBuilder = new StringBuilder("");
	    if (tempValue == null || tempValue.length <= 0) 
	    {   
	        return null;   
	    }   
	    for (int i = 0; i < count; i++) 
	    {   
	        int v = tempValue[i] & 0xFF;   
	        String hv = Integer.toHexString(v);
	        if (hv.length() < 2) 
	        {   
	            stringBuilder.append(0);   
	        }   
	        stringBuilder.append("0x"+hv+" ");   
	    }   
	    return stringBuilder.toString();   
	} 
	
	
	/**
	 * byte[] 转16进制
	 * @param tempValue
	 * @return
	 */
	public static String bytesToHexString(byte tempValue)
	{   
	    StringBuilder stringBuilder = new StringBuilder("");
	    int v = tempValue & 0xFF;   
	    String hv = Integer.toHexString(v);
	    if (hv.length() < 2) 
	    {   
	        stringBuilder.append(0);   
	    }   
	    stringBuilder.append(hv);   
	    return stringBuilder.toString();   
	} 
	
	
	/**
	 * Int值转成IP
	 * @return String
	 */
	public static String intToIP(long tempValue)
	{
		tempValue=reverseInt(tempValue);
		StringBuilder sb=new StringBuilder();
	    sb.append((tempValue>>24) & 0xFF).append(".");
	    sb.append((tempValue>>16) & 0xFF).append(".");
	    sb.append((tempValue>>8) & 0xFF).append(".");
	    sb.append(tempValue& 0xFF);
	    return sb.toString();
	    
	}
	
	/** 
	* 将int类型的值转换为字节序颠倒过来对应的int值 
	* @param i int 
	* @return int 
	*/  
	public static long reverseInt(long tempValue) 
	{  
		long result =lBytesToInt(toHH(tempValue));  
		return result;  
	}  

	/**高字节转IP时使用*/
	public static int lBytesToInt(byte[] b) 
	{  
		int s = 0;  
		for (int i = 0; i < 3; i++) 
		{  
			if (b[3-i] >= 0) 
			{  
				s = s + b[3-i];  
			} 
			else 
			{  
			  s = s + 256 + b[3-i];  
			}  
			s = s * 256;  
		}  
		if (b[0] >= 0) 
		{  
			s = s + b[0];  
		} 
		else 
		{  
			s = s + 256 + b[0];  
		}  
		 return s;  
	}   

	/** 
	* 将int转为高字节在前，低字节在后的byte数组 
	* @param n int 
	* @return byte[] 
	*/  
	public static byte[] toHH(long n) 
	{  
		byte[] b = new byte[4];  
		b[3] = (byte) (n & 0xff);  
		b[2] = (byte) (n >> 8 & 0xff);  
		b[1] = (byte) (n >> 16 & 0xff);  
		b[0] = (byte) (n >> 24 & 0xff);  
		return b;  
	} 
	
	/** 
	 * 将int转为低字节在前，高字节在后的byte数组 
	 * @param n int 
	 * @return byte[] 
	 */  
	public static byte[] toLH(long n) 
	{  
		byte[] b = new byte[4];  
		b[0] = (byte) (n & 0xff);  
		b[1] = (byte) (n >> 8 & 0xff);  
		b[2] = (byte) (n >> 16 & 0xff);  
		b[3] = (byte) (n >> 24 & 0xff);  
		return b;  
	} 
	
	/**
	 * SMS编码转换
	 */
	public static String toSMSStr(String tempStr)
	{
		byte[] byteSms = get2ByteFromString(tempStr);
		String realSms = "";
		try
		{ //USC2解码
			realSms = new String(byteSms,"UTF-16BE");
		} 
		catch (UnsupportedEncodingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return realSms;
	}
	
	//字节转化   1234  --> 0x12 0x 34
	public static byte[] get2ByteFromString(String str)
	{
		int len = str.length();
		byte[] bDest = new byte[len/2];
		byte[] bSrc = str.getBytes();
		
		for(int i=0,j=0; i<len;j++)
		{
			if(bSrc[i]>='0' && bSrc[i]<='9')
			{
				bDest[j] = (byte) (((byte)(bSrc[i]-'0')) << 4);
			}
			else
			{
				bDest[j] = (byte) (((byte)(bSrc[i]-'A'+10)) << 4);
			}
			
			i++;
			if(bSrc[i]>='0' && bSrc[i]<='9')
			{
				bDest[j] |= (byte) (bSrc[i]-'0');
			}
			else
			{
				bDest[j] |= (byte) (bSrc[i]-'A'+10);
			}
			i++;
		}
		
		return bDest;
	}	
		
}
