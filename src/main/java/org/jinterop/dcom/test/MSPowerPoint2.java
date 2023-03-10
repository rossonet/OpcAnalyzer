package org.jinterop.dcom.test;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.*;
import org.jinterop.dcom.impls.JIObjectFactory;
import org.jinterop.dcom.impls.automation.IJIDispatch;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

public class MSPowerPoint2
{

    private JIComServer comStub = null;

    private IJIDispatch dispatch = null;

    private IJIComObject unknown = null;

    public MSPowerPoint2 ( final String address, final String[] args ) throws JIException, UnknownHostException
    {
        final JISession session = JISession.createSession ( args[1], args[2], args[3] );
        this.comStub = new JIComServer ( JIProgId.valueOf ( "PowerPoint.Application" ), address, session );
    }

    public void startPowerPoint () throws JIException
    {
        this.unknown = this.comStub.createInstance ();
        this.dispatch = (IJIDispatch)JIObjectFactory.narrowObject ( this.unknown.queryInterface ( IJIDispatch.IID ) );
    }

    public void showPowerPoint () throws JIException
    {
        final int dispId = this.dispatch.getIDsOfNames ( "Visible" );
        final JIVariant variant = new JIVariant ( -1 );
        this.dispatch.put ( dispId, variant );
    }

    public IJIDispatch openPresentation ( final String fullEscapedPath ) throws JIException, InterruptedException
    {
        final IJIDispatch presentations = (IJIDispatch)JIObjectFactory.narrowObject ( this.dispatch.get ( "Presentations" ).getObjectAsComObject () );
        final JIVariant[] result = presentations.callMethodA ( "Open", new Object[] { new JIString ( fullEscapedPath ), JIVariant.OPTIONAL_PARAM (), JIVariant.OPTIONAL_PARAM (), JIVariant.OPTIONAL_PARAM () } );
        return (IJIDispatch)JIObjectFactory.narrowObject ( result[0].getObjectAsComObject () );
    }

    public IJIDispatch runPresentation ( final IJIDispatch activePresentation ) throws JIException
    {
        final IJIDispatch slideShowSettings = (IJIDispatch)JIObjectFactory.narrowObject ( activePresentation.get ( "SlideShowSettings" ).getObjectAsComObject () );
        System.out.println ( "Running Slide show : " + activePresentation.get ( "Name" ).getObjectAsString ().getString () );
        final IJIDispatch slideShowWindow = (IJIDispatch)JIObjectFactory.narrowObject ( slideShowSettings.callMethodA ( "Run" ).getObjectAsComObject () );
        final IJIDispatch slideShowView = (IJIDispatch)JIObjectFactory.narrowObject ( slideShowWindow.get ( "View" ).getObjectAsComObject () );
        return slideShowView;
    }

    public void quitPowerPoint () throws JIException
    {
        this.dispatch.callMethod ( "Quit" );
        JISession.destroySession ( this.dispatch.getAssociatedSession () );
    }

    public void closePresentation ( final IJIDispatch presentation ) throws JIException
    {
        presentation.callMethod ( "Close" );
    }

    public void savePresentationAs ( final IJIDispatch presentation, final String fullEscapedPath ) throws JIException
    {
        presentation.callMethod ( "SaveAs", new Object[] { new JIString ( fullEscapedPath ).Variant, JIVariant.OPTIONAL_PARAM (), new Integer ( -1 ) } );
    }

    public void goto_First_Slide ( final IJIDispatch view ) throws JIException
    {
        view.callMethod ( "First" );
    }

    public void goto_Last_Slide ( final IJIDispatch view ) throws JIException
    {
        view.callMethod ( "Last" );
    }

    public void do_Next_Action ( final IJIDispatch view ) throws JIException
    {
        view.callMethod ( "Next" );
    }

    public void do_Previous_Action ( final IJIDispatch view ) throws JIException
    {
        view.callMethod ( "Previous" );
    }

    public void goto_Numbered_Slide ( final IJIDispatch view, final int index ) throws JIException
    {
        view.callMethod ( "GotoSlide", new Object[] { new Integer ( index ), JIVariant.OPTIONAL_PARAM () } );
    }

    public static void main ( final String[] args )
    {

        try
        {
            if ( args.length < 4 )
            {
                System.out.println ( "Please provide address domain username password" );
                return;
            }
            final MSPowerPoint2 test = new MSPowerPoint2 ( args[0], args );
            test.startPowerPoint ();
            test.showPowerPoint ();

            System.out.println ( "Welcome to PowerPoint Manager !" );
            System.out.println ( "Commands --> " );
            System.out.println ( "'O' <path_to_ppt>               Open PPT, ex:- O c:\\temp\\j-Interop.ppt" );
            System.out.println ( "'C'  							Close PPT" );
            System.out.println ( "'N'  							Next Action" );
            System.out.println ( "'P'  							Previous Action" );
            System.out.println ( "'G' <slide number>              Goto Slide, ex:- G 3" );
            System.out.println ( "'F' 							First Slide" );
            System.out.println ( "'L' 							Last Slide" );
            System.out.println ( "'Q' 				  			Quit PowerPoint Manager" );

            final BufferedReader inputreader = new BufferedReader ( new InputStreamReader ( new BufferedInputStream ( System.in ) ) );

            final String commands = "OCNPGFLQ";
            IJIDispatch activePresentation = null;
            IJIDispatch view = null;
            boolean over = false;
            while ( !over )
            {
                final String input = inputreader.readLine ().trim ();
                if ( input.equalsIgnoreCase ( "" ) )
                {
                    continue;
                }
                int index = -1;
                String command = null;

                if ( input.length () > 1 )
                {
                    index = input.indexOf ( " " );
                    command = input.substring ( 0, index );
                }
                else
                {
                    command = input;
                }

                switch ( commands.indexOf ( command ) )
                {
                    case 0:
                        String path = input.substring ( index++ ).trim ();
                        activePresentation = test.openPresentation ( path );
                        view = test.runPresentation ( activePresentation );
                        break;
                    case 1:
                        if ( activePresentation == null )
                        {
                            System.out.println ( "Please open a presentation first !" );
                        }
                        else
                        {
                            test.closePresentation ( activePresentation );
                            activePresentation = null;
                        }
                        break;
                    case 2:
                        if ( activePresentation == null )
                        {
                            System.out.println ( "Please open a presentation first !" );
                        }
                        else
                        {
                            test.do_Next_Action ( view );
                        }
                        break;
                    case 3:
                        if ( activePresentation == null )
                        {
                            System.out.println ( "Please open a presentation first !" );
                        }
                        else
                        {
                            test.do_Previous_Action ( view );
                        }
                        break;
                    case 4:
                        path = input.substring ( index++ ).trim ();
                        if ( activePresentation == null )
                        {
                            System.out.println ( "Please open a presentation first !" );
                        }
                        else
                        {
                            test.goto_Numbered_Slide ( view, Integer.valueOf ( path ).intValue () );
                        }

                        break;
                    case 5:
                        if ( activePresentation == null )
                        {
                            System.out.println ( "Please open a presentation first !" );
                        }
                        else
                        {
                            test.goto_First_Slide ( view );
                        }

                        break;
                    case 6:
                        if ( activePresentation == null )
                        {
                            System.out.println ( "Please open a presentation first !" );
                        }
                        else
                        {
                            test.goto_Last_Slide ( view );
                        }

                        break;
                    case 7:
                        test.quitPowerPoint ();
                        over = true;
                        break;
                    default:
                        System.out.println ( "Incorrect option !" );
                }

            }

        }
        catch ( final Exception e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }
    }

}
