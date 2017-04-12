import java.rmi.server.*;
import java.rmi.*;

public class DiscuServer{
	// Bind DiscuServer and Registry
	public static void main(String args[])
	{
		//System.setSecurityManager(new RMISecurityManager());
		try
		{
			DiscuServerRMIImpl name = new DiscuServerRMIImpl();
			System.out.println("Registering ...");
			Naming.rebind("Discuss", name);	// Discuss is the name of the service
			System.out.println("RMI register success");
		}
		catch(Exception e)
		{
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
