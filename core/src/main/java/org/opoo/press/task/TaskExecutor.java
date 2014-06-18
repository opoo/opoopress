/*
 * Copyright 2014 Alex Lin.
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
package org.opoo.press.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.opoo.press.impl.SiteConfigImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alex Lin
 *
 */
public class TaskExecutor {
	/**
	 * Default thread count.
	 */
	public static final int DEFAULT_THREADS = 1;

	private static final Logger log = LoggerFactory.getLogger(TaskExecutor.class);
	private ExecutorService executorService;
	
	public TaskExecutor(SiteConfigImpl config) {
		//thread count
		int threadsCount = Integer.parseInt(System.getProperty("threads", "-1"));
		if(threadsCount <= 0){
			threadsCount = config.get("threads", DEFAULT_THREADS);
		}
		if(threadsCount <= 0){
			threadsCount = DEFAULT_THREADS;
		}
		if(threadsCount > 1){
			executorService = Executors.newFixedThreadPool(threadsCount);
			log.info("Executing build in threads: " + threadsCount);
		}else{
			log.info("Executing build in single thread.");
		}
	}
	
	public void run(Runnable task){
		if(executorService == null){
			task.run();
			return;
		}
		
		Future<?> future = executorService.submit(task);
		try {
			future.get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public <V> V call(Callable<V> task) throws Exception{
		if(executorService == null){
			return task.call();
		}
		
		Future<V> future = executorService.submit(task);
		return future.get();
	}
	
	public <T> void run(Collection<T> list, final RunnableTask<T> task){
		if(executorService == null){
			for(T t: list){
				task.run(t);
			}
			return;
		}
		
		int size = list.size();
		CompletionService<Integer> cs = new ExecutorCompletionService<Integer>(executorService);
		for(final T input:list){
			cs.submit(new Runnable(){
				public void run() {
					task.run(input);
				}}, 1);
		}
		
        try {
			for(int i = 0 ; i < size ; i++){
				cs.take().get();
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
	
	public <T,V> List<V> call(Collection<T> list, final CallableTask<T,V> task){
		List<V> result = new ArrayList<V>();
		if(executorService == null){
			for(T t: list){
				result.add(task.call(t));
			}
			return result;
		}
		
		
		int size = list.size();
		CompletionService<V> cs = new ExecutorCompletionService<V>(executorService);
		for(final T input:list){
			cs.submit(new Callable<V>(){
				public V call() throws Exception {
					return task.call(input);
				}});
		}
		
        try {
			for(int i = 0 ; i < size ; i++){
				result.add(cs.take().get());
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
        return result;
	}
	
	public void run(Runnable... tasks){
		List<Runnable> taskList = new ArrayList<Runnable>();
		for(Runnable task: tasks){
			taskList.add(task);
		}
		run(taskList);
	}
	
	public void run(List<Runnable> tasks){
		if(executorService == null){
			for(Runnable t: tasks){
				t.run();
			}
			return;
		}

		int size = tasks.size();
		CompletionService<Integer> cs = new ExecutorCompletionService<Integer>(executorService);
		for(final Runnable task : tasks){
			cs.submit(task, 1);
		}
		
        try {
			for(int i = 0 ; i < size ; i++){
				cs.take().get();
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
}
