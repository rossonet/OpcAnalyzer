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

package org.jinterop.dcom.core;

import ndr.NetworkDataRepresentation;
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.common.JIRuntimeException;
import rpc.core.UUID;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class JIOrpcThat implements Serializable
{

    private static final long serialVersionUID = -9167101165773840248L;

    private int flags = -1;

    private JIOrpcExtentArray[] arry = null;

    private JIOrpcThat ()
    {
    }

    private void setFlags ( final int value )
    {
        this.flags = value;
    }

    //Returns an array of flags present (JIOrpcFlags).
    //For now only 2 flags are returned to the user
    // 0 and 1. Reserved flags are not returned.
    public int[] getSupportedFlags ()
    {

        if ( this.flags == -1 )
        {
            return null;
        }

        if ( ( this.flags & 1 ) == 1 )
        {
            return new int[] { 1 };
        }
        else
        {
            return new int[] { 0 };
        }
    }

    private void setExtentArray ( final JIOrpcExtentArray[] arry )
    {
        this.arry = arry;
    }

    public JIOrpcExtentArray[] getExtentArray ()
    {
        return this.arry;
    }

    static void encode ( final NetworkDataRepresentation ndr )
    {
        ndr.writeUnsignedLong ( 0 );
        ndr.writeUnsignedLong ( 0 );
    }

    static JIOrpcThat decode ( final NetworkDataRepresentation ndr )
    {
        final JIOrpcThat orpcthat = new JIOrpcThat ();
        orpcthat.setFlags ( ndr.readUnsignedLong () );

        //to throw JIRuntimeException from here.
        if ( orpcthat.flags != JIOrpcFlags.ORPCF_NULL && orpcthat.flags != JIOrpcFlags.ORPCF_LOCAL && orpcthat.flags != JIOrpcFlags.ORPCF_RESERVED1 && orpcthat.flags != JIOrpcFlags.ORPCF_RESERVED2 && orpcthat.flags != JIOrpcFlags.ORPCF_RESERVED3 && orpcthat.flags != JIOrpcFlags.ORPCF_RESERVED4 )
        {
            throw new JIRuntimeException ( orpcthat.flags );
        }

        final JIStruct orpcextentarray = new JIStruct ();
        try
        {
            //create the orpcextent struct
            /*
             *  typedef struct tagORPC_EXTENT
            {
            GUID                    id;          // Extension identifier.
            unsigned long           size;        // Extension size.
            [size_is((size+7)&~7)]  byte data[]; // Extension data.
            } ORPC_EXTENT;

             */

            final JIStruct orpcextent = new JIStruct ();
            orpcextent.addMember ( UUID.class );
            orpcextent.addMember ( Integer.class ); //length
            orpcextent.addMember ( new JIArray ( Byte.class, null, 1, true ) );
            //create the orpcextentarray struct
            /*
             *    typedef struct tagORPC_EXTENT_ARRAY
            {
            unsigned long size;     // Num extents.
            unsigned long reserved; // Must be zero.
            [size_is((size+1)&~1,), unique] ORPC_EXTENT **extent; // extents
            } ORPC_EXTENT_ARRAY;

             */

            orpcextentarray.addMember ( Integer.class );
            orpcextentarray.addMember ( Integer.class );
            //this is since the pointer is [unique]
            orpcextentarray.addMember ( new JIPointer ( new JIArray ( new JIPointer ( orpcextent ), null, 1, true ) ) );
        }
        catch ( final JIException e1 )
        {
            //this won't fail...i am certain :)...
        }

        final Map map = new HashMap ();
        final List listOfDefferedPointers = new ArrayList ();
        final JIPointer orpcextentarrayptr = (JIPointer)JIMarshalUnMarshalHelper.deSerialize ( ndr, new JIPointer ( orpcextentarray ), listOfDefferedPointers, JIFlags.FLAG_NULL, map );
        int x = 0;

        while ( x < listOfDefferedPointers.size () )
        {
            final ArrayList newList = new ArrayList ();
            final JIPointer replacement = (JIPointer)JIMarshalUnMarshalHelper.deSerialize ( ndr, listOfDefferedPointers.get ( x ), newList, JIFlags.FLAG_NULL, map );
            ( (JIPointer)listOfDefferedPointers.get ( x ) ).replaceSelfWithNewPointer ( replacement ); //this should replace the value in the original place.
            x++;
            listOfDefferedPointers.addAll ( x, newList );
        }

        final ArrayList extentArrays = new ArrayList ();
        //now read whether extend array exists or not
        //int ptr = ndr.readUnsignedLong();
        if ( !orpcextentarrayptr.isNull () )
        {
            final JIPointer[] pointers = (JIPointer[]) ( (JIArray) ( (JIPointer) ( (JIStruct)orpcextentarrayptr.getReferent () ).getMember ( 2 ) ).getReferent () ).getArrayInstance ();
            for ( int i = 0; i < pointers.length; i++ )
            {
                if ( pointers[i].isNull () )
                {
                    continue;
                }

                final JIStruct orpcextent2 = (JIStruct)pointers[i].getReferent ();
                final Byte[] byteArray = (Byte[]) ( (JIArray)orpcextent2.getMember ( 2 ) ).getArrayInstance ();

                extentArrays.add ( new JIOrpcExtentArray ( ( (UUID)orpcextent2.getMember ( 0 ) ).toString (), byteArray.length, byteArray ) );
            }

        }

        orpcthat.setExtentArray ( (JIOrpcExtentArray[])extentArrays.toArray ( new JIOrpcExtentArray[extentArrays.size ()] ) );

        return orpcthat;
    }
}
