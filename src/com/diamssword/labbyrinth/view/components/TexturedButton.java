package com.diamssword.labbyrinth.view.components;

import javax.swing.*;

public class TexturedButton extends JButton {
    public TexturedButton(String buttonPath,String overedPath,String clickedPath,String disabledPath)
    {
        super();
        if(buttonPath!=null)
            this.setIcon(new ImageIcon(buttonPath));
        this.setOpaque(false);
        this.setContentAreaFilled(false);
        this.setBorderPainted(false);
        this.setFocusPainted(false);

    }
}
