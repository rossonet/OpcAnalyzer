package org.jinterop.dcom.test;

import org.jinterop.dcom.common.JISystem;
import org.jinterop.dcom.core.*;
import org.jinterop.dcom.impls.JIObjectFactory;
import org.jinterop.dcom.impls.automation.IJIDispatch;

public class Test_ITestServer2_Impl
{

    public void Execute ( final JIString str )
    {
        System.out.println ( str.getString () );
    }

    /**
     * @param args
     */
    public static void main ( final String[] args )
    {

        if ( args.length < 4 )
        {
            System.out.println ( "Please provide address domain username password" );
            return;
        }

        try
        {

            JISystem.setAutoRegisteration ( true );
            JISystem.setInBuiltLogHandler ( false );
            // JR: JISystem.getLogger ().setLevel ( Level.ALL );
            // JR: configure using slf4j now
            final JISession session1 = JISession.createSession ( args[1], args[2], args[3] );
            final JISession session2 = JISession.createSession ( args[1], args[2], args[3] );
            final JIComServer testServer1 = new JIComServer ( JIProgId.valueOf ( "TestJavaServer.TestServer1" ), args[0], session1 );
            final IJIComObject unkTestServer1 = testServer1.createInstance ();
            final IJIComObject testServer1Intf = JIObjectFactory.narrowObject ( unkTestServer1.queryInterface ( "2A93A24D-59FE-4DE0-B67E-B8D41C9F57F8" ) );
            final IJIDispatch dispatch1 = (IJIDispatch)JIObjectFactory.narrowObject ( unkTestServer1.queryInterface ( IJIDispatch.IID ) );
            ;

            //First lets call the ITestServer1.Call_TestServer2_Java using the Dispatch interface
            //Acquire a reference to ITestServer2
            final JIComServer testServer2 = new JIComServer ( JIProgId.valueOf ( "TestJavaServer.TestServer2" ), args[0], session2 );
            final IJIComObject unkTestServer2 = testServer2.createInstance ();
            //Get the interface pointer to ITestServer2
            final IJIComObject iTestServer2 = JIObjectFactory.narrowObject ( unkTestServer2.queryInterface ( "9CCC5120-457D-49F3-8113-90F7E97B54A7" ) );

            final IJIDispatch dispatch2 = (IJIDispatch)JIObjectFactory.narrowObject ( unkTestServer2.queryInterface ( IJIDispatch.IID ) );
            ;

            //send it directly without IDispatch interface, please note that the "dispatchNotSupported" flag of JICallBuilder is "false".
            final JICallBuilder callObject = new JICallBuilder ( false );
            callObject.addInParamAsComObject ( iTestServer2, JIFlags.FLAG_NULL );
            callObject.setOpnum ( 0 );
            testServer1Intf.call ( callObject );

            //Send it to ITestServer.Call_TestServer2_Java2 via IDispatch of ITestServer1. Notice that pointer here id IDispatch.
            dispatch1.callMethod ( "Call_TestServer2_Java2", new Object[] { new JIVariant ( dispatch2 ) } );

            //Send it to ITestServer.Call_TestServer2_Java via IDispatch of ITestServer1.
            dispatch1.callMethod ( "Call_TestServer2_Java", new Object[] { new JIVariant ( iTestServer2 ) } );

            //Now for the Java Implementation of ITestServer2 interface (from the type library or IDL)  
            //IID of ITestServer2 interface
            final JILocalInterfaceDefinition interfaceDefinition = new JILocalInterfaceDefinition ( "9CCC5120-457D-49F3-8113-90F7E97B54A7" );
            //lets define the method "Execute" now. Please note that either this should be in the same order as defined in IDL
            //or use the addInParamAsObject with opnum parameter function.
            final JILocalParamsDescriptor parameterObject = new JILocalParamsDescriptor ();
            parameterObject.addInParamAsObject ( new JIString ( JIFlags.FLAG_REPRESENTATION_STRING_BSTR ), JIFlags.FLAG_REPRESENTATION_STRING_BSTR );
            final JILocalMethodDescriptor methodDescriptor = new JILocalMethodDescriptor ( "Execute", 1, parameterObject );
            interfaceDefinition.addMethodDescriptor ( methodDescriptor );
            //Create the Java Server class. This contains the instance to be called by the COM Server ITestServer1.
            final JILocalCoClass _testServer2 = new JILocalCoClass ( interfaceDefinition, new Test_ITestServer2_Impl () );
            //Get a interface pointer to the Java CO Class. The template could be any IJIComObject since only the session is reused.
            final IJIComObject __testServer2 = JIObjectFactory.buildObject ( session1, _testServer2 );
            //Call our Java server. The same message should be printed on the Java console.
            dispatch1.callMethod ( "Call_TestServer2_Java", new Object[] { new JIVariant ( __testServer2 ) } );

        }
        catch ( final Exception e )
        {
            e.printStackTrace ();
        }

    }

}
