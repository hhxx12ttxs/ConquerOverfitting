/***
 * Coalevo Project
 * http://www.coalevo.net
 *
 * (c) Dieter Wimberger
 * http://dieter.wimpi.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at:
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***/
package net.coalevo.manager.impl;

import org.osgi.framework.*;
import org.osgi.service.startlevel.StartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thinlet.Thinlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;


/**
 * Provides a {@link Handler} implementation that will handle the
 * UI of the framework panel.
 * <p/>
 *
 * @author Dieter Wimberger (wimpi)
 * @version @version@ (@date@)
 */
public class FrameworkHandler implements Handler, ProgressIndicator {

  private static final Logger log = LoggerFactory.getLogger(FrameworkHandler.class);
  private ResourceBundle m_Resources = ResourceBundle.getBundle("net.coalevo.manager.resources.string");
  private BundleListener m_BundleListener;
  private ManagerDesktopImpl m_Thinlet;
  private BundleContext m_BundleContext = Activator.getBundleContext();
  private Map m_Bundles = new HashMap();
  private StartLevel m_StartLevels;
  private ServiceReference m_StartLevelRef;
  private Object m_InstallDialog;
  private boolean m_Loading = false;
  private boolean m_Cancelled = false;

  public FrameworkHandler(Thinlet thinlet) {
    m_Thinlet = (ManagerDesktopImpl) thinlet;
  }//constructor

