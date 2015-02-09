package autonavi.online.framework.support.zookeeper.des;

import sun.misc.BASE64Decoder;



public class DesUtils {
	
	
	public static String desEncode(String org)
	{
		return 	BASE64Encoder.encode(Cryptor.RC4Crypt(org.getBytes()));
	}

	public static String desDecode(String org,String set )
	{
		
		String result="";
		try {
			byte[] b=new BASE64Decoder().decodeBuffer(org);
			result=new String(Cryptor.RC4Crypt(b),set);
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
		return 	result;
	}
	public static String desDecode(String org)
	{
		
		String result="";
		try {
			byte[] b=new BASE64Decoder().decodeBuffer(org);
			result=new String(Cryptor.RC4Crypt(b),"utf-8");
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
		return 	result;
	}

}
