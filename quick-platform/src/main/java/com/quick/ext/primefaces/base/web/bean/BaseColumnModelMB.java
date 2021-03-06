package com.quick.ext.primefaces.base.web.bean;

import com.quick.ext.primefaces.base.service.BaseEJB;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import javax.persistence.Column;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.quick.ext.primefaces.base.util.MessageBundle;
import com.quick.ext.primefaces.base.web.BaseMB;
import com.quick.ext.primefaces.base.web.view.dao.BaseColumnModelSB;
import com.quick.ext.primefaces.base.web.view.entity.BaseColumnModel;
import com.quick.ext.primefaces.base.web.view.entity.BaseColumnModel_;
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.mgbean.BeanBuilder;
import com.sun.faces.mgbean.BeanManager;
import java.lang.reflect.Modifier;

@ManagedBean
@ViewScoped
public class BaseColumnModelMB extends BaseMB<BaseColumnModel, BaseColumnModel> {

    private BaseColumnModelSB baseColumnModelSB = new BaseColumnModelSB();

    @Override
    protected BaseEJB dao() {
        return baseColumnModelSB;
    }

    @Override
    protected SingularAttribute<BaseColumnModel, String> selectItemLabel() {
        return BaseColumnModel_.header;
    }

    private SelectItem[] entityItems;
    private SelectItem[] customeKeyItems;
    private SelectItem[] dataTypeItems;
    private SelectItem[] extTypeItems;
    private List<SelectItem> fieldItems;
    private SelectItem[] managedItems;
    private SelectItem[] managedBeanFields;

    private String managedClass;
    private Boolean hasManagerBean = Boolean.FALSE;
    private BaseColumnModel extBaseColumnModel;

    public void updateField(BaseColumnModel bcm) {
        this.extBaseColumnModel = bcm;
    }

    @Override
    public void reset() {
        managedClass = null;
        fieldItems = null;
        extBaseColumnModel = new BaseColumnModel();
        super.reset();
    }

    @Override
    protected String getGlobalFilterJpql() {
        return " and (o.entity like '%{text}%' or o.field like '%{text}%') and 1=1 ";
    }

    public void resetManagedBeanFields() {
        managedBeanFields = null;
    }

    public boolean getHasManagerBean() {
        hasManagerBean = StringUtils.isNotBlank(managedClass);
        return hasManagerBean;
    }

    public SelectItem[] getManagedBeanFields() throws ClassNotFoundException {
        if (ArrayUtils.isEmpty(managedBeanFields)) {
            if (StringUtils.isNotBlank(managedClass)) {
                Class<?> clz = Class.forName(managedClass);
                Method[] methods = clz.getMethods();
                List<SelectItem> ls = new ArrayList<>();
                ls.add(new SelectItem("", "select"));
                String field = "";
                for (Method f : methods) {
                    Class<?> returnType = f.getReturnType();
                    if ((returnType == java.util.List.class || returnType.isArray()) && Modifier.isPublic(f.getModifiers())) {
                        String beanElName = clz.getSimpleName().replace(clz.getSimpleName().charAt(0), Character.toLowerCase(clz.getSimpleName().charAt(0)));
                        (new StringBuilder()).append(Character.toLowerCase(clz.getSimpleName().charAt(0))).append(clz.getSimpleName().substring(1)).toString();
                        field = "#{" + beanElName + "." + f.getName() + "()}";
                        ls.add(new SelectItem(field, field));
                    }
                }
                managedBeanFields = new SelectItem[ls.size()];
                ls.toArray(managedBeanFields);
            }

        }
        return managedBeanFields;
    }

    public List<String> autocompleteExtListener(String str) throws ClassNotFoundException {
        List<String> methods = new ArrayList<String>();
        if (StringUtils.isNotBlank(managedClass)) {
            Class<?> clz = Class.forName(managedClass);
            Method[] fields = clz.getMethods();
            for (Method f : fields) {
                if (f.getReturnType().getName() != null && f.getReturnType().getName().equals("void") && f.getName().contains(str)) {
                    methods.add(f.getName());
                }
            }
        }

        return methods;
    }

    public SelectItem[] getManagedItems() {
        if (ArrayUtils.isEmpty(managedItems)) {
            ApplicationAssociate app = ApplicationAssociate.getCurrentInstance();
            BeanManager beanManager = app.getBeanManager();
            Map<String, BeanBuilder> rbs = beanManager.getRegisteredBeans();
            List<SelectItem> ls = new ArrayList<SelectItem>();

            Set<String> ss = rbs.keySet();
            String beanClass = "";
            for (String str : ss) {
                if (beanManager.isManaged(str)) {
                    beanClass = rbs.get(str).getBeanClass().getName();
                    ls.add(new SelectItem(beanClass, beanClass));
                }
            }
            managedItems = new SelectItem[ls.size()];
            ls.toArray(managedItems);
        }
        return managedItems;
    }

