/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006-2010 TH4 SYSTEMS GmbH (http://th4-systems.com)
 *
 * OpenSCADA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * only, as published by the Free Software Foundation.
 *
 * OpenSCADA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License version 3 for more details
 * (a copy is included in the LICENSE file that accompanied this code).
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with OpenSCADA. If not, see
 * <http://opensource.org/licenses/lgpl-3.0.html> for a copy of the LGPLv3 License.
 */

package org.openscada.opc.lib.da;

import org.jinterop.dcom.common.JIException;
import org.openscada.opc.dcom.common.EventHandler;
import org.openscada.opc.dcom.common.KeyedResult;
import org.openscada.opc.dcom.common.KeyedResultSet;
import org.openscada.opc.dcom.common.ResultSet;
import org.openscada.opc.dcom.da.IOPCDataCallback;
import org.openscada.opc.dcom.da.OPCDATASOURCE;
import org.openscada.opc.dcom.da.ValueData;
import org.openscada.opc.dcom.da.impl.OPCAsyncIO2;
import org.openscada.opc.lib.common.NotConnectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Async20Access extends AccessBase implements IOPCDataCallback {
	private static Logger logger = LoggerFactory.getLogger(Async20Access.class);

	private EventHandler eventHandler = null;

	private boolean initialRefresh = false;

	private final List<AccessBaseStateListner> accessBaseListeners = new CopyOnWriteArrayList<AccessBaseStateListner>();

	@Override
	public synchronized void addAccessBaseListener(final AccessBaseStateListner listener) {
		this.accessBaseListeners.add(listener);
	}

	@Override
	public synchronized void removeAccessBaseListener(final AccessBaseStateListner listener) {
		this.accessBaseListeners.remove(listener);
	}

	public Async20Access(final Server server, final int period, final boolean initialRefresh, final boolean polite)
			throws IllegalArgumentException, UnknownHostException, NotConnectedException, JIException,
			DuplicateGroupException {
		super(server, period, polite);
		this.initialRefresh = initialRefresh;
	}

	public Async20Access(final Server server, final int period, final boolean initialRefresh, final String logTag,
			final boolean polite) throws IllegalArgumentException, UnknownHostException, NotConnectedException,
			JIException, DuplicateGroupException {
		super(server, period, logTag, polite);
		this.initialRefresh = initialRefresh;
	}

	@Override
	protected synchronized void start() throws JIException, IllegalArgumentException, UnknownHostException,
			NotConnectedException, DuplicateGroupException {
		if (isActive()) {
			return;
		}

		super.start();

		this.eventHandler = this.group.attach(this);
		if (!this.items.isEmpty() && this.initialRefresh) {
			final OPCAsyncIO2 async20 = this.group.getAsyncIO20();
			if (async20 == null) {
				throw new NotConnectedException();
			}

			this.group.getAsyncIO20().refresh(OPCDATASOURCE.OPC_DS_CACHE, 0);
		}
	}

	@Override
	protected synchronized void stop() throws JIException {
		if (!isActive()) {
			return;
		}

		if (this.eventHandler != null) {
			try {
				this.eventHandler.detach();
			} catch (final Throwable e) {
				logger.warn("Failed to detach group", e);
			}

			this.eventHandler = null;
		}

		super.stop();
	}

	@Override
	public void cancelComplete(final int transactionId, final int serverGroupHandle) {
	}

	@Override
	public void dataChange(final int transactionId, final int serverGroupHandle, final int masterQuality,
			final int masterErrorCode, final KeyedResultSet<Integer, ValueData> result) {
		logger.debug("dataChange - transId {}, items: {}", transactionId, result.size());

		final Group group = this.group;
		if (group == null) {
			return;
		}

		for (final KeyedResult<Integer, ValueData> entry : result) {
			final Item item = group.findItemByClientHandle(entry.getKey());
			logger.debug("Update for '{}'", item.getId());
			updateItem(item, new ItemState(entry.getErrorCode(), entry.getValue().getValue(),
					entry.getValue().getTimestamp(), entry.getValue().getQuality()));
		}
	}

	@Override
	public void readComplete(final int transactionId, final int serverGroupHandle, final int masterQuality,
			final int masterErrorCode, final KeyedResultSet<Integer, ValueData> result) {
		logger.debug("readComplete - transId {}", transactionId);
	}

	@Override
	public void writeComplete(final int transactionId, final int serverGroupHandle, final int masterErrorCode,
			final ResultSet<Integer> result) {
		logger.debug("writeComplete - transId {}", transactionId);
	}

	@Override
	public void failedAddItem(String itemId, Integer rc) {
		for (final AccessBaseStateListner listener : accessBaseListeners) {
			listener.failedAddItem(itemId, rc);
		}
	}

	@Override
	public void addedItem(String itemId) {
		for (final AccessBaseStateListner listener : accessBaseListeners) {
			listener.addedItem(itemId);
		}
	}

	@Override
	public void failedRealizeItem(String itemId) {
		for (final AccessBaseStateListner listener : accessBaseListeners) {
			listener.failedRealizeItem(itemId);
		}
	}

	@Override
	public void failedUnrealizeItem(String itemId) {
		for (final AccessBaseStateListner lisetner : accessBaseListeners) {
			lisetner.failedUnrealizeItem(itemId);
		}
	}

	@Override
	public void failedToClearGroup(String groupName) {
		for (final AccessBaseStateListner listener : accessBaseListeners) {
			listener.failedToClearGroup(groupName);
		}
	}

	@Override
	public void clearedGroup(String groupName) {
		for (final AccessBaseStateListner listener : accessBaseListeners) {
			listener.clearedGroup(groupName);
		}
	}

	@Override
	public void failedToDeseableGroup(String groupName) {
		for (final AccessBaseStateListner listener : accessBaseListeners) {
			listener.failedToDeseableGroup(groupName);
		}
	}
}
