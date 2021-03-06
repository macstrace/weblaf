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

package com.alee.extended.painter;

import com.alee.laf.WebLookAndFeel;
import com.alee.managers.log.Log;
import com.alee.utils.LafUtils;
import com.alee.utils.ReflectUtils;
import com.alee.utils.SwingUtils;
import com.alee.utils.laf.PainterShapeProvider;
import com.alee.utils.swing.DataRunnable;

import javax.swing.*;
import java.awt.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * This special class provides basic methods to link painter with components.
 *
 * @author Mikle Garin
 */

public final class PainterSupport
{
    /**
     * Installed painters map.
     */
    private static final Map<JComponent, Map<Painter, PainterListener>> installedPainters =
            new WeakHashMap<JComponent, Map<Painter, PainterListener>> ();

    /**
     * Installs painter into the specified component.
     * It is highly recommended to call this method only from EDT.
     *
     * @param component component painter is applied to
     * @param painter   painter to install
     */
    public static void installPainter ( final JComponent component, final Painter painter )
    {
        // Simply ignore this call if empty painter is set or component doesn't exist
        if ( component == null || painter == null )
        {
            return;
        }

        // Installing painter
        Map<Painter, PainterListener> listeners = installedPainters.get ( component );
        if ( listeners == null )
        {
            listeners = new WeakHashMap<Painter, PainterListener> ( 1 );
            installedPainters.put ( component, listeners );
        }
        if ( !installedPainters.containsKey ( painter ) )
        {
            // Installing painter
            painter.install ( component, LafUtils.getUI ( component ) );

            // Applying initial component settings
            final Boolean opaque = painter.isOpaque ();
            if ( opaque != null )
            {
                LookAndFeel.installProperty ( component, WebLookAndFeel.OPAQUE_PROPERTY, opaque ? Boolean.TRUE : Boolean.FALSE );
            }

            // Creating weak references to use them inside the listener
            // Otherwise we will force it to keep strong reference to component and painter if we use them directly
            final WeakReference<JComponent> c = new WeakReference<JComponent> ( component );
            final WeakReference<Painter> p = new WeakReference<Painter> ( painter );

            // Adding painter listener
            final PainterListener listener = new PainterListener ()
            {
                @Override
                public void repaint ()
                {
                    // Forcing component to be repainted
                    c.get ().repaint ();
                }

                @Override
                public void repaint ( final int x, final int y, final int width, final int height )
                {
                    // Forcing component to be repainted
                    c.get ().repaint ( x, y, width, height );
                }

                @Override
                public void revalidate ()
                {
                    // Forcing layout updates
                    c.get ().revalidate ();
                }

                @Override
                public void updateOpacity ()
                {
                    // Updating component opacity according to painter
                    final Painter painter = p.get ();
                    if ( painter != null )
                    {
                        final Boolean opaque = painter.isOpaque ();
                        if ( opaque != null )
                        {
                            c.get ().setOpaque ( opaque );
                        }
                    }
                }
            };
            painter.addPainterListener ( listener );
            listeners.put ( painter, listener );
        }
    }

    /**
     * Uninstalls painter from the specified component.
     * It is highly recommended to call this method only from EDT.
     *
     * @param component component painter is uninstalled from
     * @param painter   painter to uninstall
     */
    public static void uninstallPainter ( final JComponent component, final Painter painter )
    {
        if ( component == null || painter == null )
        {
            return;
        }
        final Map<Painter, PainterListener> listeners = installedPainters.get ( component );
        if ( listeners != null )
        {
            // Uninstalling painter
            painter.uninstall ( component, LafUtils.getUI ( component ) );

            // Removing painter listener
            listeners.remove ( painter );
        }
    }

    /**
     * Returns the specified painter if it can be assigned to proper painter type.
     * Otherwise returns newly created adapter painter that wraps the specified painter.
     * Used by component UIs to adapt general-type painters for their specific-type needs.
     *
     * @param painter      processed painter
     * @param properClass  proper painter class
     * @param adapterClass adapter painter class
     * @param <T>          proper painter type
     * @return specified painter if it can be assigned to proper painter type, new painter adapter if it cannot be assigned
     */
    public static <T extends Painter & SpecificPainter> T getProperPainter ( final Painter painter, final Class<T> properClass,
                                                                             final Class<? extends T> adapterClass )
    {
        return painter == null ? null : ReflectUtils.isAssignable ( properClass, painter.getClass () ) ? ( T ) painter :
                ( T ) ReflectUtils.createInstanceSafely ( adapterClass, painter );
    }

