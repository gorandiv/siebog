/**
 * Licensed to the Apache Software Foundation (ASF) under one 
 * or more contributor license agreements. See the NOTICE file 
 * distributed with this work for additional information regarding 
 * copyright ownership. The ASF licenses this file to you under 
 * the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may 
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. 
 * 
 * See the License for the specific language governing permissions 
 * and limitations under the License.
 */

package org.xjaf2x.server.messagemanager;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.Context;
import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.jboss.ejb3.annotation.Clustered;
import org.xjaf2x.server.JndiManager;
import org.xjaf2x.server.agentmanager.agent.AID;
import org.xjaf2x.server.agentmanager.agent.AgentI;
import org.xjaf2x.server.messagemanager.fipaacl.ACLMessage;

/**
 * Default message manager implementation.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateless
@Remote(MessageManagerI.class)
@Clustered
public class MessageManager implements MessageManagerI
{
	private static final Logger logger;
	private static final ThreadPoolExecutor executor;
	private Cache<AID, AgentI> runningAgents;
	
	static
	{
		logger = Logger.getLogger(MessageManager.class.getName());
		executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		executor.setMaximumPoolSize(16);
	}
	
	@PostConstruct
	public void postConstruct()
	{
		try
		{	
			Context jndiContext = JndiManager.getContext();
			CacheContainer container = (CacheContainer) jndiContext.lookup("java:jboss/infinispan/container/xjaf2x-cache");
			runningAgents = container.getCache("running-agents");
		} catch (Exception ex)
		{
			logger.log(Level.SEVERE, "MessageManager initialization error", ex);
		}
	}

	@Override
	public void post(final ACLMessage message)
	{
		final boolean info = logger.isLoggable(Level.INFO);
		for (AID aid : message.getReceivers())
		{
			if (aid == null)
				continue;
			AgentI agent = runningAgents.get(aid);
			if (agent != null)
				agent.handleMessage(message);
			else if (info)
				logger.info("Agent not running: [" + aid + "]");
		}
	}
}
