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
import org.openscada.opc.lib.common.NotConnectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class SyncAccess extends AccessBase implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(SyncAccess.class);

	private Thread runner = null;

	private Throwable lastError = null;

	private final List<AccessBaseStateListner> accessBaseListeners = new CopyOnWriteArrayList<AccessBaseStateListner>();

	@Override
	public synchronized void addAccessBaseListener(final AccessBaseStateListner listener) {
		this.accessBaseListeners.add(listener);
	}

	@Override
	public synchronized void removeAccessBaseListener(final AccessBaseStateListner listener) {
		this.accessBaseListeners.remove(listener);
	}

	public SyncAccess(final Server server, final int period, final boolean polite) throws IllegalArgumentException,
			UnknownHostException, NotConnectedException, JIException, DuplicateGroupException {
		super(server, period, polite);
	}

	public SyncAccess(final Server server, final int period, final String logTag, final boolean polite)
			throws IllegalArgumentException, UnknownHostException, NotConnectedException, JIException,
			DuplicateGroupException {
		super(server, period, logTag, polite);
	}

	@Override
	public void run() {
		while (this.active) {
			try {
				runOnce();
				if (this.lastError != null) {
					this.lastError = null;
					handleError(null);
				}
			} catch (final Throwable e) {
				logger.error("Sync read failed", e);
				handleError(e);
				this.server.disconnect();
			}

			try {
				Thread.sleep(getPeriod());
			} catch (final InterruptedException e) {
			}
		}
	}

	protected void runOnce() throws JIException {
		if (!this.active || this.group == null) {
			return;
		}

		Map<Item, ItemState> result;

		// lock only this section since we could get into a deadlock otherwise
		// calling updateItem
		synchronized (this) {
			final Item[] items = this.items.keySet().toArray(new Item[this.items.size()]);
			result = this.group.read(false, items);
		}

		for (final Map.Entry<Item, ItemState> entry : result.entrySet()) {
			updateItem(entry.getKey(), entry.getValue());
		}

	}

	@Override
	public synchronized void start() throws JIException, IllegalArgumentException, UnknownHostException,
			NotConnectedException, DuplicateGroupException {
		super.start();

		this.runner = new Thread(this, "UtgardSyncReader");
		this.runner.setDaemon(true);
		this.runner.start();
	}

	@Override
	public synchronized void stop() throws JIException {
		super.stop();

		this.runner = null;
		this.items.clear();
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
