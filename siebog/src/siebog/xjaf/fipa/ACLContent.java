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

package siebog.xjaf.fipa;

import java.io.Serializable;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class ACLContent implements Serializable {
	private static final long serialVersionUID = 1L;
	private String content;

	public ACLContent(String content) {
		this.content = content;
	}

	@Override
	public int hashCode() {
		return content.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ACLContent other = (ACLContent) obj;
		if (content == null)
			return other.content == null;
		return content.equals(other.content);
	}

	public String toString() {
		return content;
	}
}
