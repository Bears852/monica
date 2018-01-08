/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package monica.files.socket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import monica.configuration.context.ConfigurationContext;
import monica.framework.Storage;
import monica.framework.storage.AbstractStorageFactory;
import monica.framework.transport.TransportFile;
import io.netty.channel.ChannelHandler.Sharable;


/**
 * 
 * @author lucy@polarcoral.com
 *
 *         2017-08-29
 */
@Sharable
public class FileServerHandler extends SimpleChannelInboundHandler<Object> {
	private String STORAGE_CONFIG_KEY = "storage";
    private final int poolSize=20;

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		  ExecutorService storageService = Executors.newFixedThreadPool(poolSize);	 		
		  String storageFactoryName = String.valueOf(ConfigurationContext.propMap.get(STORAGE_CONFIG_KEY));		   
		  Object o = this.getClass().getClassLoader().loadClass(storageFactoryName).newInstance();
		  Storage storage = ((AbstractStorageFactory)o).newStorageInstance();		
		  storageService.execute(new Runnable(){
				@Override
				public void run() {
					storage.storage((TransportFile)msg);				
				}				  
			  });		  
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		if (ctx.channel().isActive()) {
			ctx.writeAndFlush("ERR: " + cause.getClass().getSimpleName() + ": " + cause.getMessage() + '\n')
					.addListener(ChannelFutureListener.CLOSE);
		}
	}
}
