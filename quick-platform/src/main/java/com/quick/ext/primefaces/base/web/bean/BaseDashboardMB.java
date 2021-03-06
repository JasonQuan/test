package com.quick.ext.primefaces.base.web.bean;

import com.quick.ext.primefaces.base.web.view.dao.DashboardSB;
import com.quick.ext.primefaces.base.web.view.dao.PanelModelSB;
import com.quick.ext.primefaces.base.web.view.entity.BaseAjaxModel;
import com.quick.ext.primefaces.base.web.view.entity.BaseDashboardModel;
import com.quick.ext.primefaces.base.web.view.entity.BasePanelModel;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;
import org.primefaces.behavior.ajax.AjaxBehavior;
import org.primefaces.behavior.ajax.AjaxBehaviorListenerImpl;
import org.primefaces.component.dashboard.Dashboard;
import org.primefaces.component.panel.Panel;
import org.primefaces.event.ToggleEvent;

/**
 *
 * @author Jason
 */
public abstract class BaseDashboardMB {

    public final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(BaseDashboardMB.class);
    private Dashboard dashboard;
    private List<BasePanelModel> panels;
    private BaseDashboardModel basedashboard;
    private DashboardSB dashboardSB = new DashboardSB();
    private PanelModelSB panelModelSB = new PanelModelSB();

    public void ajaxListener() {
    }

    protected String getCustomeKey() {
        return "default";
    }

    public void restoreDashboard() {
        StringBuilder sb = new StringBuilder();
        for (BasePanelModel p : panels) {
            sb.append(p.getColumnIndex()).append(",").append(p.getId()).append(",").append(p.getItemIndex()).append("|");
        }
        if (sb.length() != 0) {
            sb.replace(sb.length() - 1, sb.length(), "");
            dashboardSB.update(basedashboard.getId(), "details", sb.toString());
            basedashboard = dashboardSB.findSingleByField("customeKey", getCustomeKey());
        } else {
            logger.info("panel config not found with customeKey: " + getCustomeKey());
        }
    }

    @PostConstruct
    public void init() {
        panels = panelModelSB.findByField("customeKey", getCustomeKey());
        basedashboard = dashboardSB.findSingleByField("customeKey", getCustomeKey());
        if (basedashboard == null) {
            logger.info("dashboard config not found with customeKey: " + getCustomeKey());
            return;
        }
        if (basedashboard.getDetails() == null || basedashboard.getDetails().equals("")) {
            restoreDashboard();
        }
        dashboard = new Dashboard();
        dashboard.setModel(basedashboard);
        dashboard.setStyle(basedashboard.getStyle());
        dashboard.setStyleClass(basedashboard.getStyleClass());
        dashboard.setDisabled(basedashboard.getDisabled());
        FacesContext fc = FacesContext.getCurrentInstance();
        Application application = fc.getApplication();
        ELContext elContext = fc.getELContext();
        FaceletContext faceletContext = (FaceletContext) FacesContext.getCurrentInstance().getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
        for (BasePanelModel p : panels) {
            Panel panel = (Panel) application.createComponent(fc, "org.primefaces.component.Panel", "org.primefaces.component.PanelRenderer");
            panel.setId(p.getId());
            panel.setHeader(p.getHeader());
            panel.setClosable(p.getClosable());
            panel.setToggleable(p.getToggleable());
            panel.setStyle(p.getStyle());
            panel.setToggleOrientation(p.getToggleOrientation());
            panel.setCollapsed(p.getCollapsed());
            panel.setStyleClass(p.getStyleClass());//TODO: COPY other fields

            ExpressionFactory ef = fc.getApplication().getExpressionFactory();
            for (BaseAjaxModel a : p.getAjaxModels()) {
                AjaxBehavior behavior = new AjaxBehavior();
                behavior.setOncomplete(a.getOncomplete());
                behavior.setOnsuccess(a.getOnsuccess());
                MethodExpression ex = ef.createMethodExpression(elContext, a.getListener() != null ? a.getListener() : "#{request.removeAttribute('no')}", null, new Class[]{});
                behavior.addAjaxBehaviorListener(new AjaxBehaviorListenerImpl(ex, ex));
                panel.addClientBehavior(a.getEvent(), behavior);
            }
            getDashboard().getChildren().add(panel);
            if (p.getInclude() != null) {
                try {
                    faceletContext.includeFacelet(panel, p.getInclude());
                } catch (Exception ex) {
                    logger.error("ui include error with panel : " + p.getId() + " src " + p.getInclude());
                }
            }
        }
    }

    public Dashboard getDashboard() {
        return dashboard;
    }

    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
    }

    public List<BasePanelModel> getPanels() {
        return panels;
    }

    public void setPanels(List<BasePanelModel> panels) {
        this.panels = panels;
    }

    public void updateOrder() {
        String dashboardOrder = FacesContext.getCurrentInstance().
                getExternalContext().getRequestParameterMap().get("newDashboardOrder");
        basedashboard = dashboardSB.find(basedashboard.getId());
        dashboardSB.update(basedashboard.getId(), "details", dashboardOrder);
    }

    /**
     * in testing
     *
     * @param event
     */
    public void onToggle(ToggleEvent event) {
        panelModelSB.update(event.getComponent().getId(), "collapsed", event.getVisibility().name().equals("VISIBLE"));
    }
}