    public SelectItem[] getEntityItems() {
        if (ArrayUtils.isEmpty(entityItems)) {
            entityItems = baseColumnModelSB.getEntitysItems();
        }
        return entityItems;
    }

    public void resetFieldItems() {
        this.fieldItems = null;

    }

    public void fieldChangeListener() throws Exception {
        if (StringUtils.isNotBlank(extBaseColumnModel.getField())) {
            Class<?> cla = Class.forName(extBaseColumnModel.getEntity());
            Field field = cla.getDeclaredField(extBaseColumnModel.getField());
            String dataType = field.getType().getName();
            extBaseColumnModel.setDataType(dataType);

            Column column = field.getAnnotation(Column.class);
            if (column != null && StringUtils.isNotBlank(column.name())) {
                extBaseColumnModel.setHeader(column.name());
            } else {
                extBaseColumnModel.setHeader(field.getName().toUpperCase());
            }
        }
    }

    public List<SelectItem> getFieldItems() throws ClassNotFoundException {
        if (fieldItems == null || fieldItems.isEmpty()) {
            fieldItems = baseColumnModelSB.getFieldsItems(extBaseColumnModel.getEntity());
        }
        return fieldItems;
    }

    public SelectItem[] getExtTypeItems() {
        if (ArrayUtils.isEmpty(extTypeItems)) {
            extTypeItems = new SelectItem[3];
            extTypeItems[0] = new SelectItem("button", "Button");
            extTypeItems[1] = new SelectItem("select", "Select");
            extTypeItems[2] = new SelectItem("include", "Include");
            // TODO: move to app_config
        }
        return extTypeItems;
    }

    public SelectItem[] getCustomeKeyItems() {
        if (ArrayUtils.isEmpty(customeKeyItems)) {
            customeKeyItems = baseColumnModelSB.getCustomerKeyItems();
        }
        return customeKeyItems;
    }

    public SelectItem[] getDataTypeItems() {
        if (dataTypeItems == null || dataTypeItems.length == 0) {
            dataTypeItems = baseColumnModelSB.getDataTypeItems();
        }
        return dataTypeItems;
    }

    @PostConstruct
    public void init() {
    }

    public void createColumn() {
        if (extBaseColumnModel.getIsMultiFilter()) {
            if (StringUtils.isBlank(extBaseColumnModel.getFilterOptions())) {
                MessageBundle.showWarning("Fill Warning", "if Multi Select Filter is Yes, the Filter DropDown is required");
                return;
            }
        }
        // TODO: validation
        if (extBaseColumnModel.getIsExtButton()) {

        } else if (extBaseColumnModel.getIsExtInclude()) {

        } else if (extBaseColumnModel.getIsExtSelect()) {

        }

        FacesMessage fm = dao().create(extBaseColumnModel);
        if (fm.getSeverity().equals(FacesMessage.SEVERITY_INFO)) {
            reset();
        }
        MessageBundle.autoMessage(fm);
    }

    public void updateColumn() {
        if (extBaseColumnModel.getIsMultiFilter()) {
            if (StringUtils.isBlank(extBaseColumnModel.getFilterOptions())) {
                MessageBundle.showWarning("Fill Warning", "if Multi Select Filter is Yes, the Filter DropDown is required");
                return;
            }
        }
        FacesMessage fm = dao().update(extBaseColumnModel);
        if (fm.getSeverity().equals(FacesMessage.SEVERITY_INFO)) {
            reset();
        }
        MessageBundle.autoMessage(fm);
    }

    public void initColumns() {
        baseColumnModelSB.initNewViewColumns();
        super.initViewColums();
        MessageBundle.showInfo("init operation", "completed");
    }

    public void testExtentsion(BaseColumnModel entityId) {
        MessageBundle.showInfo("test sucess: id:" + entityId.getId());
    }

    public BaseColumnModel getExtBaseColumnModel() {
        if (extBaseColumnModel == null) {
            extBaseColumnModel = new BaseColumnModel();
        }
        return extBaseColumnModel;
    }

    public void setExtBaseColumnModel(BaseColumnModel extBaseColumnModel) {
        this.extBaseColumnModel = extBaseColumnModel;
    }

    public String getManagedClass() {
        return managedClass;
    }

    public void setManagedClass(String managedClass) {
        this.managedClass = managedClass;
    }

    /**
     * in testting function
     */
    public void batchSetup() {
        for (BaseColumnModel c : getEntitys()) {
            c.setEdit(getEntity().getEdit());
        }
        try {
            dao().txBegin();
            dao().updates(getEntitys());
            dao().txCommit();
        } catch (Exception e) {
            MessageBundle.showError("操作失败");
            logger.error("操作失败", e);
            try {
                dao().txRollback();
            } catch (Exception ex) {
                logger.error("回滚失败");
                logger.error("回滚失败", ex);
            }
        }
    }
}
