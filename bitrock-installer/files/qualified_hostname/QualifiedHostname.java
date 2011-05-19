
public class QualifiedHostname
{
    public static void main(String[] args)
    {
		String name = "";
		try {
			name = java.net.InetAddress.getLocalHost().getCanonicalHostName();
		} catch ( java.net.UnknownHostException e ) {
		}
        System.out.println(name);
    }
}
