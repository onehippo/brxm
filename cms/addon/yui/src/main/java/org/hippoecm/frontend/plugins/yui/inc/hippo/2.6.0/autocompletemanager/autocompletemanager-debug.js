/*
 * Copyright 2008 Hippo
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

/**
 * @description
 * <p>
 * Todo
 * </p>
 * @namespace YAHOO.hippo
 * @requires hippoautocomplete, functionqueue, hashmap
 * @module autocompletemanager
 */

YAHOO.namespace('hippo');

if (!YAHOO.hippo.AutoCompleteManager) {
    ( function() {
        var Dom = YAHOO.util.Dom, Lang = YAHOO.lang;
        
        YAHOO.hippo.AutoCompleteManagerImpl = function() {
        };
        
        YAHOO.hippo.AutoCompleteManagerImpl.prototype = {
        		
			loader       : new YAHOO.hippo.FunctionQueue('AutoCompleteQueue'),
			instances  : new YAHOO.hippo.HashMap(),
			
			onLoad : function() {
			    this.cleanup();
				this.loader.handleQueue();
			},
			
			cleanup: function() {
				//remove old autoc components from dom and maps
			},
			
			add: function(id, clazz, config) {
				YAHOO.log("Add autocomplete component[" + id + "] of type " + clazz, "info", "AutoCompleteManager");
				this._add(id, clazz, config, this.instances);
			},

			_add: function(id, clazz, config, map) {
				var func = function() {
					var c = new clazz(id, config);
					map.put(id, c);
				};
				this.loader.registerFunction(func);
			}
			
        };
    })();

    YAHOO.hippo.AutoCompleteManager = new YAHOO.hippo.AutoCompleteManagerImpl();
    YAHOO.register("layoutmanager", YAHOO.hippo.LayoutManager, {
        version :"2.6.0",
        build :"1321"
    });
}