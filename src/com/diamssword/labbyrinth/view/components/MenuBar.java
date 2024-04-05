package com.diamssword.labbyrinth.view.components;

import com.diamssword.labbyrinth.downloaders.Utils;
import com.diamssword.labbyrinth.utils.KeyPair;
import com.diamssword.labbyrinth.view.ConsoleGui;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.lang.management.ManagementFactory;

public class MenuBar extends JPanel {
    public JButton closeBt;
    private JSlider ramSlider;
    private JLabel sliderText;
    public MenuBar()
    {
        super();
        int val=0;
        JSONObject ob=Utils.readCommonCache();
        if(ob.has("ram"))
         val= Utils.readCommonCache().getInt("ram");
        if(val<1)
            val=4;
        this.setOpaque(false);
        this.setBackground(new Color(10,10,10,100));
        this.setLayout(new MigLayout("fill,center,debug,wrap 1"));
        this.add(closeBt=new JButton("<"),"top, right");
        this.add(new JLabel("Mémoire allouée"),"bottom");
        this.add(ramSlider=new JSlider(1,getRam(),val),"w 90%,top");
        this.add(sliderText=new JLabel(val+"G"),"cell 0 2, w 10%,wrap,top");
        JButton consoleBt;
        this.add(consoleBt=new JButton("Console"));
        consoleBt.addActionListener((l)->{
            ConsoleGui.create();
        });

        ramSlider.addChangeListener(l->{
            sliderText.setText(ramSlider.getValue()+"G");
            Utils.setCommonCache(Utils.readCommonCache().put("ram",ramSlider.getValue()));
        });
    }
    public static int getRam()
    {
        com.sun.management.OperatingSystemMXBean mxbean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        String m= FileUtils.byteCountToDisplaySize(mxbean.getTotalMemorySize());
        return Integer.parseInt(m.split(" ")[0])-1;
    }
    protected void paintComponent(Graphics g)
    {
        g.setColor( getBackground() );
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

}
