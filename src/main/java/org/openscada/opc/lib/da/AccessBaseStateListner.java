package org.openscada.opc.lib.da;

public interface AccessBaseStateListner {

	public abstract void failedAddItem(String itemId, Integer rc);

	public abstract void addedItem(String itemId);

	public abstract void failedRealizeItem(String itemId);

	public abstract void failedUnrealizeItem(String itemId);

	public abstract void failedToClearGroup(String groupNam);

	public abstract void clearedGroup(String groupNam);

	public abstract void failedToDeseableGroup(String groupName);

}
