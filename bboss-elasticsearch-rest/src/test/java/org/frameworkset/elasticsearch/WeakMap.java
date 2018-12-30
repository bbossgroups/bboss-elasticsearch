package org.frameworkset.elasticsearch;
/**
 * Copyright 2008 biaoping.yin
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/12/27 23:18
 * @author biaoping.yin
 * @version 1.0
 */
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

public class WeakMap {
	// 这个是为了方便测试HashMap与WeakHashMap的区别。
	//private static HashMap<Object,String> weakHashMap = new HashMap<>();
	private static WeakHashMap<Object,String> weakHashMap = new WeakHashMap<Object,String>();

	/**
	 * main方法测试
	 * @param s
	 * @throws IOException
	 */
	public static void main(String[] s) throws IOException {
// 创建key为obj的对象
		Object obj = new Object();
		// 保存对象，并null out对象
		test(obj);

		try {
			// null out the obj reference。
           	obj = null;
			// 虽然obj被null了，但GC还没来得及回收，所以，size还是为1
			System.out.println("size2="+weakHashMap.size());
			// 通知GC回收
			System.gc();
			/**
			 * 如果回收后，size还是1，那请在这里加个延时。
			 * 我的电脑，不需要延时，gc后，size就为0了，也就是obj被回收了
			 **/
			 Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}


		// 回收完，size为0
		System.out.println("size3="+weakHashMap.size());
		// 打印结果
		Iterator j = weakHashMap.entrySet().iterator();
		while (j.hasNext()) {
			Map.Entry en = (Map.Entry)j.next();
			System.out.println("weakmap2:"+en.getKey()+":"+en.getValue());

		}


	}

	private static void test(Object obj) {



		// 以这个obj对象为key,保存value
		weakHashMap.put(obj, "这是结果");

		// 打印结果
		Iterator j = weakHashMap.entrySet().iterator();

		while (j.hasNext()) {
			Map.Entry en = (Map.Entry)j.next();
			System.out.println("weakmap:"+en.getKey()+":"+en.getValue());

		}
		// 打印map的大小，刚保存进去，所以 size为1
		System.out.println("size1="+weakHashMap.size());

	}

}
