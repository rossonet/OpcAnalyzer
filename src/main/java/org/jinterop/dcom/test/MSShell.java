package org.jinterop.dcom.test;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.common.JISystem;
import org.jinterop.dcom.core.*;
import org.jinterop.dcom.impls.JIObjectFactory;

import java.net.UnknownHostException;

public class MSShell
{

    JISession session = null;

    JIComServer comServer = null;

    MSShell ( final String args[] ) throws UnknownHostException, JIException
    {
        this.session = JISession.createSession ( args[1], args[2], args[3] );
        this.comServer = new JIComServer ( JIProgId.valueOf ( "Shell.Application" ), args[0], this.session );
    }

    void doStuff () throws JIException
    {
        //this will return a reference to the IUnknown of the Shell coclass.
        final IJIComObject comUnknown = this.comServer.createInstance ();

        //now we query for the IShellDispatch interface
        final IJIComObject shellDispatch = comUnknown.queryInterface ( "D8F015C0-C278-11CE-A49E-444553540000" );

        JICallBuilder callObject = new JICallBuilder ();
        //		callObject.setOpnum(5);
        //		callObject.addInParamAsVariant(new JIVariant(new JIString("c:")),JIFlags.FLAG_NULL);
        //		Object result[] = shellDispatch.call(callObject);

        //		callObject.reInit();
        //		callObject.setOpnum(7);
        //		result = shellDispatch.call(callObject);

        callObject.reInit ();
        callObject.setOpnum ( 2 );
        callObject.addInParamAsVariant ( new JIVariant ( 2 ), JIFlags.FLAG_NULL );
        callObject.addOutParamAsType ( IJIComObject.class, JIFlags.FLAG_NULL );
        Object[] result = shellDispatch.call ( callObject );
        final IJIComObject folder = JIObjectFactory.narrowObject ( (IJIComObject)result[0] );

        callObject = new JICallBuilder ();
        callObject.setOpnum ( 0 );
        callObject.addOutParamAsObject ( new JIString ( JIFlags.FLAG_REPRESENTATION_STRING_BSTR ), JIFlags.FLAG_NULL );
        result = folder.call ( callObject );
        System.out.println ( "Current Folder: " + result[0] );

        callObject.reInit ();
        callObject.setOpnum ( 1 );
        callObject.addOutParamAsType ( IJIComObject.class, JIFlags.FLAG_NULL );
        result = folder.call ( callObject );
        IJIComObject test = JIObjectFactory.narrowObject ( (IJIComObject)result[0] );

        //		Not implemented by shell
        //		callObject.reInit();
        //		callObject.setOpnum(2);
        //		callObject.addOutParamAsType(JIInterfacePointer.class,JIFlags.FLAG_NULL);
        //		result = folder.call(callObject);
        //		test = JIObjectFactory.createCOMInstance(shellDispatch,(JIInterfacePointer)result[0]);

        callObject.reInit ();
        callObject.setOpnum ( 3 );
        callObject.addOutParamAsType ( IJIComObject.class, JIFlags.FLAG_NULL );
        result = folder.call ( callObject );
        test = JIObjectFactory.narrowObject ( (IJIComObject)result[0] );

        callObject.reInit ();
        callObject.setOpnum ( 4 );
        callObject.addOutParamAsType ( IJIComObject.class, JIFlags.FLAG_NULL );
        result = folder.call ( callObject );
        final IJIComObject folderItems = JIObjectFactory.narrowObject ( (IJIComObject)result[0] );

        callObject = new JICallBuilder ();
        callObject.setOpnum ( 0 );
        callObject.addOutParamAsType ( Integer.class, JIFlags.FLAG_NULL );
        result = folderItems.call ( callObject );

        final int count = ( (Integer)result[0] ).intValue ();

        for ( int i = 0; i < count; i++ )
        {
            callObject.reInit ();
            callObject.setOpnum ( 3 );
            callObject.addInParamAsVariant ( new JIVariant ( i ), JIFlags.FLAG_NULL );
            callObject.addOutParamAsType ( IJIComObject.class, JIFlags.FLAG_NULL );
            result = folderItems.call ( callObject );
            final IJIComObject folderItem = JIObjectFactory.narrowObject ( (IJIComObject)result[0] );

            JICallBuilder callObject2 = new JICallBuilder ();
            callObject2.setOpnum ( 2 );
            callObject2.addOutParamAsObject ( new JIString ( JIFlags.FLAG_REPRESENTATION_STRING_BSTR ), JIFlags.FLAG_NULL );
            result = folderItem.call ( callObject2 );
            System.out.println ( "Name of Object: " + result[0] );

            callObject2.reInit ();
            callObject2.setOpnum ( 4 );
            callObject2.addOutParamAsObject ( new JIString ( JIFlags.FLAG_REPRESENTATION_STRING_BSTR ), JIFlags.FLAG_NULL );
            result = folderItem.call ( callObject2 );
            System.out.println ( "Path of the Object: " + result[0] );

            callObject2.reInit ();
            callObject2 = new JICallBuilder ();
            callObject2.setOpnum ( 9 );
            //VARIANT_BOOL is Boolean
            callObject2.addOutParamAsType ( Boolean.class, JIFlags.FLAG_NULL );
            result = folderItem.call ( callObject2 );

            final boolean isFileSystemObject = ( (Boolean)result[0] ).booleanValue ();

            if ( isFileSystemObject )
            {
                System.out.print ( " and is part of file system\n" );
            }
            else
            {
                System.out.print ( " and is not part of file system\n" );
            }

            callObject2.reInit ();
            callObject2 = new JICallBuilder ();
            callObject2.setOpnum ( 13 );
            callObject2.addOutParamAsObject ( Integer.class, JIFlags.FLAG_NULL );
            result = folderItem.call ( callObject2 );
            System.out.print ( " and size(in bytes) is: " + ( (Integer)result[0] ).intValue () + "\n" );

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
            final MSShell shell = new MSShell ( args );
            shell.doStuff ();
            JISession.destroySession ( shell.session );
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
