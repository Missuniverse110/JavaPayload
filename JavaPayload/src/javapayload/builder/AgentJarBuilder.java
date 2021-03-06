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

package javapayload.builder;

import java.util.jar.Manifest;

public class AgentJarBuilder extends Builder {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println("Usage: java javapayload.builder.AgentJarBuilder " + JarBuilder.ARGS_SYNTAX);
			return;
		}
		new AgentJarBuilder().build(args);
	}
	
	public AgentJarBuilder() {
		super("Build a debug agent Jar file", 
				"Build a debug agent Jar file, either to use it manually with the -javaagent\r\n" +
				"JVM option, or with AttachInjector.");
	}
	
	public String getParameterSyntax() {
		return JarBuilder.ARGS_SYNTAX;
	}
		
	public void build(String[] args) throws Exception {
		final Class[] baseClasses = new Class[] {
				javapayload.loader.AgentJarLoader.class,
				javapayload.stager.Stager.class,
		};
		final Manifest manifest = new Manifest();
		manifest.getMainAttributes().putValue("Agent-Class", "javapayload.loader.AgentJarLoader");
		manifest.getMainAttributes().putValue("Premain-Class", "javapayload.loader.AgentJarLoader");
		JarBuilder.buildJarFromArgs(args, "Agent", baseClasses, manifest, null, null);
	}
}