  private Bundle getSelectedBundle() {
    Object item = m_Thinlet.getSelectedItem(m_Thinlet.find(m_Thinlet.getActualPanel(), "bundles.list"));
    for (Iterator iter = m_Bundles.entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry) iter.next();
      if (entry.getValue().equals(item)) {
        Bundle b = m_BundleContext.getBundle(((Long) entry.getKey()).longValue());
        //printMetaTypes(Activator.getServices().getMetaTypeService(), b);
        return b;
      }
    }
    return null;
  }//getSelectedBundle


  public void startBundle() {
    log.debug("startBundle()");
    Bundle bundle = getSelectedBundle();
    if (bundle == null) {
      return;
    } else {
      try {
        bundle.start();
      } catch (BundleException ex) {
        log.error("startBundle()", ex);
      }
    }
  }//startBundle

  public void stopBundle() {
    log.debug("stopBundle()");
    Bundle bundle = getSelectedBundle();
    if (bundle == null) {
      return;
    } else {
      try {
        bundle.stop();
      } catch (BundleException ex) {
        log.error("stopBundle()", ex);
      }
    }
  }//stopBundle

  public void reloadBundle() {
    log.debug("reloadBundle()");
    Bundle bundle = getSelectedBundle();
    if (bundle == null) {
      return;
    } else {
      try {
        bundle.update();
      } catch (BundleException ex) {
        log.error("reloadBundle()", ex);
      }
    }

  }//reloadBundle

  public void addBundle() {
    log.debug("addBundle()");
    m_InstallDialog = m_Thinlet.addComponent(m_Thinlet, "/net/coalevo/manager/resources/addbundle_dialog.xml", this);
  }//addBundle

  public void actionInstallBundle() {
    if (m_Loading) {
      return;
    }
    final Object editor = m_Thinlet.find(m_InstallDialog, "bundle.url");
    final String str = m_Thinlet.getString(editor, "text");
    URL url;
    try {
      url = new URL(str);
    } catch (MalformedURLException ex) {
      m_Thinlet.showError(m_Resources.getString("bundle.install.urlmalformed"));
      return;
    }
    try {
      URLConnection conn = url.openConnection();
      ProgressInputStream in = new ProgressInputStream(conn.getInputStream(), this, conn.getContentLength());
      m_Thinlet.putProperty(m_InstallDialog, "stream", in);
      new Thread(new Loader(str, in)).start();
    } catch (IOException ex) {
      m_Thinlet.showError(m_Resources.getString("bundle.install.failed") + " [" + ex.getMessage() + "].");
    }
  }//actionInstallBundle

  public void actionCancelInstallBundle() {
    if (!m_Loading) {
      installBundleDone();
    }
    //Check for loading going on.
    final ProgressInputStream in = (ProgressInputStream) m_Thinlet.getProperty(m_InstallDialog, "stream");
    if (m_Loading) {
      m_Cancelled = true;
      in.cancel(true);
    }
  }//actionCancelInstallBundle

  private void installBundleDone() {
    m_Thinlet.remove(m_InstallDialog);
    m_InstallDialog = null;
  }//installBundleDone

  public void removeBundle() {
    log.debug("removeBundle()");
    Bundle bundle = getSelectedBundle();
    if (bundle == null) {
      return;
    } else {
      try {
        bundle.uninstall();
      } catch (BundleException ex) {
        log.error("removeBundle()", ex);
      }
    }
  }//removeBundle

  public void selectedBundle() {
    log.debug("selectedBundle()");
    Bundle b = getSelectedBundle();
    if (b != null) {
      m_Thinlet.setString(m_Thinlet.find("bundle.id"), "text", String.valueOf(b.getBundleId()));
      m_Thinlet.setString(m_Thinlet.find("bundle.name"), "text", b.getHeaders().get("Bundle-Name").toString());
      m_Thinlet.setString(m_Thinlet.find("bundle.startlevel"), "text", String.valueOf(m_StartLevels.getBundleStartLevel(b)));
      m_Thinlet.setString(m_Thinlet.find("bundle.headers.content"), "text", formatProperties(b.getHeaders()));
      m_Thinlet.setString(m_Thinlet.find("bundle.services.content"), "text", formatServices(b.getRegisteredServices(), b.getServicesInUse()));
      m_Thinlet.setString(
          m_Thinlet.find("bundle.packages.content"),
          "text",
          formatPackages(
              (String) b.getHeaders().get(Constants.EXPORT_PACKAGE),
              (String) b.getHeaders().get(Constants.IMPORT_PACKAGE)
          )
      );
    }
  }//selectedBundle

  public void setBundleStartLevel() {
    log.debug("setBundleStartLevel()");
    String str = m_Thinlet.getString(m_Thinlet.find("bundle.startlevel"), "text");
    int sl = Integer.parseInt(str);
    Bundle b = getSelectedBundle();
    m_StartLevels.setBundleStartLevel(b, sl);
  }//setBundleStartLevel

  private String formatProperties(Dictionary d) {
    final StringWriter writer = new StringWriter();

    try {
      for (Enumeration iter = d.keys(); iter.hasMoreElements();) {
        String key = (String) iter.nextElement();
        writer.write(key);
        writer.write(": ");
        writer.write(d.get(key).toString());
        writer.write("\n");
      }
      writer.flush();
      return writer.toString();
    } catch (Exception ex) {
      log.error("formatProperties()", ex);
    }
    return writer.getBuffer().toString();
  }//formatProperties

  private String formatServices(ServiceReference[] exported, ServiceReference[] imported) {
    final StringBuffer sbuf = new StringBuffer();
    sbuf.append(m_Resources.getString("bundle.services.imported"));
    sbuf.append("\n\n");
    if (imported == null || imported.length == 0) {
      sbuf.append(m_Resources.getString("bundle.services.none"));
    } else {
      for (int i = 0; i < imported.length; i++) {
        ServiceReference service = imported[i];
        sbuf.append("Bundle #");
        sbuf.append(service.getBundle().getBundleId());
        sbuf.append(" (");
        sbuf.append(service.getBundle().getHeaders().get(Constants.BUNDLE_VERSION));
        sbuf.append(") ");
        sbuf.append(service.toString());
        sbuf.append("\n");
      }
    }
    sbuf.append("\n\n");
    sbuf.append(m_Resources.getString("bundle.services.exported"));
    sbuf.append("\n\n");
    if (exported == null || exported.length == 0) {
      sbuf.append(m_Resources.getString("bundle.services.none"));
    } else {
      for (int i = 0; i < exported.length; i++) {
        ServiceReference service = exported[i];
        sbuf.append("Bundle #");
        sbuf.append(service.getBundle().getBundleId());
        sbuf.append(" ");
        sbuf.append(service.toString());
        sbuf.append("\n");
      }
    }
    sbuf.append("\n\n");
    return sbuf.toString();
  }//formatServices

  //TODO: Bundle and its version
  private String formatPackages(String exported, String imported) {
    final StringBuffer sbuf = new StringBuffer();
    sbuf.append(m_Resources.getString("bundle.packages.imported"));
    sbuf.append("\n\n");

    String[] str = (imported != null) ? imported.split(",") : null;
    if (str == null || str.length == 0) {
      sbuf.append(m_Resources.getString("bundle.packages.none"));
    } else {
      for (int i = 0; i < str.length; i++) {
        String s = str[i];
        sbuf.append(s);
        sbuf.append("\n");
      }
    }
    sbuf.append("\n\n");
    sbuf.append(m_Resources.getString("bundle.packages.exported"));
    sbuf.append("\n\n");
    str = (exported != null) ? exported.split(",") : null;
    if (str == null || str.length == 0) {
      sbuf.append(m_Resources.getString("bundle.packages.none"));
    } else {
      for (int i = 0; i < str.length; i++) {
        String s = str[i];
        sbuf.append(s);
        sbuf.append("\n");
      }
    }
    sbuf.append("\n\n");
    return sbuf.toString();
  }//formatPackages

  public void init() {
    log.debug("init()");
    Bundle[] bundles = m_BundleContext.getBundles();
    Object list = m_Thinlet.find(m_Thinlet.getActualPanel(), "bundles.list");
    if (list == null) {
      log.error("Could not find list.");
      return;
    }
    log.debug("Found " + bundles.length + " bundles.");
    for (int i = 0; i < bundles.length; i++) {
      Bundle bundle = bundles[i];
      addItem(list, bundle);
    }
    m_BundleListener = new BundleListenerImpl();
    m_BundleContext.addBundleListener(m_BundleListener);
    //get Startlevel service
    m_StartLevelRef = m_BundleContext.getServiceReference("org.osgi.service.startlevel.StartLevel");
    if (m_StartLevelRef == null) {
      log.error("Could not get StartLevel Service");
    } else {
      m_StartLevels = (StartLevel) m_BundleContext.getService(m_StartLevelRef);
    }

  }//initBundleList

  public void deinit() {
    m_BundleContext.removeBundleListener(m_BundleListener);
    m_BundleContext.ungetService(m_StartLevelRef);
    m_StartLevels = null;
    m_Bundles.clear();
    m_Bundles = null;
    m_BundleListener = null;
    m_BundleContext = null;
    m_Thinlet = null;
  }//deinit

  public void addItem(Object list, Bundle bundle) {
    Object item = Thinlet.create("item");
    m_Thinlet.add(list, item);
    m_Thinlet.setString(item, "text", bundle.getHeaders().get("Bundle-Name").toString());
    //m_Thinlet.setString(item, "tooltip", bundle.getHeaders().get("Bundle-Name").toString());
    log.debug("Adding " + bundle.getHeaders().get("Bundle-Name").toString());
    if (bundle.getHeaders().get("Bundle-Activator") != null) {
      if (bundle.getState() == Bundle.ACTIVE) {
        m_Thinlet.setIcon(item, "icon", m_Thinlet.getIcon("net/coalevo/manager/resources/images/bundle-started.png"));
      } else {
        m_Thinlet.setIcon(item, "icon", m_Thinlet.getIcon("net/coalevo/manager/resources/images/bundle-stopped.png"));
      }
    } else {
      m_Thinlet.setIcon(item, "icon", m_Thinlet.getIcon("net/coalevo/manager/resources/images/bundle-library.png"));
    }

    m_Bundles.put(new Long(bundle.getBundleId()), item);
  }//createItem

  public void setProgress(int percent) {
    final Object indicator = m_Thinlet.find(m_InstallDialog, "install.progress");
    m_Thinlet.setInteger(indicator, "value", percent);
  }//setProgress

  private class BundleListenerImpl
      implements BundleListener {

    public void bundleChanged(BundleEvent bundleEvent) {
      final Long bid = new Long(bundleEvent.getBundle().getBundleId());
      final Bundle bundle = bundleEvent.getBundle();
      Object item = null;
      switch (bundleEvent.getType()) {
        case BundleEvent.UNINSTALLED:
          if (m_Bundles.containsKey(bid)) {
            m_Thinlet.remove(m_Bundles.remove(bid));
          }
          break;
        case BundleEvent.STARTED:
          item = m_Bundles.get(bid);
          if (bundle.getHeaders().get("Bundle-Activator") != null) {
            m_Thinlet.setIcon(item, "icon", m_Thinlet.getIcon("net/coalevo/manager/resources/images/bundle-started.png"));
          }
          break;
        case BundleEvent.STOPPED:
          item = m_Bundles.get(bid);
          if (bundle.getHeaders().get("Bundle-Activator") != null) {
            m_Thinlet.setIcon(item, "icon", m_Thinlet.getIcon("net/coalevo/manager/resources/images/bundle-stopped.png"));
          }
          break;
        case BundleEvent.INSTALLED:
          addItem(m_Thinlet.find(m_Thinlet.getActualPanel(), "bundles.list"), bundle);
          break;
      }


    }//bundleChanged

  }//inner class BundleListenerImpl

