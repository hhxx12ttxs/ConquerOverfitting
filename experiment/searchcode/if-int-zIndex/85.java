package ua.orion.web.mixins;

import javax.inject.Inject;
import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.BindParameter;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.corelib.components.ActionLink;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.ajax.JavaScriptCallback;
import org.apache.tapestry5.services.javascript.InitializationPriority;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import ua.orion.web.OrionWebSymbols;

/**
 * ?????????????: - ?????? ???? ??????????? ?????; - ????????? ActionLink ???
 * Zone ??????? ??????? ? ??????????? ????. ?? ???? JQuery-UI dialog.
 * (http://jqueryui.com/demos/dialog http://www.linkexchanger.su/2009/95.html)
 * ????? ???????????: - ? ???? ??? ????????? ????????????? ??????? (??? ??
 * ???????????); - ? ActionLink ??? ????????????????? ? ?????? ????????
 * (????????, ?????? ? ?.?.)
 *
 * @author slobodyanuk
 */
//TODO ???????? ?????? ???????? JQuery-UI
public class Dialog {

    @Parameter(defaultPrefix = BindingConstants.LITERAL, allowNull = false)
    private String event;
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String dialogTitle;
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private boolean modal;
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private int width;
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private int height;
    @Parameter(defaultPrefix = BindingConstants.LITERAL, value = "symbol:" + OrionWebSymbols.UI_DIALOG_SHOW)
    private String show;
    @Parameter(defaultPrefix = BindingConstants.LITERAL, value = "symbol:" + OrionWebSymbols.UI_DIALOG_HIDE)
    private String hide;
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private boolean closeOnEscape;
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private boolean draggable;
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private boolean resizable;
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private int zIndex;
    @InjectContainer
    private Object container;
    @Inject
    private ComponentResources resources;
    @Inject
    private AjaxResponseRenderer ajaxResponseRenderer;
    @Inject
    private JavaScriptSupport javaScriptSupport;
    @BindParameter(value = {"zone", "id"})
    private String zone;

    void beginRender() {
        if (container instanceof ActionLink) {
            if (!resources.isBound("event")) {
                throw new RuntimeException("'event' parameter is required");
            }
            if (!resources.getContainerResources().isBound("zone")) {
                throw new RuntimeException("'zone' parameter for ActionLink is required");
            }
        } else if (container instanceof Zone) {
            javaScriptSupport.addInitializerCall(InitializationPriority.LATE, "oriDialog", initJSONObject());
        } else {
            throw new RuntimeException("'Dialog' mixin must be used on 'ActionLink' or 'Zone' component");
        }
    }

    void onAction(EventContext ec) {
        ajaxResponseRenderer.addCallback(new JavaScriptCallback() {

            @Override
            public void run(JavaScriptSupport js) {
                JSONObject init = initJSONObject();
                init.put("oriEvent", event);
                js.addInitializerCall(InitializationPriority.LATE, "oriDialog", init);
            }
        });
    }

    private JSONObject initJSONObject() {
        JSONObject init = new JSONObject();
        init.put("oriId", zone);
        if (resources.isBound("dialogTitle")) {
            init.put("title", dialogTitle);
        }
        if (resources.isBound("modal")) {
            init.put("modal", modal);
        }
        if (!"".equals(show)) {
            init.put("show", show);
        }
        if (!"".equals(hide)) {
            init.put("hide", hide);
        }
        if (resources.isBound("height")) {
            init.put("height", height);
        }
        if (resources.isBound("width")) {
            init.put("width", width);
        }
        if (resources.isBound("closeOnEscape")) {
            init.put("closeOnEscape", closeOnEscape);
        }
        if (resources.isBound("draggable")) {
            init.put("draggable", draggable);
        }
        if (resources.isBound("height")) {
            init.put("resizable", resizable);
        }
        if (resources.isBound("zIndex")) {
            init.put("zIndex", zIndex);
        }
        return init;
    }

    /**
     * ?? ???????????? ???? ????
     * @return 
     */
    boolean beforeRenderBody() {
        return !(container instanceof Zone);
    }
}

