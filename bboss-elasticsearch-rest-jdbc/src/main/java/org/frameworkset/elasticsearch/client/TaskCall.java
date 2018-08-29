package org.frameworkset.elasticsearch.client;
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
 * @Date 2018/8/29 21:27
 * @author biaoping.yin
 * @version 1.0
 */
public class TaskCall implements Runnable {
	private String refreshOption;
	private ClientInterface clientInterface;
	private String datas;
	public TaskCall(String refreshOption,ClientInterface clientInterface,String datas){
		this.refreshOption = refreshOption;
		this.clientInterface = clientInterface;
		this.datas = datas;
	}


	@Override
	public void run()   {
		if (refreshOption == null)
			clientInterface.executeHttp("_bulk", datas, ClientUtil.HTTP_POST);
		else
			clientInterface.executeHttp("_bulk?" + refreshOption, datas, ClientUtil.HTTP_POST);

	}
}
