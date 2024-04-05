package com.diamssword.labbyrinth.view.components;

import com.diamssword.labbyrinth.LauncherVariables;
import com.diamssword.labbyrinth.PacksManager;
import com.diamssword.labbyrinth.Profiles;
import com.diamssword.labbyrinth.utils.KeyPair;
import com.diamssword.labbyrinth.utils.TextUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Optional;
import java.util.Vector;

public class PackPicker extends JPanel {
    JLabel skin;
    JButton addBt;
    JComboBox<KeyPair> selector;
    static ImageIcon STEVE;
    public PackPicker()
    {
        STEVE=new ImageIcon(PackPicker.class.getResource("/images/logo_gray.png"));
        //this.setBackground(new Color(0,0,0,0));
        this.setLayout(new MigLayout("fill,center","[right]4[fill]4[left]"));
        this.add(skin=new SquareLabel(STEVE,SwingConstants.CENTER),"cell 0 0 1 1");
        skin.setMaximumSize(new Dimension(64,64));
        this.add(selector=new JComboBox<>(),"cell 1 0 3 1");
    //    this.add(addBt=new JButton("+"));

     //   addBt.addActionListener(e->addAccount());

        selector.addActionListener(e -> {
            if(e.getActionCommand().equals("comboBoxChanged"))
            {
                String id=((KeyPair)selector.getSelectedItem()).getKey();
                PacksManager.setPreferedPack(id);
                selectUser(id);
            }
        });
        PacksManager.addUpdatedListener(locked->{
            selector.setEnabled(!locked);
            if(!locked)
            {
                selectUser(PacksManager.getPreferedPack());
                selector.setSelectedItem(PacksManager.getSelectedDisplay());
            }

        });

        PacksManager.addReadyListener((packs)->{

            selector.setRenderer(new PairRenderer());
            selector.setModel(new DefaultComboBoxModel<>(PacksManager.getDisplayList()));
            String s=PacksManager.getPreferedPack();
            if(s!=null)
            {
                selector.setSelectedItem(PacksManager.getSelectedDisplay());
                selectUser(s);
            }
        });


    }

    public void selectUser(String pack)
    {
        if(pack!=null) {
            skin.setIconTextGap(0);
            File f=new File(LauncherVariables.getPackFolder(pack),"logo.png");
            if(f.exists() && f.isFile()) {
                ImageIcon ic=new ImageIcon(f.getAbsolutePath());
                ic=new ImageIcon( ic.getImage().getScaledInstance(64,64,Image.SCALE_SMOOTH));
                skin.setIcon(ic);
                skin.repaint();
                return;
            }
        }
            skin.setIcon(STEVE);
            skin.repaint();
    }
}
