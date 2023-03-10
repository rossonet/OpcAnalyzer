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
import org.jinterop.dcom.impls.automation.IJIDispatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Class representing a Marshalled Interface Pointer. You will never use the
 * members of this class directly, but always as an implementation of
 * <code>IJIComObject</code> interface. <br>
 * Sample Usage:- <br>
 * <code>
 * IJIComObject connectionPointContainer = (IJIComObject)ieObject.queryInterface("B196B284-BAB4-101A-B69C-00AA00341D07"); <br>
 * JICallBuilder object = new JICallBuilder(connectionPointContainer.getIpid(),true); <br>
 * object.setOpnum(1); <br>
 * object.addInParamAsUUID("34A715A0-6587-11D0-924A-0020AFC7AC4D",JIFlags.FLAG_NULL); <br>
 * object.addOutParamAsObject(JIInterfacePointer.class,JIFlags.FLAG_NULL); <br>
 * Object[] objects = (Object[])connectionPointContainer.call(object); //find connection point <br>
 * JIInterfacePointer connectionPtr = (JIInterfacePointer)objects[0]; <br>
 * IJIComObject connectionPointer = JIObjectFactory.createCOMInstance(connectionPointContainer,connectionPtr); <br>
 * </code>
 * </p>
 * 
 * @since 1.0
 */
final class JIInterfacePointer implements Serializable
{

    private final static Logger logger = LoggerFactory.getLogger ( JIInterfacePointer.class );

    //	static boolean inTest = true;

    private JIPointer member = null;

    private static final long serialVersionUID = 2508592294719469453L;

    static final byte[] OBJREF_SIGNATURE = { 0x4d, 0x45, 0x4f, 0x57 }; // 'MEOW'

    static final int OBJREF_STANDARD = 0x1; // standard marshaled objref

    static final int OBJREF_HANDLER = 0x2; // handler marshaled objref

    static final int OBJREF_CUSTOM = 0x4; // custom marshaled objref

    // Flag values for a STDOBJREF (standard part of an OBJREF).
    // SORF_OXRES1 - SORF_OXRES8 are reserved for the object exporters
    // use only, object importers must ignore them and must not enforce MBZ.
    static final int SORF_OXRES1 = 0x1; // reserved for exporter

    static final int SORF_OXRES2 = 0x20; // reserved for exporter

    static final int SORF_OXRES3 = 0x40; // reserved for exporter

    static final int SORF_OXRES4 = 0x80; // reserved for exporter

    static final int SORF_OXRES5 = 0x100;// reserved for exporter

    static final int SORF_OXRES6 = 0x200;// reserved for exporter

    static final int SORF_OXRES7 = 0x400;// reserved for exporter

    static final int SORF_OXRES8 = 0x800;// reserved for exporter

    static final int SORF_NULL = 0x0; // convenient for initializing SORF

    static final int SORF_NOPING = 0x1000;// Pinging is not required

    private JIInterfacePointer ()
    {
    }

    /**
     * Called from Oxid Resolver master, the resolver address are put in here
     * itself
     * 
     * @param iid
     * @param ipid
     * @param oxid
     * @param oid
     */
    JIInterfacePointer ( final String iid, final int port, final JIStdObjRef objref )
    {
        this.member = new JIPointer ( new JIInterfacePointerBody ( iid, port, objref ), false );
    }

    JIInterfacePointer ( final String iid, final JIInterfacePointer interfacePointer )
    {
        this.member = new JIPointer ( new JIInterfacePointerBody ( iid, interfacePointer ), false );
    }

    void setDeffered ( final boolean deffered )
    {
        this.member.setDeffered ( true );
    }

