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

package org.xjaf2x.server.agents.ping;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import org.jboss.ejb3.annotation.Clustered;
import org.xjaf2x.server.agentmanager.agent.Agent;
import org.xjaf2x.server.agentmanager.agent.AgentI;
import org.xjaf2x.server.messagemanager.fipaacl.ACLMessage;
import org.xjaf2x.server.messagemanager.fipaacl.Performative;

/**
 * Example of a pong agent. 
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful(name = "org_xjaf2x_server_agents_ping_PongAgent")
@Remote(AgentI.class)
@Clustered
public class PongAgent extends Agent
{
	private static final long serialVersionUID = 1L;
	private int number = 0;
	
	@Override
	protected void onMessage(ACLMessage msg)
	{
		logger.info("Pong @ [" + System.getProperty("jboss.node.name") + "]");
		
		ACLMessage reply = msg.makeReply(Performative.INFORM);
		reply.setContent(number++);
		msgMngr().post(reply);
	}
}
