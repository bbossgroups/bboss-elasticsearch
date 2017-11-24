package org.frameworkset.elasticsearch.client;

public abstract class ClientUtil implements ClientInterface{
	public static  Long longValue(Object num,Long defaultValue){
		if(num == null)
			return defaultValue;
		if(num instanceof Long)
		{
			return ((Long)num);
		}else if(num instanceof Double)
		{
			return ((Double)num).longValue();
		}else if(num instanceof Integer){
			return ((Integer)num).longValue();
		}
		else if(num instanceof Float)
		{
			return ((Float)num).longValue();
		}
		else  if(num instanceof Short)
		{
			return ((Short)num).longValue();
		}
		else
		{
			return Long.parseLong(num.toString());
		}
	}

	public static  Integer intValue(Object num,Integer defaultValue){
		if(num == null)
			return defaultValue;
		if(num instanceof Integer)
		{
			return ((Integer)num);
		}
		else if(num instanceof Long)
		{
			return ((Long)num).intValue();
		}else if(num instanceof Double)
		{
			return ((Double)num).intValue();
		}
		else if(num instanceof Float)
		{
			return ((Float)num).intValue();
		}
		else  if(num instanceof Short)
		{
			return ((Short)num).intValue();
		}
		else
		{
			return Integer.parseInt(num.toString());
		}
	}

	public static  Float floatValue(Object num,Float defaultValue){
		if(num == null)
			return defaultValue;
		if(num instanceof Float)
		{
			return (Float)num;
		}else if(num instanceof Double)
		{
			return ((Double)num).floatValue();
		}else if(num instanceof Integer){
			return ((Integer)num).floatValue();
		}
		else  if(num instanceof Long)
		{
			return ((Long)num).floatValue();
		}
		else  if(num instanceof Short)
		{
			return ((Short)num).floatValue();
		}
		else
		{
			return Float.parseFloat(num.toString());
		}
	}

	public static  Double doubleValue(Object num,Double defaultValue){
		if(num == null)
			return defaultValue;
		if(num instanceof Double)
		{
			return (Double)num;
		}else if(num instanceof Float)
		{
			return ((Float)num).doubleValue();
		}else if(num instanceof Integer){
			return ((Integer)num).doubleValue();
		}
		else  if(num instanceof Long)
		{
			return ((Long)num).doubleValue();
		}
		else  if(num instanceof Short)
		{
			return ((Short)num).doubleValue();
		}
		else
		{

			return Double.parseDouble(num.toString());
		}
	}
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
		condition = condition.replace(":","\\:");
		condition = condition.replace("-","\\-");
		condition = condition.replace("+","\\+");
		condition = condition.replace("&","\\&");
		condition = condition.replace("!","\\!");
		condition = condition.replace("{","\\{");
		condition = condition.replace("}","\\}");
		condition = condition.replace("(","\\(");
		condition = condition.replace(")","\\)");
		condition = condition.replace("|","\\|");

		condition = condition.replace("~","\\~");
		condition = condition.replace("*","\\*");
		condition = condition.replace("?","\\?");
		condition = condition.replace("/","\\/");


		return condition;
	}

}
