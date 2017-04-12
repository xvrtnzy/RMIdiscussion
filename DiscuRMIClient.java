import java.util.Scanner;
import java.io.*;
import java.rmi.*;

class DiscuRMIClient{
  public static void main (String args[]){
    int                   op;
    boolean               exitFlag = false;
    String                name = null;
    String                password,topic,content,/*retopic,*/recontent,recv;
    Scanner               keyboard = new Scanner(System.in);
    DiscuInterface	     	o = null;

    if(args.length == 0){
		    System.out.println("Usage: java DiscuRMIClient server_ip");
		}else{
        String      ip   =   args[0];
        // Connect to RMIServer
        try
        {
          o = (DiscuInterface) Naming.lookup("rmi://" + ip + "/Discuss");
          System.out.println("RMI server connected");
        }catch(Exception e){
          System.out.println("Server lookup exception: " + e.getMessage());
        }
        //menu  &  function
        try{
          while(!exitFlag){
            System.out.println("歡迎使用討論區程式,請選擇你想要的選項:");
            System.out.println("[0] 登入");
            System.out.println("[1] 註冊");
            System.out.println("[2] 新增討論主題");
            System.out.println("[3] 列出討論主題名稱");
            System.out.println("[4] 回覆討論議題");
            System.out.println("[5] 顯示一討論主題的內容及其所有回覆內容");
            System.out.println("[6] 刪除討論主題");
            System.out.println("[7] Exit");

            op = keyboard.nextInt();
            switch(op){
              case 0:
                System.out.println("LogIN");
                System.out.println("Pls type your account:");
                name = keyboard.next();
                System.out.println("Pls type your password:");
                password = keyboard.next();
                recv = o.log(name,password);
                System.out.println(recv);
                if(recv.charAt(0) == '-'){
                  name = null;  password = null;
                }
                recv = "";
                break;
              case 1:
                System.out.println("Registry");
                System.out.println("請輸入帳號 :");
                name = keyboard.next();
                System.out.println("請輸入密碼 :");
                password = keyboard.next();
                if(o.register(name,password)){
                  System.out.println("Register suecessed!"); //True
                  System.out.println("Welcome~ "+ name +" !!");
                }else{
                  name = null;
                  System.out.println("The account has existed,try a new one."); //False
                }
                break;
              case 2:
                //(a) 討論主題名稱 及 (b) 討論主題內容
                if(name != null){
                  System.out.println("新增討論主題");
                  System.out.println("(a)請輸入討論主題 :");
                  while( (topic = keyboard.nextLine()).equals("") );
                  System.out.println("(b)請輸入討論內容 :");
                  while( (content = keyboard.nextLine()).equals("") );
                  recv = o.create(name,topic,content);
                  System.out.println(recv);   recv = "";
                }else{
                  System.out.println("You need to Login or Registry first.");
                }
                break;
              case 3:
                if(name != null){
                  System.out.println("列出討論主題名稱");
                  System.out.println(o.subject());
                }else{
                  System.out.println("You need to Login or Registry first.");
                }
                break;
              case 4:
                if(name != null){
                  int  select;
                  System.out.println("回覆討論主題");
                  System.out.println(o.subject());
                  System.out.println("(a)請輸入欲回覆的討論主題 :<號碼>");
                  select = keyboard.nextInt();
                  System.out.println("(b)請輸入回覆內容 :");
                  while( (recontent = keyboard.nextLine()).equals("") );
                  System.out.println(o.reply(name,select,recontent));
                }else{
                  System.out.println("You need to Login or Registry first.");
                }
                break;
              case 5:
                if(name != null){
                  int  select;
                  System.out.println("顯示一討論主題的內容及其所有回覆內容");
                  System.out.println(o.subject());
                  System.out.println("請輸入欲顯示的討論主題 :<號碼>");
                  select = keyboard.nextInt();
                  System.out.println(o.discussion(select));
                }else{
                  System.out.println("You need to Login or Registry first.");
                }
                break;
              case 6:
                if(name != null){
                  int  select;
                  System.out.println("刪除討論主題");
                  System.out.println(o.subject());
                  System.out.println("請輸入欲刪除的討論主題 :<號碼>");
                  select = keyboard.nextInt();
                  System.out.println(o.delete(name,select));
                }else{
                  System.out.println("You need to Login or Registry first.");
                }
                break;
              case 7:
                  exitFlag = true;
                  break;
              default:
                System.out.println("Typed an error option,please select a  correct option.(0~7)");
                break;
              }
            }
        }catch(Exception e){
          System.out.println("DiscussionServer exception: " + e.getMessage());
          e.printStackTrace();
        }
      }
  }
}
