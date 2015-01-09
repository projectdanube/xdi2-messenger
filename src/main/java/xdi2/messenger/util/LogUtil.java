package xdi2.messenger.util;

public class LogUtil {

	
	public static String prepareToLog(String logMessage) {
		return logMessage.replaceAll("<\\$secret><\\$token>&/&/\".*\"", "<\\$secret><\\$token>&/&/\"\\*\\*\\*\\*\\*\\*\\*\"");
	}
	
	public static void main(String[] args) {
		
		String test = "dsfasdfasfsdfsdfsdf\n" + 
				"[=]!:uuid:50f47072-e6a8-4c5c-ac18-499035ab46fe[$msg]!:uuid:0f71a24c-7ff8-4b12-8158-f1c85552c956<$secret><$token>&/&/\"teste12..\"\n" + 
				"[=]!:uuid:50f47072-e6a8-4c5c-ac18-499035ab46fe[$msg]!:uuid:0f71a24c-7ff8-4b12-8158-f1c85552c956$do/$get/[=]!:uuid:50f47072-e6a8-4c5c-ac18-499035ab46fe[$messages]\n" +
				"[=]!:uuid:50f47072-e6a8-4c5c-ac18-499035ab46fe[$msg]!:uuid:0f71a24c-7ff8-4b12-8158-f1c85552c956/$is()/([=]!:uuid:50f47072-e6a8-4c5c-ac18-499035ab46fe)";
		
		
		System.out.println(prepareToLog(test));
	}
	
}
