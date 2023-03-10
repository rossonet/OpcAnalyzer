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

import org.jinterop.dcom.core.JIPointer;
import org.jinterop.dcom.core.JIStruct;
import org.jinterop.dcom.core.JIUnion;

import java.io.Serializable;

/**
 * Implements the <i>TYPEDESC</i> structure of COM Automation and
 * describes the type of a variable, the return type of a function,
 * or the type of a function parameter.
 * 
 * @since 1.0
 */
public final class TypeDesc implements Serializable
{

    private static final long serialVersionUID = 6276233095707726579L;

    public static final Short VT_PTR = new Short ( (short)0x1a );

    public static final Short VT_SAFEARRAY = new Short ( (short)0x1b );

    public static final Short VT_CARRAY = new Short ( (short)0x1c );

    public static final Short VT_USERDEFINED = new Short ( (short)0x1d );

    public final JIPointer typeDesc;

    public final JIPointer arrayDesc;

    public final int hrefType;

    public final short vt;

    TypeDesc ( final JIStruct values )
    {
        if ( values == null )
        {
            this.typeDesc = null;
            this.arrayDesc = null;
            this.hrefType = -1;
            this.vt = -1;
            return;
        }

        this.vt = ( (Short)values.getMember ( 1 ) ).shortValue ();
        final JIUnion union = (JIUnion)values.getMember ( 0 );

        if ( new Short ( this.vt ).equals ( VT_PTR ) || new Short ( this.vt ).equals ( VT_SAFEARRAY ) )
        {
            JIPointer pointer = ( pointer = (JIPointer)union.getMembers ().get ( VT_PTR ) ) == null ? (JIPointer)union.getMembers ().get ( VT_SAFEARRAY ) : pointer;
            this.typeDesc = new JIPointer ( new TypeDesc ( pointer ), false );
            this.arrayDesc = null;
            this.hrefType = -1;
        }
        else if ( new Short ( this.vt ).equals ( VT_CARRAY ) )
        {
            this.hrefType = -1;
            this.typeDesc = null;
            this.arrayDesc = new JIPointer ( new ArrayDesc ( (JIPointer)union.getMembers ().get ( VT_CARRAY ) ) );
        }
        else if ( new Short ( this.vt ).equals ( VT_USERDEFINED ) )
        {
            this.typeDesc = null;
            this.arrayDesc = null;
            this.hrefType = ( (Integer)union.getMembers ().get ( VT_USERDEFINED ) ).intValue ();
        }
        else
        {
            this.typeDesc = null;
            this.arrayDesc = null;
            this.hrefType = -1;
        }

    }

    TypeDesc ( final JIPointer values )
    {
        this ( values.isNull () ? null : (JIStruct)values.getReferent () );
    }

}
