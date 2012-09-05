/*
 *  Copyright 2008 Hippo.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hippoecm.repository.reviewedactions;

import java.util.Map;

import javax.jcr.Value;

public class DerivedDataFunction extends org.hippoecm.repository.ext.DerivedDataFunction {

    public Map<String,Value[]> compute(Map<String,Value[]> parameters) {
        String rtvalue;
        if(parameters.containsKey("request")) {
            rtvalue = "review";
        } else {
            if(parameters.containsKey("unpublished")) {
                if(parameters.containsKey("published")) {
                    rtvalue = "changed";
                } else {
                    rtvalue = "new";
                }
            } else {
                if(parameters.containsKey("published")) {
                    rtvalue = "live";
                } else {
                    rtvalue = "new";
                }
            }
        }
        parameters.put("summary", new Value[] { getValueFactory().createValue(rtvalue) });
        return parameters;
    }
}
