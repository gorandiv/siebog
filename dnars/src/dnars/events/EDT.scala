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

package dnars.events

import java.util.logging.Level
import java.util.logging.Logger

import scala.collection.mutable.ListBuffer

/**
 * Implementation of the Event Dispatch Thread.
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class EDT(val pendingEvents: ListBuffer[EventPayload], val observers: ListBuffer[EventObserver]) extends Thread {
	val logger = Logger.getLogger(classOf[EDT].getName)

	override def run(): Unit = {
		while (!Thread.interrupted) {
			processEvents()
		}
	}

	private def processEvents(): Unit = {
		try {
			waitAndDispatch()
		} catch {
			case _: InterruptedException =>
				Thread.currentThread.interrupt()
			case ex: Exception =>
				logger.log(Level.WARNING, "Exception in EDT.", ex)
		}
	}

	private def waitAndDispatch(): Unit = {
		var eventsToDispatch: Array[EventPayload] = null
		pendingEvents synchronized {
			waitForEvents()
			eventsToDispatch = cloneOfPendingEvents
		}
		dispatch(eventsToDispatch)
	}

	private def waitForEvents(): Unit = {
		while (pendingEvents.length == 0)
			pendingEvents.wait
	}

	private def cloneOfPendingEvents(): Array[EventPayload] = {
		val copy = pendingEvents.toBuffer
		pendingEvents.clear()
		copy.toArray
	}

	private def dispatch(events: Array[EventPayload]): Unit = {
		observers synchronized {
			observers.foreach { _.onEvents(events) }
		}
	}
}