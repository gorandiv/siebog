package org.xjaf2x.server.agents.aco.tsp;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import org.jboss.ejb3.annotation.Clustered;
import org.xjaf2x.server.agentmanager.agent.AgentAdapter;
import org.xjaf2x.server.agentmanager.agent.AgentI;
import org.xjaf2x.server.messagemanager.fipaacl.ACLMessage;

@Stateless(name = "org_xjaf2x_server_agents_aco_tsp_StarterAgent")
@Remote(AgentI.class)
@Clustered
public class StarterAgent extends AgentAdapter
{
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	@Override
	public void onMessage(ACLMessage message)
	{
		String content = (String) message.getContent();
		String[] parts = content.split(" ");
		int numAnts = Integer.parseInt(parts[1]);
		final String mapName = parts[2];
		
		/*AID mapAid = facilitator.runAgent("Map", "org_xjaf2x_aco_tsp_MapAgent");
		ACLMessage msg = new ACLMessage(Performative.INFORM);
		msg.addReceiver(mapAid);
		msg.setContent("Initialize " + mapName);
		post(msg);*/
	}

}
