import java.io.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.Date;
import java.util.StringTokenizer;
import java.text.SimpleDateFormat;

public class DiscuServerRMIImpl extends UnicastRemoteObject implements DiscuInterface{
  //檔案內容替換Class
  private class LoadChangeTxt extends UnicastRemoteObject{
    public LoadChangeTxt() throws java.rmi.RemoteException{}
     //取得檔案所有內容
     //@param =>path        檔案路徑
     //@return 文字檔內容(String)
    protected String getFileText(String path){
        StringBuffer strBuf = new StringBuffer();
        String newline = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            while(br.ready()) {
                String brStr = br.readLine();
                if(brStr.length() > 0){
                    //處理讀取BOM（ byte-order mark )格式檔案,在讀取utf8檔案的開頭會有utf8檔案格式的標記,需略過此標記再重串內容,標記16進位EF BB BF
                    int c = brStr.charAt(0);
                    if(c == 65279){
                        brStr = brStr.substring(1, brStr.length());
                    }
                    strBuf.append(brStr);
                    strBuf.append("\r\n");
                }
            }
            br.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        return strBuf.toString();
    }
         //將 String內容寫入檔案
         //@param =>path    檔案路徑
         //@param =>txt    要寫入的文字內容
         //@return    回傳1寫檔成功 | 回傳-1 寫檔失敗
        protected boolean writeFileText(String path,String txt){
            try {
                BufferedWriter br = new BufferedWriter(new FileWriter(path));
                br.write(txt);
                br.close();
            }catch(IOException e){
                e.printStackTrace();
                return false;
            }
            return true;
        }
         //改變檔案文字內容
         //@param path        檔案路徑
         //@param oldWord    要被替換的字
         //@param word        新的字
         //@return            修改過的文字內容
        public void changeTxtWord(String path, String oldWord,String newword){
            String txt = getFileText(path);          //取得檔案內容
            txt = txt.replaceAll(oldWord, newword);    //換掉某個字
            writeFileText(path,txt);                //將換過的檔案內容寫回去
            //return txt;
        }
        //附加文字檔內容
      /*  public void appendTxtWord(String path, String oldWord,String newword){
          StringBuffer strbuf = new StringBuffer();
          String txt = getFileText(path);
          strbuf.append(txt);
        }*/

      }

  protected   String    fpathD  = "D:\\lab5discussion.txt";
  protected   String    fpathR  = "D:\\lab5registry.txt";
  protected   String    fpathRe  = "D:\\lab5reply";
  public static Object 	lockD = new Object();
  public static Object 	lockR = new Object();

  // This implementation must have a public constructor.
  // The constructor throws a RemoteException.
  public DiscuServerRMIImpl() throws java.rmi.RemoteException
  {
    super(); 	// Use constructor of parent class

    File  fileRegistry  =  new File(fpathR);
    File  fileDiscussion  =  new File(fpathD);
    File  fileReply  =  new File(fpathRe);
    try{
      if(!fileRegistry.exists())
        fileRegistry.createNewFile();
      if(!fileDiscussion.exists())
        fileDiscussion.createNewFile();
      if(!fileReply.exists())
        fileReply.mkdir();
    }catch(IOException e){
      System.out.println("Exception: " + e.getMessage());
      e.printStackTrace();
    }

  }

  public static String file_illegelcharfilter(String filename){
    char[]    filenochar  =  {'?','/','\\','\"','|','<','>','*',':'};
    int   capacity  =  filename.length();
    char[]    read   =   new   char[capacity];
    char  temp;

    for(int i = 0;i < capacity;i++){
      temp  =  filename.charAt(i);
      for(int j = 0;j < 8;j++){
        if(temp == filenochar[j]){
          temp = '_';
          break;
        }
      }
      read[i] = temp;
    }
    String result = new String(read);
    return  result;
  }

  public static String getDatestring(){
    SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss aaa");
    Date    date = new Date();
    String strDate = sdFormat.format(date);
    return strDate;
  }

