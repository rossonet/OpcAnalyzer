/* Donated by Jarapac (http://jarapac.sourceforge.net/)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110, USA
 */

package rpc;

import ndr.NdrObject;
import rpc.core.PresentationSyntax;
import rpc.core.UUID;

import java.io.IOException;

public interface Endpoint
{

    public static final int MAYBE = 0x01;

    public static final int IDEMPOTENT = 0x02;

    public static final int BROADCAST = 0x04;

    public Transport getTransport ();

    public PresentationSyntax getSyntax ();

    public void call ( int semantics, UUID object, int opnum, NdrObject ndrobj ) throws IOException;

    public void detach () throws IOException;

}