    static JIInterfacePointer decode ( final NetworkDataRepresentation ndr, final List defferedPointers, final int FLAG, final Map additionalData )
    {
        JIInterfacePointer ptr = new JIInterfacePointer ();
        if ( ( FLAG & JIFlags.FLAG_REPRESENTATION_INTERFACEPTR_DECODE2 ) == JIFlags.FLAG_REPRESENTATION_INTERFACEPTR_DECODE2 )
        {
            ptr.member = (JIPointer)JIMarshalUnMarshalHelper.deSerialize ( ndr, new JIPointer ( JIInterfacePointerBody.class, true ), defferedPointers, FLAG, additionalData );
        }
        else
        {
            ptr.member = (JIPointer)JIMarshalUnMarshalHelper.deSerialize ( ndr, new JIPointer ( JIInterfacePointerBody.class ), defferedPointers, FLAG, additionalData );
        }
        //the pointer is null, no point of it's wrapper being present, so return null from here as well
        if ( ptr.member.isNull () )
        {
            ptr = null;
        }
        return ptr;
    }

    /**
     * @exclude
     * @return
     */
    int getObjectType ()
    {
        return ( (JIInterfacePointerBody)this.member.getReferent () ).getObjectType ();
    }

    /**
     * @exclude
     * @param objectType
     * @return
     */
    Object getObjectReference ( final int objectType )
    {
        return ( (JIInterfacePointerBody)this.member.getReferent () ).getObjectReference ( objectType );
    }

    /**
     * Returns the Interface Identifier for this MIP.
     * 
     * @return String representation of 128 bit uuid.
     */
    public String getIID ()
    {
        return ( (JIInterfacePointerBody)this.member.getReferent () ).getIID ();
    }

    /**
     * @exclude
     * @return
     */
    public String getIPID ()
    {
        return ( (JIInterfacePointerBody)this.member.getReferent () ).getIPID ();
    }

    /**
     * @exclude
     * @return
     */
    public byte[] getOID ()
    {
        return ( (JIStdObjRef) ( (JIInterfacePointerBody)this.member.getReferent () ).getObjectReference ( JIInterfacePointer.OBJREF_STANDARD ) ).getObjectId ();
    }

    /**
     * @exclude
     * @return
     */
    byte[] getOXID ()
    {
        return ( (JIStdObjRef) ( (JIInterfacePointerBody)this.member.getReferent () ).getObjectReference ( JIInterfacePointer.OBJREF_STANDARD ) ).getOxid ();
    }

    /**
     * @exclude
     * @return
     */
    JIDualStringArray getStringBindings ()
    {
        return ( (JIInterfacePointerBody)this.member.getReferent () ).getStringBindings ();
    }

    /**
     * @exclude
     * @return
     */
    int getLength ()
    {
        return ( (JIInterfacePointerBody)this.member.getReferent () ).getLength ();
    }

    void encode ( final NetworkDataRepresentation ndr, final List defferedPointers, final int FLAG )
    {

        if ( ( FLAG & JIFlags.FLAG_REPRESENTATION_SET_JIINTERFACEPTR_NULL_FOR_VARIANT ) == JIFlags.FLAG_REPRESENTATION_SET_JIINTERFACEPTR_NULL_FOR_VARIANT )
        {
            //just encode a null.
            JIMarshalUnMarshalHelper.serialize ( ndr, Integer.class, new Integer ( 0 ), defferedPointers, FLAG );
            return;
        }
        JIMarshalUnMarshalHelper.serialize ( ndr, this.member.getClass (), this.member, defferedPointers, FLAG );
    }

    @Override
    public String toString ()
    {
        final String retVal = "JIInterfacePointer[IID:" + getIID () + " , ObjRef: " + getObjectReference ( JIInterfacePointer.OBJREF_STANDARD ) + "]";
        return retVal;
    }

    public static boolean isOxidEqual ( final JIInterfacePointer src, final JIInterfacePointer target )
    {
        if ( src == null || target == null )
        {
            throw new NullPointerException ();
        }

        return Arrays.equals ( src.getOXID (), target.getOXID () );
    }

