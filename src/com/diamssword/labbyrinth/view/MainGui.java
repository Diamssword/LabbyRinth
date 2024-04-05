package com.diamssword.labbyrinth.view;

import com.diamssword.labbyrinth.GameInstance;
import com.diamssword.labbyrinth.Main;
import com.diamssword.labbyrinth.PacksManager;
import com.diamssword.labbyrinth.Profiles;
import com.diamssword.labbyrinth.logger.Log;
import com.diamssword.labbyrinth.view.components.MenuBar;
import com.diamssword.labbyrinth.view.components.PackPicker;
import com.diamssword.labbyrinth.view.components.ProfilePicker;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class MainGui {
    private JButton jouerButton;
    private JPanel panel1;
    private JProgressBar progressBar;
    private JLabel progressTitle;
    private MenuBar menuPanel;
    private JButton menuBt;
    public static MainGui instance;

    public static void create()
    {
        JFrame frame = new JFrame("LabbyRinth");
        if(SplashGui.instance!=null)
        {
            SplashGui.instance.close();
        }
        MainGui gui=new MainGui();
        try {
            frame.setIconImage(ImageIO.read(MainGui.class.getResource("/images/logo.png")));
        } catch (IOException e) {}
        frame.setPreferredSize(new Dimension(900,700));
        frame.setContentPane(gui.panel1);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        instance=gui;
    }
    public void setProgress(int percent)
    {
        this.progressBar.setValue(percent);
    }
    public void setProgressTitle(String title)
    {
        this.progressTitle.setText(title);
    }
    public MainGui()
    {

        MigLayout layout = new MigLayout("fill, debug","[left,30%][40%,center][right,30%]","[][bottom][]");
        panel1= new JPanel();
        panel1.setLayout(layout);
        jouerButton=new JButton("Jouer");
        panel1.add(jouerButton,"cell 1 2, h 15%, w 30%, top, gap 0px 0px 30px");
        panel1.add(menuBt=new JButton("|||"),"cell 0 0, top");
        panel1.add(menuPanel=new MenuBar(),"pos 0 0,w 30%, h 100%");
        menuPanel.setVisible(false);
        panel1.add(new ProfilePicker(),"cell 2 0");
        panel1.add(new PackPicker(),"cell 2 1, top, right, w 30%");
        panel1.add(progressTitle=new JLabel(""),"cell 1 1, w 100%, flowy");
        panel1.add(progressBar=new JProgressBar(0,100),"cell 1 1, w 100%, flowy  ");
        progressBar.setValue(50);
        menuBt.addActionListener(l->{
            menuBt.setVisible(false);
            menuPanel.setVisible(true);
        });
        menuPanel.closeBt.addActionListener(l->{
            menuBt.setVisible(true);
            menuPanel.setVisible(false);
        });
        jouerButton.addActionListener(m->{
            if(Profiles.getSelectedProfile().isPresent())
              PacksManager.launch();
            else
                System.out.println("User is null");
        });
        PacksManager.addUpdatedListener(locked->{
            jouerButton.setEnabled(!locked);

            if(!locked)
                Log.setProgress("Prêt à jouer!");
            else
                Log.setProgress("Mise à jour en cours...");
            Log.setProgress(100);
        });
    }


}
