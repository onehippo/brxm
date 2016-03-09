package org.onehippo.forge.beans;
/*
 * Copyright 2014 Hippo B.V. (http://www.onehippo.com)
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
import java.util.Calendar;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoDocument;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageSet;
import org.hippoecm.hst.content.beans.standard.HippoHtml;

@Node(jcrType="dashboarddocumentwizarddemo:eventsdocument")
public class EventsDocument extends HippoDocument {

    /**
     * The document type of the events document.
     */
    public final static String DOCUMENT_TYPE = "dashboarddocumentwizarddemo:eventsdocument";

    private final static String TITLE = "dashboarddocumentwizarddemo:title";
    private final static String DATE = "dashboarddocumentwizarddemo:date";
    private final static String INTRODUCTION = "dashboarddocumentwizarddemo:introduction";
    private final static String IMAGE = "dashboarddocumentwizarddemo:image";
    private final static String CONTENT = "dashboarddocumentwizarddemo:content";
    private final static String LOCATION = "dashboarddocumentwizarddemo:location";
    private final static String END_DATE = "dashboarddocumentwizarddemo:enddate";

    /**
     * Get the title of the document.
     *
     * @return the title
     */
    public String getTitle() {
        return getProperty(TITLE);
    }

    /**
     * Get the date of the document, i.e. the start date of the event.
     *
     * @return the (start) date
     */
    public Calendar getDate() {
        return getProperty(DATE);
    }

    /**
     * Get the introduction of the document.
     *
     * @return the introduction
     */
    public String getIntroduction() {
        return getProperty(INTRODUCTION);
    }

    /**
     * Get the image of the document.
     *
     * @return the image
     */
    public HippoGalleryImageSet getImage() {
        return getLinkedBean(IMAGE, HippoGalleryImageSet.class);
    }

    /**
     * Get the main content of the document.
     *
     * @return the content
     */
    public HippoHtml getContent() {
        return getHippoHtml(CONTENT);
    }

    /**
     * Get the location of the document.
     *
     * @return the location
     */
    public String getLocation() {
        return getProperty(LOCATION);
    }

    /**
     * Get the end date of the document, i.e. the end date of the event.
     *
     * @return the end date
     */
    public Calendar getEndDate() {
        return getProperty(END_DATE);
    }

}
