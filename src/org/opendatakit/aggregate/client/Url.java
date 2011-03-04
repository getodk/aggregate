package org.opendatakit.aggregate.client;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.Window;

public class Url {
	private Map<String, List<String>> parameters;
	private String protocol;
	private String host;
	private String path;
	
	public Url() {
		protocol = Window.Location.getProtocol();
		host = Window.Location.getHost();
		path = Window.Location.getPath();
		parameters = new HashMap<String, List<String>>();
		Map<String, List<String>> p = Window.Location.getParameterMap();
		for (String s : p.keySet()) {
			parameters.put(s, p.get(s));
		}
	}
	
	public boolean contains(String parameter) {
		return parameters.containsKey(parameter);
	}
	
	public boolean contains(String parameter, String value) {
		if (!parameters.containsKey(parameter))
			return false;
		if (!parameters.get(parameter).contains(value))
			return false;
		return true;
	}
	
	public String get(String parameter) {
		if (!parameters.containsKey(parameter))
			return null;
		return parameters.get(parameter).get(0);
	}
	
	public List<String> getAll(String parameter) {
		return parameters.get(parameter);
	}
	
	public void set(String parameter, String value) {
		List<String> l = new LinkedList<String>();
		l.add(value);
		parameters.put(parameter, l);
	}
	
	public void add(String parameter, String value) {
		if (!parameters.containsKey(parameter)) {
			set(parameter, value);
			return;
		}
		parameters.get(parameter).add(value);
	}
	
	public void remove(String parameter) {
		parameters.remove(parameter);
	}
	
	public void remove(String parameter, String value) {
		if (!parameters.containsKey(parameter))
			return;
		List<String> l = parameters.get(parameter);
		int index = l.indexOf(value);
		while (index != -1) {
			l.remove(index);
			index = l.indexOf(value);
		}
		if (l.isEmpty())
			parameters.remove(parameter);
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer(protocol + "//" + host + path + "?");
		for (String p : parameters.keySet()) {
			for (String v : parameters.get(p)) {
				sb.append(p + "=" + v + "&");
			}
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}
	
	public void goTo() {
		goToUrl(toString());
	}
	
	private native void goToUrl(String url) /*-{
		$wnd.location.href = url;
	}-*/;
}
