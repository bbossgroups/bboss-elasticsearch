package org.frameworkset.elasticsearch.client;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * Copyright 2014 Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class RoundRobinList<T extends ESAddress> {

	private final Collection<T> elements;

	private Iterator<T> iterator;

	public RoundRobinList(Collection<T> elements) {
		this.elements = elements;
		iterator = this.elements.iterator();
	}

//	public synchronized T get() {
//		T address = null;
//		while (iterator.hasNext()) {
//			address = iterator.next();
//			if (address.ok())
//				break;
//		}
//		if (address != null) {
//			return address;
//
//		}
//		else {
//			iterator = elements.iterator();
//			while (iterator.hasNext()) {
//				address = iterator.next();
//				if (address.ok())
//					break;
//			}
//			return address;
//		}
//
//	}
	private Lock lock = new ReentrantLock();
	public T get(){
		try {
			lock.lock();
			T address = null;
			while (iterator.hasNext()) {
				address = iterator.next();
				if (address.ok())
					break;
			}
			if (address != null) {
				return address;

			} else {
				iterator = elements.iterator();
				while (iterator.hasNext()) {
					address = iterator.next();
					if (address.ok())
						break;
				}
				return address;
			}
		}
		finally {
			lock.unlock();
		}
	}

	public int size() {
		return elements.size();
	}
}
