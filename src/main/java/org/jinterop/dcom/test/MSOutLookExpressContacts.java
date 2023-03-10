package org.jinterop.dcom.test;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.common.JISystem;
import org.jinterop.dcom.core.*;
import org.jinterop.dcom.impls.JIObjectFactory;
import org.jinterop.dcom.impls.automation.IJIDispatch;

import java.net.UnknownHostException;

public class MSOutLookExpressContacts
{

    JISession session = null;

    JIComServer comServer = null;

    MSOutLookExpressContacts ( final String args[] ) throws UnknownHostException, JIException
    {
        this.session = JISession.createSession ( args[1], args[2], args[3] );
        this.comServer = new JIComServer ( JIProgId.valueOf ( "Outlook.Application" ), args[0], this.session );
    }

    void doStuff () throws JIException
    {
        final IJIComObject unknown = this.comServer.createInstance ();
        final IJIComObject application = unknown.queryInterface ( "00063001-0000-0000-C000-000000000046" );

        JICallBuilder callObject = new JICallBuilder ( !application.isDispatchSupported () );
        callObject.setOpnum ( 12 );
        callObject.addInParamAsString ( "MAPI", JIFlags.FLAG_REPRESENTATION_STRING_BSTR );
        callObject.addOutParamAsType ( IJIComObject.class, JIFlags.FLAG_NULL );
        Object[] res = application.call ( callObject );

        final IJIComObject namespace = JIObjectFactory.narrowObject ( (IJIComObject)res[0] );
        callObject = new JICallBuilder ();
        callObject.setOpnum ( 16 );
        callObject.addOutParamAsType ( IJIComObject.class, JIFlags.FLAG_NULL );
        res = namespace.call ( callObject );

        if ( res[0] == null )
        {
            System.out.println ( "user cancelled request" );
            return;
        }

        final IJIComObject folder = JIObjectFactory.narrowObject ( (IJIComObject)res[0] );
        callObject = new JICallBuilder ();
        callObject.setOpnum ( 4 );
        callObject.addOutParamAsType ( Integer.class, JIFlags.FLAG_NULL );
        res = folder.call ( callObject );

        if ( ( (Integer)res[0] ).intValue () != 2 )
        {
            System.out.println ( "Invalid folder selected, this is not a \"contact\" folder , please reselect.." );
            return;
        }

        callObject.reInit ();
        callObject.setOpnum ( 10 );
        callObject.addOutParamAsType ( IJIComObject.class, JIFlags.FLAG_NULL );
        res = folder.call ( callObject );
        if ( res[0] == null )
        {
            System.out.println ( "Unable to get Contact Items." );
            return;
        }

        final IJIComObject items = JIObjectFactory.narrowObject ( (IJIComObject)res[0] );
        callObject = new JICallBuilder ();
        callObject.setOpnum ( 12 );
        callObject.addOutParamAsType ( IJIComObject.class, JIFlags.FLAG_NULL );
        res = items.call ( callObject );

        while ( true )
        {
            if ( res[0] == null )
            {
                break;
            }

            String details = null;
            final IJIDispatch contactItem = (IJIDispatch)JIObjectFactory.narrowObject ( (IJIComObject)res[0] );
            JIVariant res2 = contactItem.get ( "FullName" );
            //			callObject = new JICallBuilder(contactItem.getIpid());
            //			callObject.setOpnum(124);
            //			callObject.addOutParamAsObject(new JIString(JIFlags.FLAG_REPRESENTATION_STRING_BSTR),JIFlags.FLAG_NULL);
            //			res = contactItem.call(callObject);
            details = res2.getObjectAsString ().getString ();

            //			callObject.reInit();
            //			callObject.setOpnum(100);
            //			callObject.addOutParamAsObject(new JIString(JIFlags.FLAG_REPRESENTATION_STRING_BSTR),JIFlags.FLAG_NULL);
            //			res = contactItem.call(callObject);
            res2 = contactItem.get ( "Email1Address" );
            details = details + "<" + res2.getObjectAsString ().getString () + ">";

            System.out.println ( details );

            callObject = new JICallBuilder ();
            callObject.setOpnum ( 14 );
            callObject.addOutParamAsType ( IJIComObject.class, JIFlags.FLAG_NULL );
            res = items.call ( callObject );
        }

    }

    public static void main ( final String[] args )
    {
        if ( args.length < 4 )
        {
            System.out.println ( "Please provide address domain username password" );
            return;
        }
        JISystem.setAutoRegisteration ( true );
        try
        {
            final MSOutLookExpressContacts outlookMessages = new MSOutLookExpressContacts ( args );
            outlookMessages.doStuff ();
            JISession.destroySession ( outlookMessages.session );
        }
        catch ( final UnknownHostException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }
        catch ( final JIException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }

    }

}
