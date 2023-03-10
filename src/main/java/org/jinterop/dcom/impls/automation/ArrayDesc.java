/**j-Interop (Pure Java implementation of DCOM protocol)
 * Copyright (C) 2006  Vikram Roopchand
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * Though a sincere effort has been made to deliver a professional,
 * quality product,the library itself is distributed WITHOUT ANY WARRANTY;
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110, USA
 */

package org.jinterop.dcom.impls.automation;

import org.jinterop.dcom.core.JIArray;
import org.jinterop.dcom.core.JIPointer;
import org.jinterop.dcom.core.JIStruct;

import java.io.Serializable;

/**
 * Implements the <i>ARRAYDESC</i> structure of COM Automation.
 * <p>
 * Definition from MSDN: <i> Contained within the TYPEDESC, which describes the
 * type of the array's elements, and information about the array's dimensions.
 * </i>
 * 
 * @since 1.0
 */
public final class ArrayDesc implements Serializable
{

    private static final long serialVersionUID = 8801586899375554929L;

    /**
     * Element Type.
     */
    public final TypeDesc typeDesc;

    /**
     * Dimension Count.
     */
    public final short cDims;

    /**
     * Variable length array containing one element for each dimension.
     */
    public final SafeArrayBounds safeArrayBounds[];

    ArrayDesc ( final JIStruct values )
    {
        if ( values == null )
        {
            this.typeDesc = null;
            this.cDims = -1;
            this.safeArrayBounds = null;
            return;
        }

        this.typeDesc = new TypeDesc ( (JIStruct)values.getMember ( 0 ) );
        this.cDims = ( (Short)values.getMember ( 1 ) ).shortValue ();
        final JIArray arry = (JIArray)values.getMember ( 2 );
        final Object[] arry2 = (Object[])arry.getArrayInstance ();

        if ( arry2 != null )
        {
            this.safeArrayBounds = new SafeArrayBounds[arry2.length];
            for ( int i = 0; i < arry2.length; i++ )
            {
                this.safeArrayBounds[i] = new SafeArrayBounds ( (JIStruct)arry2[i] );
            }
        }
        else
        {
            this.safeArrayBounds = null;
        }
    }

    ArrayDesc ( final JIPointer values )
    {
        this ( values.isNull () ? null : (JIStruct)values.getReferent () );
    }
}
