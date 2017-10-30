package org.frameworkset.elasticsearch.client;

public abstract class ClientUtil implements ClientInterface{

	/**
	 * 处理lucene特殊字符
	 * @param condition
	 * @return
	 */
	public static String handleLuceneSpecialChars(String condition){
		if(condition == null || condition.equals("")){
			return condition;
		}
		condition = condition.replace(":","/:");
		condition = condition.replace("-","/-");
		condition = condition.replace("+","/+");
		condition = condition.replace("&","/&");
		condition = condition.replace("!","/!");
		condition = condition.replace("{","/{");
		condition = condition.replace("}","/}");
		condition = condition.replace("(","/(");
		condition = condition.replace(")","/)");
		condition = condition.replace("|","/|");

		condition = condition.replace("~","/~");
		condition = condition.replace("*","/*");
		condition = condition.replace("?","/?");
		condition = condition.replace("/","//");
//		condition = condition.replace("\"","/\"");

		return condition;
	}

	/**
	 * 处理es特殊字符
	 * @param condition
	 * @return
	 */
	public static String handleElasticSearchSpecialChars(String condition){
		if(condition == null || condition.equals("")){
			return condition;
		}
		condition = condition.replace(":","\\\\:");
		condition = condition.replace("-","\\\\-");
		condition = condition.replace("+","\\\\+");
		condition = condition.replace("&","\\\\&");
		condition = condition.replace("!","\\\\!");
		condition = condition.replace("{","\\\\{");
		condition = condition.replace("}","\\\\}");
		condition = condition.replace("(","\\\\(");
		condition = condition.replace(")","\\\\)");
		condition = condition.replace("|","\\\\|");

		condition = condition.replace("~","\\\\~");
		condition = condition.replace("*","\\\\*");
		condition = condition.replace("?","\\\\?");
		condition = condition.replace("/","\\\\/");


		return condition;
	}

}
