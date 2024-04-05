package com.diamssword.labbyrinth.view;

import com.diamssword.labbyrinth.PacksManager;
import com.diamssword.labbyrinth.Profiles;
import com.diamssword.labbyrinth.downloaders.FileDownloader;
import com.diamssword.labbyrinth.logger.Log;
import com.diamssword.labbyrinth.view.components.MenuBar;
import com.diamssword.labbyrinth.view.components.PackPicker;
import com.diamssword.labbyrinth.view.components.ProfilePicker;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ConsoleGui {
    public static ConsoleGui instance;
    private final JPanel panel1;
    private final JTextPane textArea;

    public static void create()
    {
        JFrame frame = new JFrame("Console | LabbyRinth");
        ConsoleGui gui=new ConsoleGui();
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
    public ConsoleGui()
    {

        MigLayout layout = new MigLayout("fill, debug","[100%]","[100%]");
        panel1= new JPanel();
        panel1.setLayout(layout);
        textArea = new JTextPane();
        textArea.setEditable(false);

        JScrollPane scroll = new JScrollPane(textArea);

        panel1.add(scroll,"w 100%,h 100%");
    }
    public static void pipeOutput(InputStream stream,InputStream errorStream) throws IOException {
        if(instance !=null)
        {
            new Thread(()->{
                // IOUtils.copy(stream,new GuiOutputStream(instance.textArea,Color.BLACK));
                InputStreamReader inputStreamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(inputStreamReader);
                String line;
                try {
                    while ((line = br.readLine()) != null) {
                        appendToPane(instance.textArea, line+"\n",Color.black);
                    }
                }catch (IOException e)
                {
                    e.printStackTrace();
                }
                System.out.println("EndOfStream");
            }).start();
            new Thread(()->{
                try {
                    IOUtils.copy(errorStream,new GuiOutputStream(instance.textArea,Color.RED));
                    System.out.println("EndOfStreamErr");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }
    private static void appendToPane(JTextPane tp, String msg, Color c)
    {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        try {
            tp.getDocument().insertString(tp.getDocument().getLength(), msg,aset);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

    }

    public static class GuiOutputStream extends OutputStream
    {
        JTextPane component;
        StringBuilder currentText=new StringBuilder();
        Color color;
        public GuiOutputStream(JTextPane console,Color color) {
            super();
            this.component=console;
            this.color=color;

        }

        @Override
        public void write(int b) {
            // redirects data to the text area
            currentText.append((char)b);
            if(currentText.toString().contains(System.lineSeparator()))
            {
                appendToPane(component,currentText.toString(),color);
                currentText=new StringBuilder();
                component.setCaretPosition(component.getDocument().getLength());
            }

            // keeps the textArea up to date
            //textArea.update(textArea.getGraphics());
        }
    }
}
