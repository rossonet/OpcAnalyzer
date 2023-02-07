package org.rossonet.debug.console.classic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.JICurrency;
import org.jinterop.dcom.core.JIFlags;
import org.jinterop.dcom.core.JIString;
import org.jinterop.dcom.core.JIVariant;
import org.openscada.opc.dcom.list.ClassDetails;
import org.openscada.opc.lib.common.AlreadyConnectedException;
import org.openscada.opc.lib.common.NotConnectedException;
import org.openscada.opc.lib.da.AccessBase;
import org.openscada.opc.lib.da.AddFailedException;
import org.openscada.opc.lib.da.AutoReconnectController;
import org.openscada.opc.lib.da.AutoReconnectListener;
import org.openscada.opc.lib.da.AutoReconnectState;
import org.openscada.opc.lib.da.DataCallback;
import org.openscada.opc.lib.da.DuplicateGroupException;
import org.openscada.opc.lib.da.Group;
import org.openscada.opc.lib.da.Item;
import org.openscada.opc.lib.da.ItemState;
import org.openscada.opc.lib.da.Server;
import org.openscada.opc.lib.da.SyncAccess;
import org.openscada.opc.lib.da.browser.Branch;
import org.openscada.opc.lib.da.browser.Leaf;
import org.openscada.opc.lib.da.browser.TreeBrowser;
import org.openscada.opc.lib.list.Category;
import org.openscada.opc.lib.list.ServerList;
import org.rossonet.annotation.ShellMethod;
import org.rossonet.annotation.ShellOption;
import org.rossonet.utils.LogHelper;

import com.google.gson.Gson;

/**
 *
 * Interfaccia da linea di comando
 *
 * @author Andrea Ambrosini Rossonet s.c.a r.l. andrea.ambrosini@rossonet.com
 */

public class OpcDaShellInterface {

	private static final Logger logger = Logger.getLogger(OpcDaShellInterface.class.getName());

	private final static Gson gson = new Gson();

	private static final String convertData(JIVariant data) throws JIException {
		String result = null;
		switch (data.getType()) {
		case JIVariant.VT_VARIANT:
			result = data.getObjectAsVariant().toString();
			break;
		case JIVariant.VT_BOOL:
			result = String.valueOf(data.getObjectAsBoolean());
			break;
		case JIVariant.VT_DECIMAL:
			result = String.valueOf(data.getObjectAsLong());
			break;
		case JIVariant.VT_INT:
			result = String.valueOf(data.getObjectAsInt());
			break;
		case JIVariant.VT_UI1:
			result = String.valueOf(data.getObjectAsUnsigned().getValue());
			break;
		case JIVariant.VT_UI2:
			result = String.valueOf(data.getObjectAsUnsigned().getValue());
			break;
		case JIVariant.VT_UI4:
			result = String.valueOf(data.getObjectAsUnsigned().getValue());
			break;
		case JIVariant.VT_I4:
			result = String.valueOf(data.getObjectAsInt());
			break;
		case JIVariant.VT_R4:
			result = String.valueOf(data.getObjectAsFloat());
			break;
		case JIVariant.VT_R8:
			result = String.valueOf(data.getObjectAsDouble());
			break;
		case JIVariant.VT_UINT:
			result = String.valueOf(data.getObjectAsInt());
			break;
		case JIVariant.VT_I2:
			result = String.valueOf(data.getObjectAsShort());
			break;
		case JIVariant.VT_CY:
			final JICurrency a = (JICurrency) data.getObject();
			result = String.valueOf(a.getUnits() + "." + a.getFractionalUnits());
			break;
		case 8197:
			result = gson.toJson(data.getObjectAsArray().getArrayInstance());
			break;
		case 8200:
			result = gson.toJson(data.getObjectAsArray().getArrayInstance());
			break;
		case JIVariant.VT_EMPTY:
			result = "VT_EMPTY";
			break;
		case JIVariant.VT_ERROR:
			result = "VT_ERROR";
			break;
		case JIVariant.VT_DATE:
			result = String.valueOf(data.getObjectAsDate().toInstant().toEpochMilli());
			break;
		case JIVariant.VT_BSTR:
			result = data.getObjectAsString().getString();
			break;
		case JIVariant.VT_I1:
			result = String.valueOf(data.getObjectAsChar());
			break;
		default:
			logger.warning("Conversion type not found. The type is " + data.getType());
		}
		return result;
	}

