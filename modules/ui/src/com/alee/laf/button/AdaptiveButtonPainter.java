package com.alee.laf.button;

import com.alee.extended.painter.AdaptivePainter;
import com.alee.extended.painter.Painter;

import javax.swing.*;

/**
 * Simple ButtonPainter adapter class.
 * It is used to install simple non-specific painters into WebButtonUI.
 *
 * @author Mikle Garin
 */

public class AdaptiveButtonPainter<E extends JButton, U extends WebButtonUI> extends AdaptivePainter<E, U> implements ButtonPainter<E, U>
{
    /**
     * Constructs new AdaptiveButtonPainter for the specified painter.
     *
     * @param painter painter to adapt
     */
    public AdaptiveButtonPainter ( final Painter painter )
    {
        super ( painter );
    }
}