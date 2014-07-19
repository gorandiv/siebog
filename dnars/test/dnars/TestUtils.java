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

package dnars;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import scala.collection.mutable.ListBuffer;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.gremlin.scala.ScalaGraph;
import dnars.base.Copula;
import dnars.base.Statement;
import dnars.base.StatementParser;
import dnars.events.Event;
import dnars.graph.DNarsGraph;

public class TestUtils
{
	public static Statement[] createAndAdd(DNarsGraph graph, String... statements)
	{
		ListBuffer<Event> events = new ListBuffer<>();
		Statement[] st = new Statement[statements.length];
		for (int i = 0; i < statements.length; i++)
		{
			st[i] = StatementParser.apply(statements[i]);
			graph.statements().add(st[i], events);
		}
		return st;
	}
	
	public static Statement[] create(String... statements) 
	{
		Statement[] st = new Statement[statements.length];
		for (int i = 0; i < statements.length; i++)
			st[i] = StatementParser.apply(statements[i]);
		return st;
	}
	
	public static void assertGraph(DNarsGraph graph, Statement[] kb, Statement[] res)
	{
		List<Statement> all = new ArrayList<>(kb.length + res.length);
		for (Statement st: kb)
			all.add(st);
		for (Statement st: res)
			all.add(st);
		
		Statement[] gr = graph.statements().getAll();
		try
		{
			assertEquals(all.size(), gr.length);
			
			for (Statement st: all)
			{
				boolean found = false;
				for (int i = 0; i < gr.length && !found; i++)
					if (st.equivalent(gr[i]))
						found = true;
				assertTrue("Statement " + st + " not found.", found);
			}
		} catch (AssertionError e)
		{
			System.out.println("Graph statements:");
			for (Statement st: gr)
				System.out.println(st);
			throw e;
		}
	}
	
	public static dnars.graph.DNarsGraph createGraph()
	{
		return DNarsGraph.wrap(ScalaGraph.wrap(new TinkerGraph()));
	}
	
	public static Statement invert(Statement st)
	{
		if (!st.copula().equals(Copula.Similar()))
			throw new IllegalArgumentException("Only similarity statements can be inverted.");
		return new Statement(st.pred(), Copula.Similar(), st.subj(), st.truth());
	}
}