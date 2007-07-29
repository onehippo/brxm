package org.hippocms.repository.frontend.plugin;

import java.lang.reflect.Constructor;

import org.apache.wicket.model.IModel;
import org.hippocms.repository.frontend.model.JcrNodeModel;
import org.hippocms.repository.frontend.plugin.error.ErrorPlugin;

public class PluginFactory {

    private String classname;

    public PluginFactory(String classname) {
        this.classname = classname;
    }

    public Plugin getPlugin(String id, IModel model) {
        Plugin plugin;
        try {
            Class clazz = Class.forName(classname);
            Class[] formalArgs = new Class[] { String.class, JcrNodeModel.class };
            Constructor constructor = clazz.getConstructor(formalArgs);
            Object[] actualArgs = new Object[] { id, model };
            plugin = (Plugin) constructor.newInstance(actualArgs);
        } catch (Exception e) {
            String msg = e.getClass().getName() + ": " + e.getMessage();
            plugin = new ErrorPlugin(id, (JcrNodeModel)model, msg);
        }
        return plugin;
    }

}