	private static final String convertTypeData(JIVariant data) throws JIException {
		String result = null;
		switch (data.getType()) {
		case JIVariant.VT_VARIANT:
			result = "VT_VARIANT";
			break;
		case JIVariant.VT_BOOL:
			result = "VT_BOOL";
			break;
		case JIVariant.VT_DECIMAL:
			result = "VT_DECIMAL";
			break;
		case JIVariant.VT_INT:
			result = "VT_INT";
			break;
		case JIVariant.VT_UI1:
			result = "VT_UI1";
			break;
		case JIVariant.VT_UI2:
			result = "VT_UI2";
			break;
		case JIVariant.VT_UI4:
			result = "VT_UI4";
			break;
		case JIVariant.VT_I4:
			result = "VT_I4";
			break;
		case JIVariant.VT_R4:
			result = "VT_R4";
			break;
		case JIVariant.VT_R8:
			result = "VT_R8";
			break;
		case JIVariant.VT_UINT:
			result = "VT_UINT";
			break;
		case JIVariant.VT_I2:
			result = "VT_I2";
			break;
		case JIVariant.VT_CY:
			result = "VT_CY";
			break;
		case 8197:
			result = "VT_DOUBLE_ARRAY";
			break;
		case 8200:
			result = "VT_TEXT_ARRAY";
			break;
		case JIVariant.VT_EMPTY:
			result = "VT_EMPTY";
			break;
		case JIVariant.VT_ERROR:
			result = "VT_ERROR";
			break;
		case JIVariant.VT_DATE:
			result = "VT_DATE";
			break;
		case JIVariant.VT_BSTR:
			result = "VT_BSTR";
			break;
		case JIVariant.VT_I1:
			result = "VT_I1";
			break;
		default:
			logger.info("Conversion type not found. The type is " + data.getType());
		}
		return result;
	}

	private static Collection<ClassDetails> execDiscover(String host, String dom, String user, String pass,
			Category category) throws IllegalArgumentException, UnknownHostException, JIException {
		Collection<ClassDetails> srvs = null;
		final ServerList srvlist = new ServerList(host, user, pass, dom);
		final org.openscada.opc.lib.list.Category[] implemented = new Category[1];
		final org.openscada.opc.lib.list.Category[] required = new Category[1];
		implemented[0] = category;
		required[0] = category;
		srvs = srvlist.listServersWithDetails(implemented, required);
		return srvs;
	}

	private final ScheduledExecutorService serverScheduler = Executors.newScheduledThreadPool(32);

	private Server createConnection(String progId, String clsId, String hostTarget, String domainTarget,
			String usernameTarget, String passwordTarget) {
		final org.openscada.opc.lib.common.ConnectionInformation ci = new org.openscada.opc.lib.common.ConnectionInformation();
		ci.setHost(hostTarget);
		ci.setDomain(domainTarget);
		ci.setUser(usernameTarget);
		ci.setPassword(passwordTarget);
		ci.setClsid(clsId);
		ci.setProgId(progId);
		return new Server(ci, serverScheduler);
	}

	@ShellMethod(value = "Discover OPC DA service on a remote connection", group = "OPC DA Tools Commands")
	public String discoverServiceClassicOpcDa(@ShellOption(help = "host of OPC DA Server") String hostTarget,
			@ShellOption(help = "domain of OPC DA Server", defaultValue = "WORKGROUP") String domainTarget,
			@ShellOption(help = "username of OPC DA Server", defaultValue = "Administrator") String usernameTarget,
			@ShellOption(help = "password of OPC DA Server") String passwordTarget) {
		return discoveryServer(hostTarget, domainTarget, usernameTarget, passwordTarget);
	}

