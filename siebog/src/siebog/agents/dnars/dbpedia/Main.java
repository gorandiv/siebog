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

package siebog.agents.dnars.dbpedia;

import siebog.SiebogClient;
import siebog.core.Global;
import siebog.utils.ObjectFactory;
import siebog.xjaf.core.AID;
import siebog.xjaf.core.AgentClass;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.fipa.Performative;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class Main {
	public static void main(String[] args) {
		SiebogClient.connect("localhost");

		AgentClass agClass = new AgentClass(Global.SERVER, Resolver.class.getSimpleName());
		AID aid = ObjectFactory.getAgentManager().startAgent(agClass, "Resolver_" + System.currentTimeMillis(), null);

		ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.sender = AID.EXTERNAL_CLIENT;
		msg.receivers.add(aid);
		msg.content = "http://dbpedia.org/resource/Albert_Einstein";
		ObjectFactory.getMessageManager().post(msg);
	}
}
