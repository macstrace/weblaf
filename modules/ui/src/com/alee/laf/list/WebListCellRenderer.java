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

package com.alee.laf.list;

import com.alee.laf.Styles;

import javax.swing.*;
import java.awt.*;

/**
 * Custom default list cell renderer for WebLookAndFeel.
 *
 * @author Mikle Garin
 */

public class WebListCellRenderer extends WebListElement implements ListCellRenderer
{
    /**
     * Constructs default list cell renderer.
     */
    public WebListCellRenderer ()
    {
        super ();
        setName ( "List.cellRenderer" );
    }

    /**
     * Returns list cell renderer component.
     *
     * @param list         tree
     * @param value        cell value
     * @param index        cell index
     * @param isSelected   whether cell is selected or not
     * @param cellHasFocus whether cell has focus or not
     * @return cell renderer component
     */
    @Override
    public Component getListCellRendererComponent ( final JList list, final Object value, final int index, final boolean isSelected,
                                                    final boolean cellHasFocus )
    {
        // Updating style ID
        setStyleId ( getIcon () != null ? Styles.listIconCellRenderer : Styles.listCellRenderer );

        // Orientation
        setComponentOrientation ( list.getComponentOrientation () );

        // Visual settings
        setFont ( list.getFont () );
        setEnabled ( list.isEnabled () );
        setForeground ( isSelected ? list.getSelectionForeground () : list.getForeground () );

        // Icon and text
        if ( value instanceof Icon )
        {
            setIcon ( ( Icon ) value );
            setText ( "" );
        }
        else
        {
            setIcon ( null );
            setText ( value == null ? "" : value.toString () );
        }

        return this;
    }

    /**
     * A subclass of WebListCellRenderer that implements UIResource.
     */
    public static class UIResource extends WebListCellRenderer implements javax.swing.plaf.UIResource
    {
        //
    }
}