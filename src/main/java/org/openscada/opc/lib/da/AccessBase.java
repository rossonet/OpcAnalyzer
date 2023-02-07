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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AccessBase implements ServerConnectionStateListener, AccessBaseStateListner {
	private static Logger logger = LoggerFactory.getLogger(AccessBase.class);

	protected Server server = null;

	protected Group group = null;

	protected boolean active = false;

	private final List<AccessStateListener> stateListeners = new CopyOnWriteArrayList<AccessStateListener>();

	private boolean bound = false;

	/**
	 * Holds the item to callback assignment
	 */
	protected Map<Item, DataCallback> items = new HashMap<Item, DataCallback>();

	protected Map<String, Item> itemMap = new HashMap<String, Item>();

	protected Map<Item, ItemState> itemCache = new HashMap<Item, ItemState>();

	private int period = 0;

	protected Map<String, DataCallback> itemSet = new HashMap<String, DataCallback>();

	protected String logTag = null;

	protected Logger dataLogger = null;

	private final boolean polite;

	public AccessBase(final Server server, final int period, final boolean polite) throws IllegalArgumentException,
			UnknownHostException, NotConnectedException, JIException, DuplicateGroupException {
		super();
		this.server = server;
		this.period = period;
		this.polite = polite;
	}

	public AccessBase(final Server server, final int period, final String logTag, final boolean polite) {
		super();
		this.server = server;
		this.period = period;
		this.logTag = logTag;
		if (this.logTag != null) {
			this.dataLogger = LoggerFactory.getLogger("opc.data." + logTag);
		}
		this.polite = polite;
	}

	public boolean isBound() {
		return this.bound;
	}

	public synchronized void bind() {
		if (isBound()) {
			return;
		}

		this.server.addStateListener(this);
		this.bound = true;
	}

	public synchronized void unbind() throws JIException {
		if (!isBound()) {
			return;
		}

		this.server.removeStateListener(this);
		this.bound = false;

		stop();
	}

	public boolean isActive() {
		return this.active;
	}

	public void addStateListener(final AccessStateListener listener) {
		this.stateListeners.add(listener);
		listener.stateChanged(isActive());
	}

	public void removeStateListener(final AccessStateListener listener) {
		this.stateListeners.remove(listener);
	}

	protected void notifyStateListenersState(final boolean state) {
		final List<AccessStateListener> list = new ArrayList<AccessStateListener>(this.stateListeners);

		for (final AccessStateListener listener : list) {
			listener.stateChanged(state);
		}
	}

	protected void notifyStateListenersError(final Throwable t) {
		final List<AccessStateListener> list = new ArrayList<AccessStateListener>(this.stateListeners);

		for (final AccessStateListener listener : list) {
			listener.errorOccured(t);
		}
	}

	public int getPeriod() {
		return this.period;
	}

	public synchronized void addItem(final String itemId, final DataCallback dataCallback)
			throws JIException, AddFailedException {
		if (this.itemSet.containsKey(itemId)) {
			return;
		}

		this.itemSet.put(itemId, dataCallback);

		if (isActive()) {
			realizeItem(itemId);
		}
	}

	public synchronized void removeItem(final String itemId) {
		if (!this.itemSet.containsKey(itemId)) {
			return;
		}

		this.itemSet.remove(itemId);

		if (isActive()) {
			unrealizeItem(itemId);
		}
	}

	@Override
	public void connectionStateChanged(final boolean connected) {
		try {
			if (connected) {
				start();
			} else {
				stop();
			}
		} catch (final Exception e) {
			logger.error(String.format("Failed to change state (%s)", connected), e);
		}
	}

	protected synchronized void start() throws JIException, IllegalArgumentException, UnknownHostException,
			NotConnectedException, DuplicateGroupException {
		if (isActive()) {
			return;
		}

		logger.debug("Create a new group");
		this.group = this.server.addGroup(period);
		this.group.setActive(true);
		this.active = true;

		notifyStateListenersState(true);

		realizeAll();
	}

	protected void realizeItem(final String itemId) throws JIException, AddFailedException {
		if (polite) {
			logger.debug("Realizing item: {}", itemId);
		}
		final DataCallback dataCallback = this.itemSet.get(itemId);
		if (dataCallback == null) {
			return;
		}
		final Item item = this.group.addItem(itemId);
		this.items.put(item, dataCallback);
		this.itemMap.put(itemId, item);
		addedItem(itemId);
		if (polite) {
			try {
				Thread.sleep(50L);
			} catch (final InterruptedException e) {
				logger.error("in realizing item", e);
			}
		}
		if (polite) {
			logger.debug("addedItem: {}", itemId);
		}
	}

	protected void unrealizeItem(final String itemId) {
		final Item item = this.itemMap.remove(itemId);
		this.items.remove(item);
		this.itemCache.remove(item);

		try {
			this.group.removeItem(itemId);
		} catch (final Throwable e) {
			logger.error(String.format("Failed to unrealize item '%s'", itemId), e);
			failedUnrealizeItem(itemId);
		}
	}

	/*
	 * FIXME: need some perfomance boost: subscribe all in one call
	 */
	protected void realizeAll() {
		int itemCount = 0;
		for (final String itemId : this.itemSet.keySet()) {
			try {
				realizeItem(itemId);
				itemCount++;
			} catch (final AddFailedException e) {
				Integer rc = e.getErrors().get(itemId);
				if (rc == null) {
					rc = -1;
				}
				logger.warn(String.format("Failed to add item: %s (%08X)", itemId, rc));
				failedAddItem(itemId, rc);

			} catch (final Exception e) {
				logger.warn("Failed to realize item: " + itemId, e);
				failedRealizeItem(itemId);
			}
		}
		logger.debug("completed group with " + itemCount + " items");
	}

	protected void unrealizeAll() {
		this.items.clear();
		this.itemCache.clear();
		try {
			this.group.clear();
			clearedGroup(this.group.getName());
		} catch (final JIException e) {
			logger.debug("Failed to clear group. No problem if we already lost the connection", e);
			try {
				failedToClearGroup(this.group.getName());
			} catch (final JIException ex) {
				ex.printStackTrace();
			}
		}
	}

	protected synchronized void stop() throws JIException {
		if (!isActive()) {
			return;
		}

		unrealizeAll();

		this.active = false;
		notifyStateListenersState(false);

		try {
			this.group.remove();
		} catch (final Throwable t) {
			logger.warn("Failed to disable group. No problem if we already lost connection");
			failedToDeseableGroup(this.group.getName());
		}
		this.group = null;
	}

	public synchronized void clear() {
		this.itemSet.clear();
		this.items.clear();
		this.itemMap.clear();
		this.itemCache.clear();
		try {
			clearedGroup(this.group.getName());
		} catch (final JIException e) {
			e.printStackTrace();
		}
	}

	protected void updateItem(final Item item, final ItemState itemState) {
		if (this.dataLogger != null) {
			this.dataLogger.debug("Update item: {}, {}", item.getId(), itemState);
		}

		final DataCallback dataCallback = this.items.get(item);
		if (dataCallback == null) {
			return;
		}

		final ItemState cachedState = this.itemCache.get(item);
		if (cachedState == null) {
			this.itemCache.put(item, itemState);
			dataCallback.changed(item, itemState);
		} else {
			if (!cachedState.equals(itemState)) {
				this.itemCache.put(item, itemState);
				dataCallback.changed(item, itemState);
			}
		}
	}

	protected void handleError(final Throwable e) {
		notifyStateListenersError(e);
		this.server.dispose();
	}

	public abstract void addAccessBaseListener(final AccessBaseStateListner listener);

	public abstract void removeAccessBaseListener(final AccessBaseStateListner listener);

}