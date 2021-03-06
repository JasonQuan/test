package com.quick.ext.primefaces.converter;

import java.io.Serializable;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author Jason
 */
@FacesConverter(value = "com.primefaces.ext.converter.BooleanConverter")
public class BooleanConverter implements Converter, Serializable {

    @Override
    public Boolean getAsObject(FacesContext context, UIComponent component, String value) {
        return Boolean.valueOf(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof Boolean) {
            return ((Boolean) value) ? "Yes" : "No";
        }
        return "No";
    }

}