    /**
     * Returns either the specified painter if it is not an adapted painter or the adapted painter.
     * Used by component UIs to retrieve painters adapted for their specific needs.
     *
     * @param painter painter to process
     * @param <T>     desired painter type
     * @return either the specified painter if it is not an adapted painter or the adapted painter
     */
    public static <T extends Painter> T getAdaptedPainter ( final Painter painter )
    {
        return ( T ) ( painter != null && painter instanceof AdaptivePainter ? ( ( AdaptivePainter ) painter ).getPainter () : painter );
    }

    /**
     * Sets panel painter.
     * Pass null to remove panel painter.
     *
     * @param painter new panel painter
     */
    public static <P extends Painter & SpecificPainter> void setPainter ( final JComponent component, final DataRunnable<P> setter,
                                                                          final P oldPainter, final Painter painter,
                                                                          final Class<P> properClass,
                                                                          final Class<? extends P> adapterClass )
    {
        // Creating adaptive painter if required
        final P properPainter = getProperPainter ( painter, properClass, adapterClass );

        // Properly updating painter
        PainterSupport.uninstallPainter ( component, oldPainter );
        setter.run ( properPainter );
        PainterSupport.installPainter ( component, properPainter );

        // Firing painter change event
        // This is made using reflection because required method is protected within Component class
        firePainterChanged ( component, oldPainter, properPainter );
    }

    /**
     * Fires painter property change event.
     * This is a workaround since {@code firePropertyChange()} method is protected and cannot be called w/o using reflection.
     *
     * @param component  component to fire property change to
     * @param oldPainter old painter
     * @param newPainter new painter
     */
    public static void firePainterChanged ( final JComponent component, final Painter oldPainter, final Painter newPainter )
    {
        try
        {
            ReflectUtils.callMethod ( component, "firePropertyChange", WebLookAndFeel.PAINTER_PROPERTY, oldPainter, newPainter );
        }
        catch ( final NoSuchMethodException e )
        {
            Log.error ( LafUtils.class, e );
        }
        catch ( final InvocationTargetException e )
        {
            Log.error ( LafUtils.class, e );
        }
        catch ( final IllegalAccessException e )
        {
            Log.error ( LafUtils.class, e );
        }
    }

    /**
     * Updates component border using the specified margin
     *
     * @param component component which border needs to be updated
     * @param margin    component margin, or null if it doesn't have one
     * @param painter   component painter, or null if it doesn't have one
     */
    public static void updateBorder ( final JComponent component, final Insets margin, final Painter painter )
    {
        if ( component != null )
        {
            // Preserve old borders
            if ( SwingUtils.isPreserveBorders ( component ) )
            {
                return;
            }

            final boolean ltr = component.getComponentOrientation ().isLeftToRight ();
            final Insets m = new Insets ( 0, 0, 0, 0 );

            // Calculating margin borders
            if ( margin != null )
            {
                m.top += margin.top;
                m.left += ltr ? margin.left : margin.right;
                m.bottom += margin.bottom;
                m.right += ltr ? margin.right : margin.left;
            }

            // Calculating painter borders
            if ( painter != null )
            {
                // Painter borders
                final Insets pi = painter.getMargin ();
                if ( pi != null )
                {
                    m.top += pi.top;
                    m.left += ltr ? pi.left : pi.right;
                    m.bottom += pi.bottom;
                    m.right += ltr ? pi.right : pi.left;
                }
            }

            // Installing border
            component.setBorder ( LafUtils.createWebBorder ( m ) );
        }
    }

    /**
     * Returns component shape according to its painter.
     *
     * @param component component painter is applied to
     * @param painter   component painter
     * @return component shape according to its painter
     */
    public static Shape getShape ( final JComponent component, final Painter painter )
    {
        if ( painter != null && painter instanceof PainterShapeProvider )
        {
            return ( ( PainterShapeProvider ) painter ).provideShape ( component, SwingUtils.size ( component ) );
        }
        else
        {
            return SwingUtils.size ( component );
        }
    }

    /**
     * Returns component preferred size with painter taken into account.
     *
     * @param component component painter is applied to
     * @param ps        default component preferred size
     * @param painter   component painter
     * @return component preferred size with painter taken into account
     */
    public static Dimension getPreferredSize ( final JComponent component, final Dimension ps, final Painter painter )
    {
        // Painter's preferred size
        Dimension pps = painter != null ? SwingUtils.max ( ps, painter.getPreferredSize () ) : ps;

        // Checking layout preferred size
        final LayoutManager layout = component.getLayout ();
        if ( layout != null )
        {
            pps = SwingUtils.max ( ps, layout.preferredLayoutSize ( component ) );
        }

        return pps;
    }
}