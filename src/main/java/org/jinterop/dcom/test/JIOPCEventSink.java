package org.jinterop.dcom.test;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class JIOPCEventSink
{
    public static final String OPC_IID = "6516885F-5783-11D1-84A0-00608CB8A7E9";

    private static final String LOCAL_CLASS_IID = "85360DFE-6249-47AB-BE2D-6D68AA325CE8";

    private final Set listeners;

    public JIOPCEventSink ()
    {
        this.listeners = new HashSet ();
    }

    public void addListener ( final EventNotificationListener listener )
    {
        if ( listener == null )
        {
            throw new NullPointerException ( "The listener is null" );
        }
        synchronized ( this.listeners )
        {
            this.listeners.add ( listener );
        }
    }

    public void removeListener ( final EventNotificationListener listener )
    {
        synchronized ( this.listeners )
        {
            this.listeners.remove ( listener );
        }
    }

    /**
     * This method is provided by the client to handle notifications from the
     * OPCEventSubscription for events. This method can be
     * called whether this is a refresh or standard event notification.
     * 
     * @param clientSubscription
     *            The client handle for the subscription object sending the
     *            event notifications.
     * @param refresh
     *            TRUE if this is a subscription refresh.
     * @param lastRefresh
     *            TRUE if this is the last subscription refresh in response to a
     *            specific invocation of the IOPCEventSubscriptionMgt::Refresh
     *            method.
     * @param count
     *            Number of event notifications. A value of zero indicates this
     *            is a keep-alive notification.
     * @param events
     *            Array of event notifications
     * @return
     *         An EMPTY() array.
     * @throws JIException
     */
    public Object[] onEvent ( final int clientSubscription, final int refresh, final int lastRefresh, final int count, final JIArray eventsArray ) throws JIException
    {
        final JIStruct[] events;
        if ( count == 0 )
        {
            events = new JIStruct[0];
        }
        else
        {
            events = (JIStruct[])eventsArray.getArrayInstance ();
        }
        new Thread ( new Runnable () {
            @Override
            public void run ()
            {
                EventNotificationListener[] l;
                synchronized ( JIOPCEventSink.this.listeners )
                {
                    l = (EventNotificationListener[])JIOPCEventSink.this.listeners.toArray ( new EventNotificationListener[JIOPCEventSink.this.listeners.size ()] );
                }
                for ( int i = 0; i < l.length; i++ )
                {
                    l[i].onEvent ( events );
                }
            }
        }, "Opc event sink thread" ).start ();
        return new Object[0];
    }

    /**
     * Create an out struct definition of this object that may be use in a call
     * object
     * 
     * @return
     *         The OPC struct definition
     */
    public static final JIStruct fileTimeOutStruct ()
    {
        final JIStruct struct = new JIStruct ();
        try
        {
            struct.addMember ( Integer.class );//Low date time
            struct.addMember ( Integer.class );//High date time
            return struct;
        }
        catch ( final JIException e )
        {// Can't occur
            throw new RuntimeException ( "Add member error" );
        }
    }

    /**
     * Create an out struct definition of this object that may be use in a call
     * object
     * 
     * @return
     *         The OPC struct definition
     */
    private static final JIStruct outStruct ()
    {
        final JIStruct struct = new JIStruct ();
        try
        {
            struct.addMember ( Short.class );
            struct.addMember ( Short.class );
            struct.addMember ( new JIPointer ( new JIString ( JIFlags.FLAG_REPRESENTATION_STRING_LPWSTR ) ) );
            struct.addMember ( fileTimeOutStruct () );
            struct.addMember ( new JIPointer ( new JIString ( JIFlags.FLAG_REPRESENTATION_STRING_LPWSTR ) ) );
            struct.addMember ( Integer.class );
            struct.addMember ( Integer.class );
            struct.addMember ( Integer.class );
            struct.addMember ( new JIPointer ( new JIString ( JIFlags.FLAG_REPRESENTATION_STRING_LPWSTR ) ) );
            struct.addMember ( new JIPointer ( new JIString ( JIFlags.FLAG_REPRESENTATION_STRING_LPWSTR ) ) );
            struct.addMember ( Short.class );
            struct.addMember ( Short.class );
            struct.addMember ( Integer.class );
            struct.addMember ( fileTimeOutStruct () );
            struct.addMember ( Integer.class );
            struct.addMember ( Integer.class );
            struct.addMember ( new JIPointer ( new JIArray ( JIVariant.class, null, 1, true ) ) );
            struct.addMember ( new JIPointer ( new JIString ( JIFlags.FLAG_REPRESENTATION_STRING_LPWSTR ) ) );
            return struct;
        }
        catch ( final JIException e )
        {// Can't occur
            throw new RuntimeException ( "Add member error" );
        }
    }

    public static final JILocalCoClass getCoClass ( final JIOPCEventSink instance )
    {
        //Define the onEvent method for this interface
        final JILocalParamsDescriptor oeParams = new JILocalParamsDescriptor ();
        oeParams.addInParamAsType ( Integer.class, JIFlags.FLAG_NULL );
        oeParams.addInParamAsType ( Integer.class, JIFlags.FLAG_NULL );
        oeParams.addInParamAsType ( Integer.class, JIFlags.FLAG_NULL );
        oeParams.addInParamAsType ( Integer.class, JIFlags.FLAG_NULL );
        oeParams.addInParamAsObject ( new JIArray ( outStruct (), null, 1, true ), JIFlags.FLAG_NULL );
        final JILocalMethodDescriptor oeMethod = new JILocalMethodDescriptor ( "onEvent", 0, oeParams );
        //This identify the JIOPCEventSink and not the interface
        final JILocalInterfaceDefinition def = new JILocalInterfaceDefinition ( LOCAL_CLASS_IID, false );
        def.addMethodDescriptor ( oeMethod );
        final JILocalCoClass coClass = instance == null ? new JILocalCoClass ( def, JIOPCEventSink.class ) : new JILocalCoClass ( def, instance );
        final ArrayList list = new ArrayList ();
        //Supported interface
        list.add ( OPC_IID );
        coClass.setSupportedEventInterfaces ( list );
        return coClass;
    }
}
