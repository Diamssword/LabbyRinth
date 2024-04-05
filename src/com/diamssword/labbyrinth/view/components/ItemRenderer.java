package com.diamssword.labbyrinth.view.components;

import com.diamssword.labbyrinth.utils.KeyPair;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;

class PairRenderer extends BasicComboBoxRenderer
    {
        public Component getListCellRendererComponent(
                JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus)
        {
            super.getListCellRendererComponent(list, value, index,
                isSelected, cellHasFocus);
            if (value != null)
            {

                KeyPair item = (KeyPair)value;
                setText( item.getValue());
            }


            return this;
        }
    }