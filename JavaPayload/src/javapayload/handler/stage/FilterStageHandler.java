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
package javapayload.handler.stage;

import javapayload.Parameter;

public abstract class FilterStageHandler extends StageHandler {
	
	public FilterStageHandler(String name, String summary) {
		super(name, true, true, summary);
	}
	
	public Parameter[] getParameters() {
		return new Parameter[] {
				new Parameter("STAGE", false, Parameter.TYPE_STAGE, "Stage to load")
		};
	}
	
	protected String[] realParameters = null;
	protected int realStageOffset;

	protected StageHandler findRealStageHandler(String[] parameters) throws Exception {
		String realStage = null;
		realParameters = (String[]) parameters.clone();
		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i].equals("--")) {
				realParameters[i] = "";
				realParameters[i + 1] = "--";
				realStageOffset = i + 2;
				realStage = parameters[i + 2];
				break;
			}
		}
		if (realStage == null)
			throw new IllegalArgumentException("Cannot find stage");
		StageHandler realStageHandler = (StageHandler) Class.forName("javapayload.handler.stage." + realStage).newInstance();
		realStageHandler.consoleIn = consoleIn;
		realStageHandler.consoleOut = consoleOut;
		realStageHandler.consoleErr = consoleErr;
		return realStageHandler;
	}
}
