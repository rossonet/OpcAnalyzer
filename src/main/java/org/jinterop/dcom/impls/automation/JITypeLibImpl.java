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

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.*;
import org.jinterop.dcom.impls.JIObjectFactory;
import rpc.core.UUID;

/**
 * @exclude
 * @since 1.0
 */
final class JITypeLibImpl extends JIComObjectImplWrapper implements IJITypeLib
{

    /**
	 *
	 */
    private static final long serialVersionUID = -7090247136574816759L;

    //IJIComObject comObject = null;
    //JIRemUnknown unknown = null;
    JITypeLibImpl ( final IJIComObject comObject/*, JIRemUnknown unknown*/)
    {
        super ( comObject );
        //this.comObject = comObject;
    }

    public IJIComObject getCOMObject ()
    {
        return this.comObject;
    }

    @Override
    public int getTypeInfoCount () throws JIException
    {
        final JICallBuilder callObject = new JICallBuilder ( true );
        callObject.setOpnum ( 0 );
        callObject.addOutParamAsType ( Integer.class, JIFlags.FLAG_NULL );
        final Object[] result = this.comObject.call ( callObject );
        return ( (Integer)result[0] ).intValue ();
    }

    @Override
    public IJITypeInfo getTypeInfo ( final int index ) throws JIException
    {
        final JICallBuilder callObject = new JICallBuilder ( true );
        callObject.setOpnum ( 1 );
        callObject.addInParamAsInt ( index, JIFlags.FLAG_NULL );
        callObject.addOutParamAsType ( IJIComObject.class, JIFlags.FLAG_NULL );
        final Object[] result = this.comObject.call ( callObject );
        return (IJITypeInfo)JIObjectFactory.narrowObject ( (IJIComObject)result[0] );
    }

    @Override
    public int getTypeInfoType ( final int index ) throws JIException
    {
        final JICallBuilder callObject = new JICallBuilder ( true );
        callObject.setOpnum ( 2 );
        callObject.addInParamAsInt ( index, JIFlags.FLAG_NULL );
        callObject.addOutParamAsType ( Integer.class, JIFlags.FLAG_NULL );
        final Object[] result = this.comObject.call ( callObject );
        return ( (Integer)result[0] ).intValue ();
    }

    @Override
    public IJITypeInfo getTypeInfoOfGuid ( final String uuid ) throws JIException
    {
        final JICallBuilder callObject = new JICallBuilder ( true );
        callObject.setOpnum ( 3 );
        callObject.addInParamAsUUID ( uuid, JIFlags.FLAG_NULL );
        callObject.addOutParamAsType ( IJIComObject.class, JIFlags.FLAG_NULL );
        final Object[] result = this.comObject.call ( callObject );
        return (IJITypeInfo)JIObjectFactory.narrowObject ( (IJIComObject)result[0] );
    }

    @Override
    public void getLibAttr () throws JIException
    {
        final JICallBuilder callObject = new JICallBuilder ( true );
        callObject.setOpnum ( 4 );

        final JIStruct tlibattr = new JIStruct ();
        tlibattr.addMember ( UUID.class );
        tlibattr.addMember ( Integer.class );
        tlibattr.addMember ( Integer.class );
        tlibattr.addMember ( Short.class );
        tlibattr.addMember ( Short.class );
        tlibattr.addMember ( Short.class );

        callObject.addOutParamAsObject ( new JIPointer ( tlibattr ), JIFlags.FLAG_NULL );
        callObject.addOutParamAsType ( Integer.class, JIFlags.FLAG_NULL );//CLEANUPSTORAGE
        final Object[] result = this.comObject.call ( callObject );
        final int i = 0;
    }

    @Override
    public Object[] getDocumentation ( final int memberId ) throws JIException
    {
        final JICallBuilder callObject = new JICallBuilder ( true );
        callObject.addInParamAsInt ( memberId, JIFlags.FLAG_NULL );
        callObject.addInParamAsInt ( 0xb, JIFlags.FLAG_NULL );//refPtrFlags , as per the oaidl.idl...
        callObject.addOutParamAsObject ( new JIString ( JIFlags.FLAG_REPRESENTATION_STRING_BSTR ), JIFlags.FLAG_NULL );
        callObject.addOutParamAsObject ( new JIString ( JIFlags.FLAG_REPRESENTATION_STRING_BSTR ), JIFlags.FLAG_NULL );
        callObject.addOutParamAsObject ( Integer.class, JIFlags.FLAG_NULL );
        callObject.addOutParamAsObject ( new JIString ( JIFlags.FLAG_REPRESENTATION_STRING_BSTR ), JIFlags.FLAG_NULL );
        callObject.setOpnum ( 6 );
        return this.comObject.call ( callObject );
    }

    @Override
    public Object[] findName ( final JIString nameBuf, final int hashValue, final short found ) throws JIException
    {
        final JICallBuilder callObject = new JICallBuilder ( true );
        callObject.setOpnum ( 8 );
        callObject.addInParamAsString ( nameBuf.getString (), nameBuf.getType () );
        callObject.addInParamAsInt ( hashValue, JIFlags.FLAG_NULL );
        callObject.addInParamAsShort ( found, JIFlags.FLAG_NULL );

        callObject.addOutParamAsObject ( new JIArray ( IJIComObject.class, null, 1, true, true ), JIFlags.FLAG_NULL );
        callObject.addOutParamAsObject ( new JIArray ( Integer.class, null, 1, true, true ), JIFlags.FLAG_NULL );
        callObject.addOutParamAsType ( Short.class, JIFlags.FLAG_NULL );
        callObject.addOutParamAsObject ( new JIString ( JIFlags.FLAG_REPRESENTATION_STRING_BSTR ), JIFlags.FLAG_NULL );

        return this.comObject.call ( callObject );
    }
}
