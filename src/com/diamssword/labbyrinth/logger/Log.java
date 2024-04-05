package com.diamssword.labbyrinth.logger;

import com.diamssword.labbyrinth.Main;
import com.diamssword.labbyrinth.view.MainGui;
import com.diamssword.labbyrinth.view.SplashGui;

public class Log {
    public static void setProgress(int percent)
    {
        if(MainGui.instance!=null)
            MainGui.instance.setProgress(Math.min(percent,100));
        else if(SplashGui.instance !=null)
            SplashGui.instance.setProgress(Math.min(percent,100));
        else
            System.out.println("..."+percent+"%");
    }
    public static void setProgress(String title,int percent)
    {
        if(MainGui.instance!=null)
        {
            MainGui.instance.setProgress(Math.min(percent,100));
            MainGui.instance.setProgressTitle(title);
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