	private String discoveryServer(String hostTarget, String domainTarget, String usernameTarget,
			String passwordTarget) {
		final Category[] categoryList = { org.openscada.opc.lib.list.Categories.OPCDAServer10,
				org.openscada.opc.lib.list.Categories.OPCDAServer20,
				org.openscada.opc.lib.list.Categories.OPCDAServer30,
				new Category("58E13251-AC87-11d1-84D5-00608CB8A7E9"),
				new Category("7DE5B060-E089-11d2-A5E6-000086339399") };
		final StringBuilder sb = new StringBuilder();
		for (final Category c : categoryList) {
			try {
				switch (c.toString()) {
				case "63D5F430-CFE4-11d1-B2C8-0060083BA1FB":
					sb.append("OPC DA Server 1.0\n");
					break;
				case "63D5F432-CFE4-11d1-B2C8-0060083BA1FB":
					sb.append("OPC DA Server 2.0\n");
					break;
				case "CC603642-66D7-48f1-B69A-B625E73652D7":
					sb.append("OPC DA Server 3.0\n");
					break;
				case "3098EDA4-A006-48b2-A27F-247453959408":
					sb.append("OPC DA Server XML 1.0\n");
					break;
				case "58E13251-AC87-11d1-84D5-00608CB8A7E9":
					sb.append("OPC A&E Server 1.0\n");
					break;
				case "7DE5B060-E089-11d2-A5E6-000086339399":
					sb.append("OPC HDA Server 1.0\n");
					break;
				default:
				}
				for (final ClassDetails single : execDiscover(hostTarget, domainTarget, usernameTarget, passwordTarget,
						c)) {
					sb.append("- " + single.getDescription() + " -> [" + single.getClsId() + "] " + single.getProgId()
							+ "\n");
				}
			} catch (final Exception e) {
				logger.warning(
						"service " + c + " is not implemented on the server\n" + LogHelper.stackTraceToString(e));
			}
		}
		return sb.toString();
	}

	private String dumpTree(PrintStream out, String progId, String clsId, String hostTarget, String domainTarget,
			String usernameTarget, String passwordTarget)
			throws IllegalArgumentException, UnknownHostException, JIException, AlreadyConnectedException,
			InterruptedException, NotConnectedException, DuplicateGroupException {
		final Server opcDaServer = createConnection(progId, clsId, hostTarget, domainTarget, usernameTarget,
				passwordTarget);
		final AutoReconnectController autoReconnectController = new AutoReconnectController(opcDaServer);
		autoReconnectController.connect();
		final AtomicBoolean done = new AtomicBoolean(false);
		final StringBuilder sb = new StringBuilder();
		final AutoReconnectListener listener = new AutoReconnectListener() {
			private boolean down = true;

			private void resolveBranch(PrintStream out, TreeBrowser treeBrowser, Branch branch, String tab)
					throws IllegalArgumentException, UnknownHostException, JIException {
				System.out.println("SEARCH IN " + branch.getName() + " - " + branch.getBranches() + " - "
						+ branch.getBranchStack() + branch.getLeaves());
				treeBrowser.fill(branch);
				for (final Branch b : branch.getBranches()) {
					out.println(tab + "-* branche: " + b.getName());
					resolveBranch(out, treeBrowser, b, tab + "\t");
				}
				for (final Leaf l : branch.getLeaves()) {
					out.println(tab + "-* leave: " + l.getName());
				}
			}

			private void resolveTreeServer(PrintStream out, Server opcDaServer)
					throws UnknownHostException, JIException {
				final TreeBrowser treeBrowser = opcDaServer.getTreeBrowser();
				for (final Branch b : treeBrowser.browseBranches().getBranches()) {
					out.println("-> branche: " + b.getName());
					resolveBranch(out, treeBrowser, b, "\t");
				}
				for (final Leaf l : treeBrowser.browseLeaves().getLeaves()) {
					out.println("-> leave: " + l.getName());
				}
			}

			@Override
			public void stateChanged(AutoReconnectState state) {
				if (down) {
					System.out.print("." + state.name() + ".");
				}
				if (state.equals(AutoReconnectState.CONNECTED)) {
					down = false;
					System.out.println("\n");
					try {
						resolveTreeServer(out, opcDaServer);
						done.set(true);
					} catch (IllegalArgumentException | JIException | UnknownHostException e) {
						e.printStackTrace();
					}
				}
			}
		};
		autoReconnectController.addListener(listener);
		while (!done.get()) {
			Thread.sleep(1000L);
			System.out.print(".");
		}
		System.out.println("ok");
		autoReconnectController.disconnect();
		return sb.toString();
	}

