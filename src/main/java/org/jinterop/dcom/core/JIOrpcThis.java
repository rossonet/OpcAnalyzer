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

import ndr.NdrException;
import ndr.NetworkDataRepresentation;
import org.jinterop.dcom.common.JIComVersion;
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.common.JISystem;
import org.jinterop.dcom.common.UUIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.core.UUID;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class JIOrpcThis implements Serializable
{
    private final static Logger logger = LoggerFactory.getLogger ( JIOrpcThis.class );

    private static final long serialVersionUID = 9148006530957254901L;

    private static ThreadLocal cidForCallback = new ThreadLocal ();

    private int flags = 0;

    private JIOrpcExtentArray[] arry = null;

    private JIComVersion version = JISystem.getCOMVersion ();

    private String cid = null;

    public JIOrpcThis ()
    {
        this.cid = UUIDGenerator.generateID ();
    }

    public JIOrpcThis ( final UUID casualityIdentifier )
    {
        this.cid = casualityIdentifier.toString ();
    }

    public void setORPCFlags ( final int flags )
    {
        this.flags = flags;
    }

    public int getORPCFlags ()
    {
        return this.flags;
    }

    public void setExtentArray ( final JIOrpcExtentArray[] arry )
    {
        this.arry = arry;
    }

    public JIOrpcExtentArray[] getExtentArray ()
    {
        return this.arry;
    }

    public String getCasualityIdentifier ()
    {
        return this.cid;
    }

    public void encode ( final NetworkDataRepresentation ndr )
    {
        ndr.writeUnsignedShort ( this.version.getMajorVersion () ); //COM Major version
        ndr.writeUnsignedShort ( this.version.getMinorVersion () ); //COM minor version
        ndr.writeUnsignedLong ( this.flags ); // No Flags
        ndr.writeUnsignedLong ( 0 ); // Reserved ...always 0.

        //the order here is important since the cid is always filled from the ctor hence will never be null.
        final String cid2 = cidForCallback.get () == null ? this.cid : (String)cidForCallback.get ();
        //		System.out.println(cid2);
        rpc.core.UUID uuid = new rpc.core.UUID ( cid2 );
        try
        {
            uuid.encode ( ndr, ndr.getBuffer () );
        }
        catch ( final NdrException e )
        {
            logger.warn ( "JIOrpcThis", e );
        }

        int i = 0;
        if ( this.arry != null && this.arry.length != 0 )
        {
            ndr.writeUnsignedLong ( this.arry.length );
            ndr.writeUnsignedLong ( 0 );
            while ( i < this.arry.length )
            {
                final JIOrpcExtentArray arryy = this.arry[i];
                uuid = new rpc.core.UUID ( arryy.getGUID () );
                try
                {
                    uuid.encode ( ndr, ndr.getBuffer () );
                }
                catch ( final NdrException e )
                {
                    logger.warn ( "encode", e );
                }

                ndr.writeUnsignedLong ( arryy.getSizeOfData () );
                ndr.writeOctetArray ( arryy.getData (), 0, arryy.getSizeOfData () );
                i++;
            }
        }
        else
        {
            ndr.writeUnsignedLong ( 0 );
        }
    }

    static JIOrpcThis decode ( final NetworkDataRepresentation ndr )
    {
        final JIOrpcThis retval = new JIOrpcThis ();
        final Map map = new HashMap ();
        final int majorVersion = ( (Short)JIMarshalUnMarshalHelper.deSerialize ( ndr, Short.class, null, JIFlags.FLAG_NULL, map ) ).intValue ();
        final int minorVersion = ( (Short)JIMarshalUnMarshalHelper.deSerialize ( ndr, Short.class, null, JIFlags.FLAG_NULL, map ) ).intValue ();

        retval.version = new JIComVersion ( majorVersion, minorVersion );
        retval.flags = ( (Integer)JIMarshalUnMarshalHelper.deSerialize ( ndr, Integer.class, null, JIFlags.FLAG_NULL, map ) ).intValue ();

        JIMarshalUnMarshalHelper.deSerialize ( ndr, Integer.class, null, JIFlags.FLAG_NULL, map );//reserved.

        final rpc.core.UUID uuid = new rpc.core.UUID ();
        try
        {
            uuid.decode ( ndr, ndr.getBuffer () );
            retval.cid = uuid.toString ();
        }
        catch ( final NdrException e )
        {
            logger.warn ( "decode", e );
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

        retval.arry = (JIOrpcExtentArray[])extentArrays.toArray ( new JIOrpcExtentArray[extentArrays.size ()] );

        //decode can only be executed incase of a request made from the server side in case of a callback. so the thread making this
        //callback will store the cid from the decode operation in the threadlocal variable. In case an encode is performed using the
        //same thread then we know that this is a nested call. Hence will replace the cid with the thread local cid. For the calls being in
        //case of encode this value will not be used if the encode thread is of the client and not of JIComOxidRuntimeHelper.
        cidForCallback.set ( retval.cid );
        return retval;
    }

}
