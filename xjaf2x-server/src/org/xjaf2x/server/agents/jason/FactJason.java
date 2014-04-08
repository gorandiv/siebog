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

package org.xjaf2x.server.agents.jason;

import java.util.List;
import java.util.Map;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import jason.asSyntax.Literal;
import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.jboss.ejb3.annotation.Clustered;
import org.xjaf2x.server.JndiManager;
import org.xjaf2x.server.agentmanager.agent.AID;
import org.xjaf2x.server.agentmanager.agent.Agent;
import org.xjaf2x.server.agentmanager.agent.jason.JasonAgentI;
import org.xjaf2x.server.messagemanager.fipaacl.ACLMessage;

/**
 * Work in progress.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful(name = "org_xjaf2x_server_agents_jason_FactJason")
@Remote(JasonAgentI.class)
@Clustered
public class FactJason extends Agent implements JasonAgentI
{
	private static final long serialVersionUID = 1L;
	private int numAgents;
	private long startTime;

	@Override
	public void init(Map<String, Object> args) throws Exception
	{
		// get rid of trailing "s
		String str = (String) args.get("numAgents");
		str = str.substring(1, str.length() - 1);
		numAgents = Integer.parseInt(str);
	}

	@Override
	@Remove
	public void terminate()
	{
	}

	@Override
	public void onMessage(ACLMessage message)
	{
	}

	@Override
	public List<Literal> perceive()
	{
		return null;
	}

	@Override
	public boolean act(String functor)
	{
		if (functor.equals("start"))
			startTime = System.nanoTime();
		else
		{
			long total = (System.nanoTime() - startTime) / 1000000;
			try
			{
				CacheContainer container = (CacheContainer) JndiManager.getContext().lookup(
						"java:jboss/infinispan/container/xjaf2x-cache");
				Cache<AID, Long> cache = container.getCache("factJason");
				cache.put(getAid(), total);
				if (cache.size() == numAgents)
				{
					long sum = 0;
					for (Long tm : cache.values())
						sum += tm;
					System.out.println("Average time: " + (sum / numAgents) + " ms");
					cache.clear();
				}
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return true;
	}
}
