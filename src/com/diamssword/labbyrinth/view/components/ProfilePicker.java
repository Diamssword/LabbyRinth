package com.diamssword.labbyrinth.view.components;

import com.diamssword.labbyrinth.Profiles;
import com.diamssword.labbyrinth.utils.KeyPair;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

public class ProfilePicker extends JPanel {
    JLabel skin;
    JButton addBt;
    JComboBox<KeyPair> selector;
    java.awt.event.ActionListener listener;
    static ImageIcon STEVE;
    public ProfilePicker()
    {
        STEVE=new ImageIcon(ProfilePicker.class.getResource("/images/steve.png"));
     //   this.setBackground(new Color(0,0,0,0));
        this.setLayout(new MigLayout("fill,center","[right]4[fill]4[left]"));
        this.add(skin=new SquareLabel(STEVE,SwingConstants.CENTER),"cell 0 0 1 1");
        this.add(selector=new JComboBox<>(new KeyPair[]{new KeyPair("Steve","Steve")}),"cell 1 0 3 1");
        this.add(addBt=new JButton("+"));
        addBt.addActionListener(e->addAccount());

        listener=e -> {
            if(e.getActionCommand().equals("comboBoxChanged"))
            {
                String id=((KeyPair)selector.getSelectedItem()).getKey();
                Profiles.getUser(id).thenAccept(this::selectUser);
            }
        };
        selector.addActionListener(listener);

        Profiles.loadProfiles().thenAccept(this::populateList);
    }

    public void addAccount()
    {
        String input=JOptionPane.showInputDialog(this.getParent(),"Entrez l'adresse mail de votre compte Microsoft","Ajouter un compte",JOptionPane.PLAIN_MESSAGE);
       if(input !=null)
       {
           Profiles.login(input).thenAccept(r->{
             if(r)
             {
                 Profiles.setReloadNeeded();
                 Profiles.loadProfiles().thenAccept((b)->{
                     populateList(b);
                     Profiles.getUserByEmail(input.trim()).thenAccept(e->{
                         e.ifPresent(Profiles::setSelectedProfile);
                     });
                 });

             }
           });
       }
       JOptionPane.showMessageDialog(this.getParent(),"Une fenêtre va s'ouvrir dans votre navigateur.\nAuthentifiez vous à votre compte Microsoft.\n Vous pouvez ensuite fermer la fenêtre et revenir ici","Connexion en cours...",JOptionPane.INFORMATION_MESSAGE);

    }
    private void populateList(List<Profiles.PlayerProfile> profiles)
    {

            Vector<KeyPair> v=new Vector<>();
            profiles.forEach(c1-> v.add(new KeyPair(c1.uuid,c1.username)));
            selector.setRenderer(new PairRenderer());
            selector.setModel(new DefaultComboBoxModel<>(v));
            Optional<Profiles.PlayerProfile> prof=Profiles.getSelectedProfile();
            prof.ifPresent(c-> selector.setSelectedItem(v.stream().filter(v1->v1.getKey().equals(c.uuid)).findFirst().get()));
            listener.actionPerformed(new ActionEvent(selector,0,"comboBoxChanged"));
            this.repaint();

    }
    public void selectUser(Optional<Profiles.PlayerProfile> profile)
    {
        if(profile.isPresent()) {

            if(Profiles.getSelectedProfile().isEmpty() || !Profiles.getSelectedProfile().get().uuid.equals(profile.get().uuid))
                Profiles.setSelectedProfile(profile.get());
            profile.get().skin.load().thenAccept(c -> {
                    c.getHead(64).thenAccept(c1 -> {
                        skin.setIconTextGap(0);
                        skin.setIcon(new ImageIcon(c1));
                        skin.repaint();
                    });
                });
        }
        else
        {
            skin.setIconTextGap(0);
            skin.setIcon(STEVE);
            skin.repaint();

        }
    }
}
