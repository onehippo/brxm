/*
 * Copyright 2007 Hippo
 *
 * Licensed under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hippoecm.frontend.dialog;

import java.lang.reflect.Constructor;

import org.apache.wicket.Page;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.PageCreator;
import org.hippoecm.frontend.dialog.error.ErrorDialog;

/**
 * Class for on-the-fly dialog creation based on a dynamic dialogClass attribute.
 */
public class DynamicDialogFactory implements PageCreator {
    private static final long serialVersionUID = 1L;

    private DialogWindow window;
    private Class dialogClass;

    public DynamicDialogFactory(DialogWindow window, Class dialogClass) {
        this.window = window;
        this.dialogClass = dialogClass;
    }

    public Page createPage() {
        Page result = null;
        if (dialogClass == null) {
            String msg = "No dialog renderer specified";
            result = new ErrorDialog(window, msg);
        } else {
            try {
                Class[] formalArgs = new Class[] { window.getClass() };
                Constructor constructor = dialogClass.getConstructor(formalArgs);
                Object[] actualArgs = new Object[] { window };
                result = (AbstractDialog) constructor.newInstance(actualArgs);
            } catch (Exception e) {
                String msg = e.getClass().getName() + ": " + e.getMessage();
                result = new ErrorDialog(window, msg);
            }
        }
        return result;
    }

    public void setDialogClass(Class dialogClass) {
        this.dialogClass = dialogClass;
    }

}