    //    public static void main(String[] args) {
    //
    //
    //		byte[] buffer = new byte[183];
    //		FileInputStream inputStream;
    //		try {
    //			inputStream = new FileInputStream("F:/tmp/experiment/rawip2");
    //			inputStream.read(new byte[13],0,13);
    //			inputStream.read(buffer,0,183);
    //		} catch (Exception e) {
    //			// TODO Auto-generated catch block
    //			e.printStackTrace();
    //		}
    //		NetworkDataRepresentation ndr = new NetworkDataRepresentation();
    //		NdrBuffer ndrBuffer = new NdrBuffer(buffer,0);
    //		ndr.setBuffer(ndrBuffer);
    //		ndrBuffer.length = 183;
    //
    //    	JIInterfacePointer ptr = JIInterfacePointer.decode(ndr, new ArrayList(), 0, new HashMap());
    //    	try {
    //    		JISystem.getLogger().setLevel(Level.FINEST);
    //			JISystem.setInBuiltLogHandler(false);
    //
    //		} catch (SecurityException e1) {
    //			// TODO Auto-generated catch block
    //			e1.printStackTrace();
    //		} catch (IOException e1) {
    //			// TODO Auto-generated catch block
    //			e1.printStackTrace();
    //		}
    //
    //    	JISession session = JISession.createSession("deepspace9", "administrator", "enterprise");
    //    	session.useSessionSecurity(true);
    //    	try {
    //    		JIComServer comServer = new JIComServer(session,ptr,null);
    //			IJIComObject comObject = comServer.getInstance();
    //			comObject.queryInterface("87bc18dc-c8b3-11d5-ae96-00b0d0e93ca1");
    //		} catch (JIException e) {
    //			// TODO Auto-generated catch block
    //			e.printStackTrace();
    //		}
    //	}

}

/**
 * @exclude
 */
class JIInterfacePointerBody implements Serializable
{
    private final static Logger logger = LoggerFactory.getLogger ( JIInterfacePointerBody.class );

    private static final long serialVersionUID = 2597456459096838320L;

    private String iid = null;

    private int objectType = -1;

    private JIStdObjRef stdObjRef = null;

    private int length = -1;

    private JIDualStringArray resolverAddr = null;

    private int port = -1; //to be used when doing local resolution.

    private JIInterfacePointerBody ()
    {
    }

    /**
     * Called from Oxid Resolver master, the resolver address are put in here
     * itself
     * 
     * @param iid
     * @param ipid
     * @param oxid
     * @param oid
     */
    JIInterfacePointerBody ( final String iid, final int port, final JIStdObjRef objref )
    {
        this.iid = iid;
        this.stdObjRef = objref;
        this.port = port;
        this.resolverAddr = new JIDualStringArray ( port );
        this.length = 40 + 4 + 4 + 16 + this.resolverAddr.getLength ();
    }

    JIInterfacePointerBody ( final String iid, final JIInterfacePointer interfacePointer )
    {
        this.iid = iid;
        this.stdObjRef = (JIStdObjRef)interfacePointer.getObjectReference ( JIInterfacePointer.OBJREF_STANDARD );
        this.resolverAddr = interfacePointer.getStringBindings ();
        this.length = 40 + 4 + 4 + 16 + this.resolverAddr.getLength ();
    }

    static JIInterfacePointerBody decode ( final NetworkDataRepresentation ndr, final int Flags )
    {
        if ( ( Flags & JIFlags.FLAG_REPRESENTATION_INTERFACEPTR_DECODE2 ) == JIFlags.FLAG_REPRESENTATION_INTERFACEPTR_DECODE2 )
        {
            return decode2 ( ndr );
        }
        final int length = ndr.readUnsignedLong ();
        ndr.readUnsignedLong ();//length

        final JIInterfacePointerBody ptr = new JIInterfacePointerBody ();
        ptr.length = length;
        //check for MEOW
        final byte b[] = new byte[4];
        ndr.readOctetArray ( b, 0, 4 );

        int i = 0;
        while ( i != 4 )
        {
            //not MEOW then what ?
            if ( b[i] != JIInterfacePointer.OBJREF_SIGNATURE[i] )
            {
                return null;
            }
            i++;
        }

        //TODO only STDOBJREF supported for now

        if ( ( ptr.objectType = ndr.readUnsignedLong () ) != JIInterfacePointer.OBJREF_STANDARD )
        {
            return null;
        }

        try
        {
            final rpc.core.UUID ipid2 = new rpc.core.UUID ();
            ipid2.decode ( ndr, ndr.getBuffer () );
            ptr.iid = ipid2.toString ();
        }
        catch ( final NdrException e )
        {
            logger.warn ( "decode", e );
        }

        ptr.stdObjRef = JIStdObjRef.decode ( ndr );

        ptr.resolverAddr = JIDualStringArray.decode ( ndr );

        return ptr;
    }

