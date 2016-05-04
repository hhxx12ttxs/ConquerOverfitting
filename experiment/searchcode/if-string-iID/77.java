package com.dotspots.mozilla.api;

import org.mozilla.xpconnect.gecko.nsIDOMWindow;
import org.mozilla.xpconnect.gecko.nsIDocShell;
import org.mozilla.xpconnect.gecko.nsIInterfaceRequestor;
import org.mozilla.xpconnect.gecko.nsIRequest;
import org.mozilla.xpconnect.gecko.nsIURI;
import org.mozilla.xpconnect.gecko.nsIWebProgress;
import org.mozilla.xpconnect.gecko.nsIWebProgressListener;
import org.mozilla.xpconnect.gecko.nsIWindowMediator;
import org.mozilla.xpconnect.gecko.nsIWindowMediatorListener;
import org.mozilla.xpconnect.gecko.nsIXULWindow;

import com.dotspots.mozilla.dom.ChromeWindow;
import com.dotspots.mozilla.dom.InternalSimpleIteratorWrapper;
import com.dotspots.mozilla.dom.xul.Browser;
import com.dotspots.mozilla.dom.xul.Tab;
import com.dotspots.mozilla.dom.xul.TabContainer;
import com.dotspots.mozilla.dom.xul.TabbedBrowser;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

public class Tabs implements nsIWindowMediatorListener.Callback {
	private static final String WINDOW_MEDIATOR = "@mozilla.org/appshell/window-mediator;1";
	private static final String BROWSER_WINDOW = "navigator:browser";

	private final HandlerManager handlerManager = new HandlerManager(this);
	private final nsIWindowMediator windowMediator;

	public Tabs() {
		this.windowMediator = nsIWindowMediator.getService(WINDOW_MEDIATOR);
		windowMediator.addListener(nsIWindowMediatorListener.wrap(this));

		for (ChromeWindow window : new InternalSimpleIteratorWrapper<ChromeWindow>(windowMediator.getEnumerator(BROWSER_WINDOW))) {
			onOpenWindow(window);
		}
	}

	public HandlerRegistration addCreatedListener(TabCreatedHandler handler) {
		return handlerManager.addHandler(TabCreatedEvent.TYPE, handler);
	}

	public HandlerRegistration addNavigatedListener(TabNavigatedHandler handler) {
		return handlerManager.addHandler(TabNavigatedEvent.TYPE, handler);
	}

	@Override
	public void onCloseWindow(nsIXULWindow window) {

	}

	@Override
	public void onOpenWindow(nsIXULWindow window) {
		ChromeWindow chromeWindow = getChromeWindowFromXulWindow(window);
		onOpenWindow(chromeWindow);
	}

	private void onOpenWindow(final ChromeWindow chromeWindow) {
		chromeWindow.addEventListener("load", new EventListener() {
			public void onBrowserEvent(Event event) {
				onChromeWindowLoaded(chromeWindow);
			}
		});
	}

	private void onChromeWindowLoaded(final ChromeWindow chromeWindow) {
		// Ensure that it's a browser-type window
		if (!chromeWindow.getDocument().getWindowType().equals(BROWSER_WINDOW)) {
			return;
		}

		final TabbedBrowser browser = chromeWindow.getDocument().getBrowser();
		final TabContainer tabContainer = browser.getTabContainer();

		// Simulate TabOpen for existing browsers
		for (int i = 0; i < tabContainer.getItemCount(); i++) {
			onTabBrowserOpened(tabContainer.getItemAtIndex(i));
		}

		tabContainer.addEventListener("TabOpen", new EventListener() {
			@Override
			public void onBrowserEvent(Event event) {
				final Tab tab = (Tab) event.getEventTarget().cast();

				onTabBrowserOpened(tab);
			}
		});

		tabContainer.addEventListener("TabClose", new EventListener() {
			@Override
			public void onBrowserEvent(Event event) {
				final Tab tab = (Tab) event.getEventTarget().cast();

				handlerManager.fireEvent(new TabRemovedEvent(tab));
			}
		});
	}

	private void onTabBrowserOpened(final Tab tab) {
		handlerManager.fireEvent(new TabCreatedEvent(tab));
		handlerManager.fireEvent(new TabNavigatedEvent(tab));

		final Browser browser = tab.getLinkedBrowser();

		final HandlerRegistration progressListener = browser.addProgressListener(nsIWebProgressListener.wrap(new nsIWebProgressListener.Callback() {
			@Override
			public void onStatusChange(nsIWebProgress aWebProgress, nsIRequest aRequest, int aStatus, String aMessage) {

			}

			@Override
			public void onStateChange(nsIWebProgress aWebProgress, nsIRequest aRequest, int aStateFlags, int aStatus) {
				// GWT.log("State flags: " + Integer.toHexString(aStateFlags) + ", status: " +
				// Integer.toHexString(aStatus), null);
			}

			@Override
			public void onSecurityChange(nsIWebProgress aWebProgress, nsIRequest aRequest, int aState) {

			}

			@Override
			public void onProgressChange(nsIWebProgress aWebProgress, nsIRequest aRequest, int aCurSelfProgress, int aMaxSelfProgress,
					int aCurTotalProgress, int aMaxTotalProgress) {

			}

			@Override
			public void onLocationChange(nsIWebProgress aWebProgress, nsIRequest aRequest, nsIURI aLocation) {
				handlerManager.fireEvent(new TabNavigatedEvent(tab));
			}
		}));

		tab.addEventListener("TabClose", new EventListener() {
			@Override
			public void onBrowserEvent(Event event) {
				progressListener.removeHandler();
			}
		});
	}

	private ChromeWindow getChromeWindowFromXulWindow(nsIXULWindow window) {
		final nsIDocShell docShell = window.getDocShell();
		nsIInterfaceRequestor interfaceRequestor = docShell.queryInterface(nsIInterfaceRequestor.iid());
		ChromeWindow chromeWindow = interfaceRequestor.getInterface(nsIDOMWindow.iid()).cast();

		return chromeWindow;
	}

	@Override
	public void onWindowTitleChange(nsIXULWindow window, String newTitle) {
		// Unused at this time
	}
}

