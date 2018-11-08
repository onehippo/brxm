/*
 * Copyright 2018 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onehippo.cms7.essentials.dashboard.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Used to mark source code that has been generated by Essentials' BeanWriter tool.
 *
 * This annotation lives in this odd place/package in order to preserve backwards compatibility. The annotation
 * used to live in the Essentials project, but that fact caused the archetype to have the site webapp depend on
 * /pull in the Essentials plugin SDK API, which was even more ugly.
 */

@Documented
@Retention(SOURCE)
@Target({PACKAGE, TYPE, ANNOTATION_TYPE, METHOD, CONSTRUCTOR, FIELD,
        LOCAL_VARIABLE, PARAMETER})
public @interface HippoEssentialsGenerated {

    /**
     * Name used by Essentials' BeanWriter tool.
     *
     * User can safely rename generated property (e.g. class name, method name),
     * HippoEssentials framework will check internal name before generating mentioned property
     *
     * @return internal name of generated code
     */
    String internalName() default "";

    /**
     * Indicates if generated code may be modified by Essentials' BeanWriter tool.
     *
     * @return true by default
     */
    boolean allowModifications() default true;
}