  //methods
  public String log(String account,String password)throws java.rmi.RemoteException
  {
      String    line,write;
      String    response      =    new String();
      int       index;
      boolean   acountflag     =     true;  //帳號有無
      boolean   pwdFlag      =    false;
      BufferedReader  r    =    null;

      try{
          synchronized(lockR){
            r = new BufferedReader(new FileReader(fpathR));
            while((line = r.readLine()) != null){
                if((index = line.indexOf(account)) >= 0){
                  acountflag = false;
                  if((index = line.indexOf(password)) >= 0){
                    pwdFlag = true;
                    r.close();
                  }else{
                    response = "-ERR Password is error.";
                  }
                  break;
                }else{
                  continue;
                }
            }
          }
          if(pwdFlag){
            String lasttime,newtime,lastformat,newformat;
            //抓時間
            index = line.indexOf('/');
            lasttime = line.substring(index-4);
            //get log in time for this time log in.
            newtime = this.getDatestring();
            //rewrite log in time
            lastformat = line;
            newformat = line.replace(lasttime,newtime);
            //newformat = account +" "+ password +" "+ +newtime;
            synchronized(lockR){
              LoadChangeTxt   t;
              t   =  new  LoadChangeTxt();
              t.changeTxtWord(fpathR,lastformat,newformat);
            }
            line = "";
            response = "+OK Log suecess ,last time log in: "+ lasttime +".";
          }
          if(acountflag)
          {
            response =  "-ERR This account not exist.";
          }
      }catch(IOException e){
        System.out.println("Exception: " + e.getMessage());
			  e.printStackTrace();
      }
      return response;
  }

  public boolean register(String account,String password) throws java.rmi.RemoteException
	{
      String line,write;
      StringBuffer writebuffer  =  new StringBuffer();
      boolean   flag      =    true;  //assume that account isn't existed is true
      BufferedReader r    =    null;
      BufferedWriter w    =    null;

      try{
          synchronized(lockR){
            r = new BufferedReader(new FileReader(fpathR));
            while((line = r.readLine()) != null){
                if(line.indexOf(account) >= 0){
                  flag = false;
                  break;
                }
            }
            r.close();
          }
          // Add account entry
          if(flag){
            synchronized(lockR){
              w = new BufferedWriter(new FileWriter(fpathR,true));
              write  =  account + "   " + password + "   ";
              writebuffer.append("\r\n");   writebuffer.append(write);    writebuffer.append(getDatestring());
              write  =  writebuffer.toString();
              w.write(write);
              w.close();
            }
          }
      }catch(IOException e){
        System.out.println("Exception: " + e.getMessage());
			  e.printStackTrace();
      }
      return flag;
	}

  public String create(String account,String topic,String content) throws java.rmi.RemoteException
  {
    BufferedReader  r    =    null;
    BufferedWriter  w    =    null;
    String    line,write;
    String    re = "";
    boolean   flag    =   true;
    String    strDate = getDatestring();

    try{
      synchronized(lockD){
        r = new BufferedReader(new FileReader(fpathD));
        while((line = r.readLine()) != null){
          if(line != null && line.length() > 0){
            if(line.charAt(0) == '\''){
              if(line.indexOf(topic) >= 0){
                re = "The topic has existed!";
                flag = false;
                r.close();
                break;
              }
            }else{  continue;  }
          }
        }
      }
      if(flag){
        synchronized(lockD){
          w = new BufferedWriter(new FileWriter(fpathD,true));
          w.newLine();
          write = "'主題:"+ topic + " 作者:" + account + " 時間:"+strDate+"'";
          w.write(write);
          w.newLine();
          write = "@"+content+"@";
          w.write(write);
          re = "Created suecess!";
          w.close();
        }
      }
    }catch(IOException e){
      System.out.println("Exception: " + e.getMessage());
      e.printStackTrace();
    }
    return re;
  }

  public String subject() throws java.rmi.RemoteException
  {
    int     i           =   1;
    int     state            ;
    String  responsestr =   new String();
    String  line        =   null;
    BufferedReader  r   =   null;

    try{
      synchronized(lockD){
        r   =   new  BufferedReader(new  FileReader(fpathD));
        while((line = r.readLine()) != null){
          if(line != null && line.length() > 0){
            if(line.charAt(0) == '\''){
              responsestr += i + ". " + line + "\r\n";
              i++;
            }else{
              continue;
            }
          }
        }
        r.close();
      }
    }catch(IOException e){
      System.out.println("Exception: " + e.getMessage());
      e.printStackTrace();
    }
    return responsestr;
  }

