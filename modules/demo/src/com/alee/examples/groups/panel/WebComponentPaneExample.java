/*
 * This file is part of WebLookAndFeel library.
 *
 * WebLookAndFeel library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WebLookAndFeel library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WebLookAndFeel library.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alee.examples.groups.panel;

import com.alee.examples.WebLookAndFeelDemo;
import com.alee.examples.content.DefaultExample;
import com.alee.extended.panel.GridPanel;
import com.alee.extended.panel.GroupPanel;
import com.alee.extended.panel.WebComponentPane;
import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.slider.WebSlider;
import com.alee.laf.text.WebTextField;

import java.awt.*;

/**
 * @author Mikle Garin
 */

public class WebComponentPaneExample extends DefaultExample
{
    @Override
    public String getTitle ()
    {
        return "Components panel";
    }

    @Override
    public String getDescription ()
    {
        return "Web-styled components panel";
    }

    @Override
    public Component getPreview ( WebLookAndFeelDemo owner )
    {
        // Creating WebComponentPanel to place some components into it
        WebComponentPane webComponentPane = new WebComponentPane ();
        webComponentPane.setElementMargin ( 10 );
        webComponentPane.setReorderingAllowed ( true );

        // Adding a panel with buttons
        WebButton b1 = new WebButton ( "Button" );
        WebButton b2 = new WebButton ( "Button" );
        WebButton b3 = new WebButton ( "Button" );
        webComponentPane.addElement ( new GridPanel ( 10, b1, b2, b3 ) );

        // Adding a panel with label and field
        WebLabel label = new WebLabel ( "Field" );
        WebTextField field = new WebTextField ();
        field.putClientProperty ( GroupPanel.FILL_CELL, true );
        webComponentPane.addElement ( new GroupPanel ( 10, label, field ) );

        // Adding a simple slider
        WebSlider slider = new WebSlider ( WebSlider.HORIZONTAL, 0, 100, 50 );
        webComponentPane.addElement ( slider );

        // Adding a panel with textfields
        WebTextField f1 = new WebTextField ();
        WebTextField f2 = new WebTextField ();
        WebTextField f3 = new WebTextField ();
        webComponentPane.addElement ( new GridPanel ( 10, f1, f2, f3 ) );

        return new GroupPanel ( webComponentPane );
    }
}