    static JIInterfacePointerBody decode2 ( final NetworkDataRepresentation ndr )
    {

        final JIInterfacePointerBody ptr = new JIInterfacePointerBody ();

        //check for MEOW
        final byte b[] = new byte[4];
        ndr.readOctetArray ( b, 0, 4 );

        int i = 0;
        while ( i != 4 )
        {
            //not MEOW then what ?
            if ( b[i] != JIInterfacePointer.OBJREF_SIGNATURE[i] )
            {
                return null;
            }
            i++;
        }

        //TODO only STDOBJREF supported for now

        if ( ( ptr.objectType = ndr.readUnsignedLong () ) != JIInterfacePointer.OBJREF_STANDARD )
        {
            return null;
        }

        try
        {
            final rpc.core.UUID ipid2 = new rpc.core.UUID ();
            ipid2.decode ( ndr, ndr.getBuffer () );
            ptr.iid = ipid2.toString ();
        }
        catch ( final NdrException e )
        {
            logger.warn ( "decode", e );
        }

        ptr.stdObjRef = JIStdObjRef.decode ( ndr );

        ptr.resolverAddr = JIDualStringArray.decode ( ndr );

        return ptr;
    }

    /**
     * @exclude
     * @return
     */
    int getObjectType ()
    {
        return this.objectType;
    }

    /**
     * @exclude
     * @param objectType
     * @return
     */
    Object getObjectReference ( final int objectType )
    {
        if ( objectType == JIInterfacePointer.OBJREF_STANDARD )
        {
            return this.stdObjRef;
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns the Interface Identifier for this MIP.
     * 
     * @return String representation of 128 bit uuid.
     */
    String getIID ()
    {
        return this.iid;
    }

    /**
     * @exclude
     * @return
     */
    String getIPID ()
    {
        return this.stdObjRef.getIpid ();
    }

    /**
     * @exclude
     * @return
     */
    byte[] getOID ()
    {
        return this.stdObjRef.getObjectId ();
    }

    /**
     * @exclude
     * @return
     */
    JIDualStringArray getStringBindings ()
    {
        return this.resolverAddr;
    }

    /**
     * @exclude
     * @return
     */
    int getLength ()
    {
        return this.length;
    }

    void encode ( final NetworkDataRepresentation ndr, final int FLAGS )
    {

        //now for length
        //the length for STDOBJREF is fixed 40 bytes : 4,4,8,8,16.
        //Dual string array has to be computed, since that can vary. MEOW = 4., flag stdobjref = 4
        // + 16 bytes of ipid
        final int length = 40 + 4 + 4 + 16 + this.resolverAddr.getLength ();

        ndr.writeUnsignedLong ( length );
        ndr.writeUnsignedLong ( length );

        ndr.writeOctetArray ( JIInterfacePointer.OBJREF_SIGNATURE, 0, 4 );
        //std ref
        ndr.writeUnsignedLong ( JIInterfacePointer.SORF_OXRES1 );

        try
        {
            rpc.core.UUID ipid2 = new rpc.core.UUID ( this.iid );

            if ( ( FLAGS & JIFlags.FLAG_REPRESENTATION_USE_IUNKNOWN_IID ) == JIFlags.FLAG_REPRESENTATION_USE_IUNKNOWN_IID )
            {
                ipid2 = new rpc.core.UUID ( IJIComObject.IID );
            }
            else if ( ( FLAGS & JIFlags.FLAG_REPRESENTATION_USE_IDISPATCH_IID ) == JIFlags.FLAG_REPRESENTATION_USE_IDISPATCH_IID )
            {
                ipid2 = new rpc.core.UUID ( IJIDispatch.IID );
            }

            ipid2.encode ( ndr, ndr.getBuffer () );
        }
        catch ( final NdrException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }

        this.stdObjRef.encode ( ndr );

        this.resolverAddr.encode ( ndr );

    }

}