	@ShellMethod(value = "View address space on remote OPC DA service", group = "OPC DA Tools Commands")
	public String dumpTreeServiceClassicOpcDa(@ShellOption(help = "progId of OPC DA Server") String progId,
			@ShellOption(help = "clsId of OPC DA Server") String clsId,
			@ShellOption(help = "host of OPC DA Server") String hostTarget,
			@ShellOption(help = "domain of OPC DA Server", defaultValue = "WORKGROUP") String domainTarget,
			@ShellOption(help = "username of OPC DA Server", defaultValue = "Administrator") String usernameTarget,
			@ShellOption(help = "password of OPC DA Server") String passwordTarget)
			throws IllegalArgumentException, UnknownHostException, JIException, AlreadyConnectedException,
			InterruptedException, NotConnectedException, DuplicateGroupException {
		return dumpTree(System.out, progId, clsId, hostTarget, domainTarget, usernameTarget, passwordTarget);
	}

	private String readSyncDataOnOpcDaGroup(String progId, String clsId, String hostTarget, String domainTarget,
			String usernameTarget, String passwordTarget, String itemId, long timeout, int frequency)
			throws IllegalArgumentException, UnknownHostException, JIException, AlreadyConnectedException,
			InterruptedException, NotConnectedException, DuplicateGroupException {
		final Server opcDaServer = createConnection(progId, clsId, hostTarget, domainTarget, usernameTarget,
				passwordTarget);
		final AutoReconnectController autoReconnectController = new AutoReconnectController(opcDaServer);
		autoReconnectController.connect();
		final AtomicBoolean done = new AtomicBoolean(false);
		final StringBuilder sb = new StringBuilder();
		final AutoReconnectListener listener = new AutoReconnectListener() {

			AccessBase access = null;

			private final DataCallback callback = new DataCallback() {
				@Override
				public void changed(Item item, ItemState itemState) {
					try {
						System.out.println("\n" + item.getId() + " ["
								+ itemState.getTimestamp().toInstant().getEpochSecond() + "]\nq:"
								+ itemState.getQuality() + " t:" + convertTypeData(itemState.getValue()) + "\nv:"
								+ convertData(itemState.getValue())
								+ ((itemState.getErrorCode() > 0) ? "\ne:" + itemState.getErrorCode() : ""));
					} catch (final JIException e) {
						e.printStackTrace();
					}
				}
			};

			private boolean down = true;

			@Override
			public void stateChanged(AutoReconnectState state) {
				try {
					if (down) {
						System.out.print("." + state.name() + ".");
					}
					if (state.equals(AutoReconnectState.CONNECTED)) {
						down = false;
						System.out.println("\n");
						access = new SyncAccess(opcDaServer, frequency, false);
						access.addItem(itemId, callback);
						access.bind();
						Thread.sleep(timeout);
						done.set(true);
					}
				} catch (JIException | AddFailedException | IllegalArgumentException | UnknownHostException
						| NotConnectedException | DuplicateGroupException | InterruptedException e) {
					if (access != null) {
						try {
							access.unbind();
							access.clear();
							done.set(true);
						} catch (final JIException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		};
		autoReconnectController.addListener(listener);
		while (!done.get()) {
			Thread.sleep(1000L);
		}
		autoReconnectController.disconnect();
		return sb.toString();
	}

	@ShellMethod(value = "read values from item in sync mode on OPC DA service", group = "OPC DA Tools Commands")
	public String readValueSyncServiceClassicOpcDa(@ShellOption(help = "progId of OPC DA Server") String progId,
			@ShellOption(help = "clsId of OPC DA Server") String clsId,
			@ShellOption(help = "host of OPC DA Server") String hostTarget,
			@ShellOption(help = "domain of OPC DA Server", defaultValue = "WORKGROUP") String domainTarget,
			@ShellOption(help = "username of OPC DA Server", defaultValue = "Administrator") String usernameTarget,
			@ShellOption(help = "password of OPC DA Server") String passwordTarget,
			@ShellOption(help = "item id (from dumptree)") String ItemId,
			@ShellOption(help = "frequency in ms", defaultValue = "10000") String frequency,
			@ShellOption(help = "time to work in ms", defaultValue = "60000") String time)
			throws IllegalArgumentException, UnknownHostException, JIException, AlreadyConnectedException,
			InterruptedException, NotConnectedException, DuplicateGroupException {
		return readSyncDataOnOpcDaGroup(progId, clsId, hostTarget, domainTarget, usernameTarget, passwordTarget, ItemId,
				Long.valueOf(time), Integer.valueOf(frequency));
	}

	@ShellMethod(value = "Save address space of the remote OPC DA service on file", group = "OPC DA Tools Commands")
	public String saveDumpTreeServiceClassicOpcDa(
			@ShellOption(help = "target file", defaultValue = "list-items.txt") String fileTarget,
			@ShellOption(help = "progId of OPC DA Server") String progId,
			@ShellOption(help = "clsId of OPC DA Server") String clsId,
			@ShellOption(help = "host of OPC DA Server") String hostTarget,
			@ShellOption(help = "domain of OPC DA Server", defaultValue = "WORKGROUP") String domainTarget,
			@ShellOption(help = "username of OPC DA Server", defaultValue = "Administrator") String usernameTarget,
			@ShellOption(help = "password of OPC DA Server") String passwordTarget)
			throws IllegalArgumentException, UnknownHostException, JIException, AlreadyConnectedException,
			InterruptedException, NotConnectedException, DuplicateGroupException, FileNotFoundException {
		final PrintStream writetoEngineer = new PrintStream(new File(fileTarget));
		dumpTree(writetoEngineer, progId, clsId, hostTarget, domainTarget, usernameTarget, passwordTarget);
		writetoEngineer.close();
		return "ok";
	}

	private boolean writeData(String progId, String clsId, String hostTarget, String domainTarget,
			String usernameTarget, String passwordTarget, String itemId, String value) {
		try {
			final Server opcDaServer = createConnection(progId, clsId, hostTarget, domainTarget, usernameTarget,
					passwordTarget);
			final AutoReconnectController autoReconnectController = new AutoReconnectController(opcDaServer);
			autoReconnectController.connect();
			final AtomicBoolean done = new AtomicBoolean(false);
			final AutoReconnectListener listener = new AutoReconnectListener() {

				private boolean down = true;

				@Override
				public void stateChanged(AutoReconnectState state) {
					try {
						if (down) {
							System.out.print("." + state.name() + ".");
						}
						if (state.equals(AutoReconnectState.CONNECTED)) {
							down = false;
							System.out.println("\n");
							final Group group = opcDaServer.addGroup("writer", 1000);
							final Item item = group.addItem(itemId);
							logger.info("*****pre writeData " + value);
							final int count = writeSingle(item, value);
							logger.info("*****post writeData " + count);
							done.set(true);
						}
					} catch (JIException | AddFailedException | IllegalArgumentException | UnknownHostException
							| NotConnectedException | DuplicateGroupException e) {
					}
				}
			};
			autoReconnectController.addListener(listener);
			while (!done.get()) {
				try {
					Thread.sleep(1000L);
				} catch (final InterruptedException e) {
					logger.severe(LogHelper.stackTraceToString(e));
				}
			}
			autoReconnectController.disconnect();

			return true;
		} catch (final IllegalArgumentException e) {
			logger.severe(LogHelper.stackTraceToString(e));
			return false;
		}
	}

	private final Integer writeSingle(Item item, String data) throws JIException {
		logger.info("****************writeSingle received data from " + item.getId());
		Integer result = null;
		if (item != null) {
			final String type = item.read(true).getValue().getObject().getClass().getSimpleName();
			if (data != null && Arrays.asList("Integer", "Short", "Float", "Double", "Boolean", "Long", "String",
					"JIUnsignedInteger", "JIString").contains(type)) {
				try {
					JIVariant targetValue = null;
					switch (type) {
					case "Integer":
						targetValue = new JIVariant(Integer.parseInt(data));
						result = item.write(targetValue);
						break;
					case "JIUnsignedInteger":
						targetValue = new JIVariant(Integer.parseInt(data));
						result = item.write(targetValue);
						break;
					case "Short":
						targetValue = new JIVariant(Short.parseShort(data));
						result = item.write(targetValue);
						break;
					case "Float":
						targetValue = new JIVariant(Float.parseFloat(data));
						result = item.write(targetValue);
						break;
					case "Double":
						targetValue = new JIVariant(Double.parseDouble(data));
						result = item.write(targetValue);
						break;
					case "Boolean":
						if (data.equalsIgnoreCase("1") || data.equalsIgnoreCase("1.0")) {
							data = "true";
						} else {
							data = "false";
						}
						targetValue = new JIVariant(Boolean.parseBoolean(data));
						result = item.write(targetValue);
						break;
					case "Long":
						targetValue = new JIVariant(Long.parseLong(data));
						result = item.write(targetValue);
						break;
					case "String":
						targetValue = new JIVariant(data);
						result = item.write(targetValue);
						break;
					case "JIString":
						targetValue = new JIVariant(new JIString(data, JIFlags.FLAG_REPRESENTATION_STRING_BSTR), false);
						result = item.write(targetValue);
						break;
					default:
						targetValue = new JIVariant(data);
						result = item.write(targetValue);
						break;
					}
				} catch (final Exception ee) {
					logger.severe(LogHelper.stackTraceToString(ee));
				}
			} else {
				logger.info(
						"Not good type value in the write, type is: " + type + " / item: " + item + " / data: " + data);
			}
		}
		return result;
	}

	@ShellMethod(value = "read values from item in sync mode on OPC DA service", group = "OPC DA Tools Commands")
	public boolean writeValueSyncServiceClassicOpcDa(@ShellOption(help = "progId of OPC DA Server") String progId,
			@ShellOption(help = "clsId of OPC DA Server") String clsId,
			@ShellOption(help = "host of OPC DA Server") String hostTarget,
			@ShellOption(help = "domain of OPC DA Server", defaultValue = "WORKGROUP") String domainTarget,
			@ShellOption(help = "username of OPC DA Server", defaultValue = "Administrator") String usernameTarget,
			@ShellOption(help = "password of OPC DA Server") String passwordTarget,
			@ShellOption(help = "item id (from dumptree)") String ItemId,
			@ShellOption(help = "value to write") String value)
			throws IllegalArgumentException, UnknownHostException, JIException, AlreadyConnectedException,
			InterruptedException, NotConnectedException, DuplicateGroupException {
		return writeData(progId, clsId, hostTarget, domainTarget, usernameTarget, passwordTarget, ItemId, value);
	}

}
