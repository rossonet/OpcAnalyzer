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

//import java.util.UUID;

import org.jinterop.dcom.core.JIPointer;
import org.jinterop.dcom.core.JIStruct;

import java.io.Serializable;

/**
 * Implements the <i>TYPEATTR</i> structure of COM Automation and
 * contains attributes of an IJITypeInfo.
 * 
 * @since 1.0
 */
public final class TypeAttr implements Serializable
{

    private static final long serialVersionUID = -4450777076320962915L;

    /**
     * GUID guid; // The GUID of the type information.
     * LCID lcid; // Locale of member names and doc
     * // strings.
     * unsigned long dwReserved;
     * MEMBERID memidConstructor; // ID of constructor, or MEMBERID_NIL if
     * // none.
     * MEMBERID memidDestructor; // ID of destructor, or MEMBERID_NIL if
     * // none.
     * OLECHAR FAR* lpstrSchema; // Reserved for future use.
     * unsigned long cbSizeInstance;// The size of an instance of
     * // this type.
     * TypeKind typekind; // The kind of type this information
     * // describes.
     * unsigned short cFuncs; // Number of functions.
     * unsigned short cVars; // Number of variables/data members.
     * unsigned short cImplTypes; // Number of implemented interfaces.
     * unsigned short cbSizeVft; // The size of this type's VTBL.
     * unsigned short cbAlignment; // Byte alignment for an instance
     * // of this type.
     * unsigned short wTypeFlags;
     * unsigned short wMajorVerNum; // Major version number.
     * unsigned short wMinorVerNum; // Minor version number.
     * TYPEDESC tdescAlias; // If TypeKind == TKIND_ALIAS,
     * // specifies the type for which
     * // this type is an alias.
     * IDLDESC idldescType; // IDL attributes of the
     * // described type.
     */

    /**
     * The GUID of the type information.
     */
    public final String guid; // The GUID of the type information.

    /**
     * Locale of member names and doc strings.
     */
    public final int lcid; // Locale of member names and doc

    // strings.
    public final int dwReserved;

    /**
     * ID of constructor, or MEMBERID_NIL if none.
     */
    public final int memidConstructor; // ID of constructor, or MEMBERID_NIL if

    // none.
    /**
     * ID of destructor, or MEMBERID_NIL if none.
     */
    public final int memidDestructor; // ID of destructor, or MEMBERID_NIL if

    // none.
    public final JIPointer lpstrSchema; // Reserved for future use.

    /**
     * The size of an instance of this type.
     */
    public final int cbSizeInstance;// The size of an instance of

    // this type.
    /**
     * The kind of type this information describes.
     */
    public final int typekind; // The kind of type this information

    // describes.
    /**
     * Number of functions.
     */
    public final short cFuncs; // Number of functions.

    /**
     * Number of variables/data members.
     */
    public final short cVars; // Number of variables/data members.

    /**
     * Number of implemented interfaces.
     */
    public final short cImplTypes; // Number of implemented interfaces.

    /**
     * The size of this type's VTBL.
     */
    public final short cbSizeVft; // The size of this type's VTBL.

    /**
     * Byte alignment for an instance of this type.
     */
    public final short cbAlignment; // Byte alignment for an instance

    // of this type.
    public final short wTypeFlags;

    /**
     * Major version number.
     */
    public final short wMajorVerNum; // Major version number.

    /**
     * Minor version number.
     */
    public final short wMinorVerNum; // Minor version number.

    /**
     * if TypeKind == TKIND_ALIAS, specifies the type for which this type is an
     * alias.
     */
    public final TypeDesc tdescAlias; // If TypeKind == TKIND_ALIAS,

    // specifies the type for which
    // this type is an alias.
    /**
     * IDL attributes of the described type.
     */
    public final IdlDesc idldescType; // IDL attributes of the

    // described type.

    public static final int TYPEFLAG_FAPPOBJECT = 0x01;

    public static final int TYPEFLAG_FCANCREATE = 0x02;

    public static final int TYPEFLAG_FLICENSED = 0x04;

    public static final int TYPEFLAG_FPREDECLID = 0x08;

    public static final int TYPEFLAG_FHIDDEN = 0x10;

    public static final int TYPEFLAG_FCONTROL = 0x20;

    public static final int TYPEFLAG_FDUAL = 0x40;

    public static final int TYPEFLAG_FNONEXTENSIBLE = 0x80;

    public static final int TYPEFLAG_FOLEAUTOMATION = 0x100;

    public static final int TYPEFLAG_FRESTRICTED = 0x200;

    public static final int TYPEFLAG_FAGGREGATABLE = 0x400;

    public static final int TYPEFLAG_FREPLACEABLE = 0x800;

    public static final int TYPEFLAG_FDISPATCHABLE = 0x1000;

    public static final int TYPEFLAG_FREVERSEBIND = 0x2000;

    TypeAttr ( final JIPointer values )
    {
        this ( values.isNull () ? null : (JIStruct)values.getReferent () );
    }

    TypeAttr ( final JIStruct filledStruct )
    {
        if ( filledStruct == null )
        {
            this.guid = null;
            this.lcid = -1;
            this.dwReserved = -1;
            this.memidConstructor = -1;
            this.memidDestructor = -1;
            this.lpstrSchema = null;
            this.cbSizeInstance = -1;
            this.typekind = -1;
            this.cFuncs = -1;
            this.cVars = -1;
            this.cImplTypes = -1;
            this.cbSizeVft = -1;
            this.cbAlignment = -1;
            this.wTypeFlags = -1;
            this.wMajorVerNum = -1;
            this.wMinorVerNum = -1;
            this.tdescAlias = null;
            this.idldescType = null;
            return;
        }

        this.guid = ( (rpc.core.UUID)filledStruct.getMember ( 0 ) ).toString ();
        this.lcid = ( (Integer)filledStruct.getMember ( 1 ) ).intValue ();
        this.dwReserved = ( (Integer)filledStruct.getMember ( 2 ) ).intValue ();
        this.memidConstructor = ( (Integer)filledStruct.getMember ( 3 ) ).intValue ();
        this.memidDestructor = ( (Integer)filledStruct.getMember ( 4 ) ).intValue ();
        this.lpstrSchema = (JIPointer)filledStruct.getMember ( 5 );
        this.cbSizeInstance = ( (Integer)filledStruct.getMember ( 6 ) ).intValue ();
        this.typekind = ( (Integer)filledStruct.getMember ( 7 ) ).intValue ();
        this.cFuncs = ( (Short)filledStruct.getMember ( 8 ) ).shortValue ();
        this.cVars = ( (Short)filledStruct.getMember ( 9 ) ).shortValue ();
        this.cImplTypes = ( (Short)filledStruct.getMember ( 10 ) ).shortValue ();
        this.cbSizeVft = ( (Short)filledStruct.getMember ( 11 ) ).shortValue ();
        this.cbAlignment = ( (Short)filledStruct.getMember ( 12 ) ).shortValue ();
        this.wTypeFlags = ( (Short)filledStruct.getMember ( 13 ) ).shortValue ();
        this.wMajorVerNum = ( (Short)filledStruct.getMember ( 14 ) ).shortValue ();
        this.wMinorVerNum = ( (Short)filledStruct.getMember ( 15 ) ).shortValue ();
        this.tdescAlias = new TypeDesc ( (JIStruct)filledStruct.getMember ( 16 ) );
        this.idldescType = new IdlDesc ( (JIStruct)filledStruct.getMember ( 17 ) );

    }

}
