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

/**
 * @exclude
 * @since 1.0
 */
final class JIEnumVARIANTImpl extends JIComObjectImplWrapper implements IJIEnumVariant
{

    //IJIComObject comObject = null;

    /**
	 *
	 */
    private static final long serialVersionUID = -8405188611519724869L;

    JIEnumVARIANTImpl ( final IJIComObject comObject )
    {
        super ( comObject );
    }

    @Override
    public Object[] next ( final int celt ) throws JIException
    {
        final JICallBuilder callObject = new JICallBuilder ( true );
        callObject.setOpnum ( 0 );
        callObject.addInParamAsInt ( celt, JIFlags.FLAG_NULL );
        callObject.addOutParamAsObject ( new JIArray ( JIVariant.class, null, 1, true, true ), JIFlags.FLAG_NULL );
        callObject.addOutParamAsType ( Integer.class, JIFlags.FLAG_NULL );
        final Object[] result = this.comObject.call ( callObject );
        return result;
    }

    @Override
    public void skip ( final int celt ) throws JIException
    {
        final JICallBuilder callObject = new JICallBuilder ( true );
        callObject.setOpnum ( 1 );
        callObject.addInParamAsInt ( celt, JIFlags.FLAG_NULL );
        final Object[] result = this.comObject.call ( callObject );
    }

    @Override
    public void reset () throws JIException
    {
        final JICallBuilder callObject = new JICallBuilder ( true );
        callObject.setOpnum ( 2 );
        final Object[] result = this.comObject.call ( callObject );
    }

    @Override
    public IJIEnumVariant Clone () throws JIException
    {
        final JICallBuilder callObject = new JICallBuilder ( true );
        callObject.setOpnum ( 3 );
        callObject.addOutParamAsObject ( IJIComObject.class, JIFlags.FLAG_NULL );
        final Object[] result = this.comObject.call ( callObject );
        return (IJIEnumVariant)JIObjectFactory.narrowObject ( (IJIComObject)result[0] );
    }

}
