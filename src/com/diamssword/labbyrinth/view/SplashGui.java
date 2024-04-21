package com.diamssword.labbyrinth.view;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class SplashGui {
    private final JPanel panel1;
    private final JLabel progressTitle;
    private final JProgressBar progressBar;
    public void setProgress(int percent)
    {
        this.progressBar.setValue(percent);
    }
    public void setProgressTitle(String title)
    {
        this.progressTitle.setText(title);
    }
    public static SplashGui instance;
    private static JFrame frame;
    public static void create()
    {
        frame = new JFrame(" Initialisation LabbyRinth");
        SplashGui gui=new SplashGui();
        try {
            frame.setIconImage(ImageIO.read(SplashGui.class.getResource("/images/logo.png")));
        } catch (IOException e) {}
        frame.setPreferredSize(new Dimension(300,100));
        frame.setResizable(false);
        frame.setContentPane(gui.panel1);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        instance=gui;
    }
    public void close()
    {
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.dispose();
    }
    public SplashGui()
    {

        panel1= new JPanel();
        panel1.add(progressTitle=new JLabel("..."),"wrap");
        panel1.add(progressBar=new JProgressBar(0,100),"grow");
        progressBar.setValue(100);

    }
}
