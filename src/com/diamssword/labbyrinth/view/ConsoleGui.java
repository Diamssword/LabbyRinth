package com.diamssword.labbyrinth.view;

import com.diamssword.labbyrinth.Main;
import com.diamssword.labbyrinth.logger.TextAreaOutputStream;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.PrintStream;
import java.util.logging.Logger;

public class ConsoleGui extends JPanel{
    private JPanel panel1;
    private JTextPane textPane;
    private static JFrame frame;
    private static PrintStream oldOut;
    private static PrintStream oldErr;
    private static void createWindow()
    {
        frame=new JFrame();
        var gui=new ConsoleGui();
        frame.setContentPane(gui);
        frame.pack();
        frame.setTitle("Console");
        frame.setSize(960, 600);
        frame.setVisible(true);
        frame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {  }
            @Override
            public void windowClosing(WindowEvent e) {
            }
            @Override
            public void windowClosed(WindowEvent e) {}
            @Override
            public void windowIconified(WindowEvent e) {
                System.setErr(oldErr);
                System.setOut(oldOut);
                Main.logger=  Logger.getLogger(Main.class.getName());
            }
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) { }
            @Override
            public void windowDeactivated(WindowEvent e) { }
        });
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        PrintStream con=new PrintStream(new TextAreaOutputStream(gui.textPane,20000));
        oldOut=System.out;
        oldErr=System.err;
        System.setOut(con);
        System.setErr(con);
        Main.logger=  Logger.getLogger("Console");
    }
    public static void showConsole()
    {
        if(frame == null || !frame.isVisible())
        {
            if(frame!=null)
                frame.dispose();
            SwingUtilities.invokeLater(ConsoleGui::createWindow);
        }

    }
    public static void hideConsole()
    {
        if(frame != null && frame.isVisible()) {
            System.setErr(oldErr);
            System.setOut(oldOut);
            Main.logger=  Logger.getLogger(Main.class.getName());
            frame.dispose();
        }
    }
    public ConsoleGui()
    {
        super();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }
        this.setLayout(new BorderLayout());
        var pane=new JScrollPane();
        pane.setViewportView(textPane=new JTextPane());
        pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        textPane.setEditable(false);
        textPane.setBackground(Color.DARK_GRAY);
        textPane.addStyle("style",null);
        DefaultCaret caret = (DefaultCaret) textPane.getCaret();
        this.add(pane,BorderLayout.CENTER);

    }
}
