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

package siebog.server.xjaf.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.infinispan.Cache;
import siebog.server.xjaf.Global;
import siebog.server.xjaf.agents.base.AID;
import siebog.server.xjaf.agents.base.AgentClass;
import siebog.server.xjaf.agents.base.AgentI;

/**
 * Default agent manager implementation.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 * @author <a href="tntvteod@neobee.net">Teodor-Najdan Trifunov</a>
 * @author <a href="rade.milovanovic@hotmail.com">Rade Milovanovic</a>
 */
@Stateless
@Remote(AgentManagerI.class)
@Path("/agents")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@LocalBean
public class AgentManager implements AgentManagerI
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(AgentManager.class.getName());
	private Context jndiContext;
	private Cache<AID, AgentI> runningAgents;

	@PostConstruct
	public void postConstruct()
	{
		try
		{
			jndiContext = Global.getContext();
			runningAgents = Global.getRunningAgents();
		} catch (Exception ex)
		{
			logger.log(Level.SEVERE, "AgentManager initialization error.", ex);
		}
	}

	@PUT
	@Path("/running/{agClass}/{name}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Override
	public AID start(@PathParam("agClass") AgentClass agClass, @PathParam("name") String name,
			Map<String, String> args)
	{
		AID aid = new AID(name);
		// is it running already?
		AgentI agent = runningAgents.get(aid);
		if (agent != null)
		{
			logger.info("Already running: [" + aid + "]");
			return aid;
		}

		createNew(agClass, aid, args);
		logger.fine("Agent [" + aid + "] started.");
		return aid;
	}

	/**
	 * Terminates an active agent.
	 * 
	 * @param aid AID object.
	 */
	@DELETE
	@Path("/running/{aid}")
	@Override
	public void stop(@PathParam("aid") AID aid)
	{
		AgentI agent = runningAgents.get(aid);
		if (agent != null)
		{
			// TODO : implement this
			// runningAgents.remove(aid);
			// agent.terminate();
		}
	}

	private void createNew(AgentClass agClass, AID aid, Map<String, String> args)
	{
		try
		{
			// build the JNDI lookup string
			final String view = AgentI.class.getName();
			String jndiNameStateless = String.format("ejb:/%s//%s!%s", agClass.getModule(),
					agClass.getEjbName(), view);
			String jndiNameStateful = jndiNameStateless + "?stateful";

			AgentI agent = null;
			try
			{
				agent = (AgentI) jndiContext.lookup(jndiNameStateful);
			} catch (NamingException ex)
			{
				final Throwable cause = ex.getCause();
				if (cause == null || !(cause instanceof IllegalStateException))
					throw ex;
				agent = (AgentI) jndiContext.lookup(jndiNameStateless);
			}

			// the order of the next two statements matters. if we call init first and the agent
			// sends a message from there, it sometimes happens that the reply arrives before we
			// register the AID. also some agents might wish to terminate themselves inside init.
			runningAgents.put(aid, agent);
			agent.init(aid, args);
		} catch (Exception ex)
		{
			logger.log(Level.INFO, "Error while creating [" + aid + "]", ex);
			throw new IllegalArgumentException("Cannot create an agent of class " + agClass, ex);
		}
	}

	@GET
	@Path("/deployed")
	@Override
	public List<AgentClass> getDeployed()
	{
		List<AgentClass> result = new ArrayList<>();
		final String intf = "!" + AgentI.class.getName();
		final String exp = "java:jboss/exported/";
		try
		{
			NamingEnumeration<NameClassPair> moduleList = jndiContext.list(exp);
			while (moduleList.hasMore())
			{
				String module = moduleList.next().getName();
				NamingEnumeration<NameClassPair> agentList = jndiContext.list(exp + "/" + module);
				while (agentList.hasMore())
				{
					String ejbName = agentList.next().getName();
					if (ejbName != null && ejbName.endsWith(intf))
					{
						int n = ejbName.lastIndexOf(intf);
						ejbName = ejbName.substring(0, n);
						AgentClass agClass = new AgentClass(module, ejbName);
						result.add(agClass);
					}
				}
			}
		} catch (NamingException ex)
		{
			logger.log(Level.WARNING, "Error while loading deployed agents.", ex);
		}
		return result;
	}

	@GET
	@Path("/running")
	@Override
	public List<AID> getRunning()
	{
		final Set<AID> keys = runningAgents.keySet();
		List<AID> aids = new ArrayList<>(keys.size());
		aids.addAll(keys);
		return aids;
	}
	
	@Override
	public AID getAIDByName(String name)
	{
		List<AID> running = getRunning();
		for (AID aid: running)
			if (aid.getName().equals(name))
				return aid;
		return null;
	}
}