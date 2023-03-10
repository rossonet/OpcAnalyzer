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

import java.io.Serializable;

final class JISecurityBinding implements Serializable
{

    private static final long serialVersionUID = 2100264431889577123L;

    private JISecurityBinding ()
    {
    }

    public static final int COM_C_AUTHZ_NONE = 0xffff;

    private int authnSvc = 0; // Cannot be zero.

    private int authzSvc = 0; // Must not be zero.

    private String princName = null; // Zero terminated.

    private int length = -1;

    public int getLength ()
    {
        return this.length;
    }

    JISecurityBinding ( final int authnSvc, final int authzSvc, final String princName )
    {
        this.authnSvc = authnSvc;
        this.authzSvc = authzSvc;
        this.princName = princName;
        if ( princName.equals ( "" ) )
        {
            this.length = 2 + 2 + 2;
        }
        else
        {
            this.length = 2 + 2 + princName.length () * 2 + 2;
        }
    }

    static JISecurityBinding decode ( final NetworkDataRepresentation ndr )
    {
        final JISecurityBinding securityBinding = new JISecurityBinding ();

        securityBinding.authnSvc = ndr.readUnsignedShort ();

        if ( securityBinding.authnSvc == 0 )
        {
            //security binding over.
            return null;
        }

        securityBinding.authzSvc = ndr.readUnsignedShort ();

        //now to read the String till a null termination character.
        // a '0' will be represented as 30
        int retVal = -1;
        final StringBuffer buffer = new StringBuffer ();
        while ( ( retVal = ndr.readUnsignedShort () ) != 0 )
        {
            //even though this is a unicode string , but will not have anything else
            //other than ascii charset, which is supported by all encodings.
            buffer.append ( new String ( new byte[] { (byte)retVal } ) );
        }

        securityBinding.princName = buffer.toString ();

        // 2 bytes for authnsvc, 2 for authzsvc , each character is 2 bytes (short) and last 2 bytes for null termination
        securityBinding.length = 2 + 2 + securityBinding.princName.length () * 2 + 2;

        return securityBinding;
    }

    public void encode ( final NetworkDataRepresentation ndr )
    {
        ndr.writeUnsignedShort ( this.authnSvc );
        ndr.writeUnsignedShort ( this.authzSvc );

        //now to write the network address.
        int i = 0;
        while ( i < this.princName.length () )
        {
            ndr.writeUnsignedShort ( this.princName.charAt ( i ) );
            i++;
        }

        ndr.writeUnsignedShort ( 0 ); //null termination

    }
}