  public String reply(String account,int select,String content) throws java.rmi.RemoteException
  { //主題:   作者:   日期:
    int          index;
    String       topic = new String();
    String       responsestr = "回覆成功!";
    String       line   =   null;
    BufferedReader  rd   =   null;

    try{
      synchronized(lockD){
        rd  =  new BufferedReader(new FileReader(fpathD));
        while((line = rd.readLine()) != null && select > 0){
          if(line != null && line.length() > 0){
            if(line.charAt(0) == '\''){
              select--;
              if(select == 0){
                rd.close();
                break;
              }
            }else{
              continue;
            }
          }
        }
      }
      index = line.indexOf(" ");
      topic = this.file_illegelcharfilter(line.substring(4,index));
    }catch(IOException e){
      System.out.println("Exception: " + e.getMessage());
      e.printStackTrace();
    }
    String path = "D:\\lab5reply\\" + topic +".txt";
    //Object "lock"+path  =  new Object();
    try{
      File file = new File(path);
      if(file.exists()){
        file.createNewFile();;    //檔案不存在 開新檔案
      }
        BufferedWriter  w  =  new  BufferedWriter(new FileWriter(path,true));
        StringBuffer reString = new StringBuffer();
        reString.append(account); reString.append(" 說:"); reString.append(content); reString.append(" "); reString.append(this.getDatestring());
        w.newLine();
        w.write(reString.toString());
        w.close();
      }catch(IOException e){
        responsestr  =  "產生錯誤";
        System.out.println("Exception: " + e.getMessage());
        e.printStackTrace();
      }
    return responsestr;
  }

  public String discussion(int select) throws java.rmi.RemoteException
  {
    int          index;
    String       responsestr = "";
    String       topic = new String();
    String       line   =   null;
    StringBuffer   strbuf  =  new  StringBuffer();
    BufferedReader   rd   =   null;

    try{
      rd  =  new BufferedReader(new FileReader(fpathD));
      while((line = rd.readLine()) != null && select > 0){
        if(line != null && line.length() > 0){
          if(line.charAt(0) == '\''){
            select--;
            if(select == 0){
              index = line.indexOf(" ");
              topic = this.file_illegelcharfilter(line.substring(4,index)); //抓主題後濾掉非法字元，用以當檔名
              strbuf.append(line);
              strbuf.append("\r\n");
              while((line = rd.readLine()) != null){    //往下讀出完整內容
                  if(line != null && line.length() > 0){
                    if(line.charAt(0) != '\''){
                      strbuf.append(line);
                      strbuf.append("\r\n");
                    }else{
                      break;
                    }
                  }
              }
              rd.close();
              break;
            }else{
              continue;
            }
          }
        }
      }
    }catch(IOException e){
      System.out.println("Exception: " + e.getMessage());
      e.printStackTrace();
    }
    String path = "D:\\lab5reply\\" + topic +".txt";    //讀回復
    File file = new File(path);
      if(file.exists()){
        try{
          BufferedReader  r  =  new  BufferedReader(new FileReader(path));
          while((line = r.readLine()) != null){
            if(line != null && line.length() > 0){
              strbuf.append(line);
              strbuf.append("\r\n");
            }
          }
          r.close();
        }catch(IOException e){
          System.out.println("Exception: " + e.getMessage());
          e.printStackTrace();
        }
      }
      return strbuf.toString();
    }

    public String delete(String account,int select) throws java.rmi.RemoteException
    {
      BufferedReader  r  =  null;
      String line  =  new String();
      String topic  =  new String();
      int index,index2;
      boolean flag  =  false;
      String  name  =  null;
      String  header  =  null;
      String  restr =  "成功!";
      StringBuffer strbuf = new StringBuffer();
      LoadChangeTxt  t  =  new  LoadChangeTxt();

      try{
        r  =  new BufferedReader(new FileReader(fpathD));
        while((line = r.readLine()) != null && select > 0){
          if(line.length() > 0 && line != null){
            if(line.charAt(0) == '\''){
              select--;
              if(select == 0){
                header = line;
                index = line.indexOf(" ");
                index2 = line.indexOf(" ",index+1);
                name = line.substring((index+4),index2);
                if(name.equals(account)){
                  flag = true;
                  topic = this.file_illegelcharfilter(line.substring(4,index));
                  t.changeTxtWord(fpathD,header,"已刪除的主題是無緣見面的~");
                  strbuf.append(header);
                  while((line = r.readLine()) != null){
                    if(line != null && line.length() > 0){
                      if(line.charAt(0) != '\''){
                        t.changeTxtWord(fpathD,line,"已刪除的內容是無緣見面的~");
                        strbuf.append(line);
                        strbuf.append("\r\n");
                      }else{
                        break;
                      }
                    }
                  }
                }else{
                  restr =  "這不是你的討論主題!";
                  break;
                }
              }
            }
          }
        }
        r.close();
      }catch(IOException e){
        System.out.println("Exception: " + e.getMessage());
        e.printStackTrace();
      }
      String  path  =  "D:\\lab5reply\\"+ topic +".txt";
      if(flag){
        File  file  =  new  File(path);
        if(file.delete()){
          restr = "刪除成功";
        }else{
          restr = "刪除錯誤";
        }
      }
      return restr;
    }
}
