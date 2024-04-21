package com.diamssword.labbyrinth.logger;

import com.diamssword.labbyrinth.view.SplashGui;
import com.diamssword.labbyrinth.view.JavaBridge;

public class Log {
    public static void setProgress(int percent)
    {
        if(JavaBridge.instance!=null)
            JavaBridge.instance.sendEvent("progress",Math.min(percent,100));
        else if(SplashGui.instance !=null)
            SplashGui.instance.setProgress(Math.min(percent,100));
        else
            System.out.println("..."+percent+"%");
    }
    public static void setProgress(String title,int percent)
    {
        if(JavaBridge.instance!=null)
        {
            JavaBridge.instance.sendEvent("progress",Math.min(percent,100));
            JavaBridge.instance.sendEvent("status",title);
        }
        else if(SplashGui.instance !=null)
        {
            SplashGui.instance.setProgress(Math.min(percent,100));
            SplashGui.instance.setProgressTitle(title);
        }
        else
            System.out.println(title+": "+percent+"%");
    }
    public static void setProgress(String title)
    {
        setProgress(title,0);
    }
}
