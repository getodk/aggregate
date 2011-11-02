package org.opendatakit.aggregate.externalservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.Callable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

/**
 * {@link #call()} starts a server listening on the port passed to the
 * constructor which can be used as the destination for Aggregate's Ohmage Json
 * Server.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class OhmageJsonServerListener extends AbstractHandler implements
		Callable<Void> {

	private int port;

	public OhmageJsonServerListener(int port) {
		this.port = port;
	}

	@Override
	public void handle(String target, HttpServletRequest request,
			HttpServletResponse response, int dispatch) throws IOException,
			ServletException {
		BufferedReader reader = request.getReader();
		StringBuffer json = new StringBuffer();
		while (reader.ready()) {
			json.append(reader.readLine());
		}
		System.out.println(json.toString());

		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println("{\"result\": \"success\"}");

		Request baseRequest = (request instanceof Request) ? (Request) request
				: HttpConnection.getCurrentConnection().getRequest();
		baseRequest.setHandled(true);
	}

	@Override
	public Void call() throws Exception {
		Server server = new Server(port);
		server.setHandler(this);

		server.start();
		server.join();
		return null;
	}

	public static void main(String[] args) throws Exception {
		OhmageJsonServerListener listener = new OhmageJsonServerListener(80);
		listener.call();
	}
}