/*

  void printMetaTypes(MetaTypeService mts, Bundle b) {
    if (mts == null) {
      log.error("MetaTypeService is null.");
      return;
    }
    if (b == null) {
      log.error("Bundle is null.");
      return;
    }
    MetaTypeInformation mti =
        mts.getMetaTypeInformation(b);
    if (mti == null) {
      log.error("No Metatype Information.");
      return;
    }
    String [] pids = mti.getPids();
    String [] factoryPids = mti.getFactoryPids();
    if (pids != null) {
      for (int i = 0; i < pids.length; i++) {
        String pid = pids[i];
        System.out.println("PID " + i + ":" + pid);
      }
    } else {
      System.out.println("No PIDS!");
      return;
    }
    String [] locales = mti.getLocales();
    if (factoryPids != null) {
      for (int i = 0; i < factoryPids.length; i++) {
        String pid = factoryPids[i];
        System.out.println("Factory PID " + i + ":" + pid);
      }
      if (locales == null) {
      System.out.println("Just default locale or none.");
      for (int i = 0; i < factoryPids.length; i++) {
        ObjectClassDefinition ocd =
            mti.getObjectClassDefinition(factoryPids[i], null);
        AttributeDefinition[] ads =
            ocd.getAttributeDefinitions(ObjectClassDefinition.ALL);
        for (int j = 0; j < ads.length; j++) {
          System.out.println("OCD=" + ocd.getName()
              + "AD=" + ads[j].getName());
        }
      }
    } else {
      for (int locale = 0; locale < locales.length; locale++) {
        System.out.println("Locale " + locales[locale]);
        for (int i = 0; i < factoryPids.length; i++) {
          ObjectClassDefinition ocd =
              mti.getObjectClassDefinition(factoryPids[i], locales[i]);
          AttributeDefinition[] ads =
              ocd.getAttributeDefinitions(ObjectClassDefinition.ALL);
          for (int j = 0; j < ads.length; j++) {
            System.out.println("OCD=" + ocd.getName()
                + "AD=" + ads[j].getName());
          }
        }
      }
    }

    }
    //String [] locales = new String[] {"","de","en"};
    if (locales == null) {
      System.out.println("Just default locale or none.");
      for (int i = 0; i < pids.length; i++) {
        ObjectClassDefinition ocd =
            mti.getObjectClassDefinition(pids[i], null);
        AttributeDefinition[] ads =
            ocd.getAttributeDefinitions(ObjectClassDefinition.ALL);
        for (int j = 0; j < ads.length; j++) {
          System.out.println("OCD=" + ocd.getName()
              + "AD=" + ads[j].getName());
        }
      }
    } else {
      for (int locale = 0; locale < locales.length; locale++) {
        System.out.println("Locale " + locales[locale]);
        for (int i = 0; i < pids.length; i++) {
          ObjectClassDefinition ocd =
              mti.getObjectClassDefinition(pids[i], locales[i]);
          AttributeDefinition[] ads =
              ocd.getAttributeDefinitions(ObjectClassDefinition.ALL);
          for (int j = 0; j < ads.length; j++) {
            System.out.println("OCD=" + ocd.getName()
                + "AD=" + ads[j].getName());
          }
        }
      }
    }
  }//printMetaTypes

*/

  class Loader implements Runnable {

    private String m_Location;
    private InputStream m_In;

    public Loader(String loc, InputStream in) {
      m_Location = loc;
      m_In = in;
    }//Loader

    public void run() {
      m_Loading = true;
      m_Cancelled = false;
      setProgress(0);
      try {
        m_BundleContext.installBundle(m_Location, m_In);
      } catch (BundleException e) {
        if (!m_Cancelled) {
          log.error("Loader::run()", e);
          m_Thinlet.showError(m_Resources.getString("bundle.install.failed") + " [" + e.getMessage() + "].");
          setProgress(0);
          return;
        } else {
          m_Thinlet.showInfo(m_Resources.getString("bundle.install.cancelled"));
          setProgress(0);
          return;
        }
      } finally {
        m_Loading = false;
      }
      installBundleDone();
    }//run

  }//inner class loader

}//class FrameworkHandler

