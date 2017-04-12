import java.rmi.Remote;

public interface DiscuInterface extends Remote{
  public String log(String account,String password) throws java.rmi.RemoteException;
  public boolean register(String account,String password) throws java.rmi.RemoteException;
  public String create(String account,String topic,String content) throws java.rmi.RemoteException;
  public String subject() throws java.rmi.RemoteException;
  public String reply(String account,int select,String content) throws java.rmi.RemoteException;
  public String discussion(int select) throws java.rmi.RemoteException;
  public String delete(String account,int select) throws java.rmi.RemoteException;
}
