Ext.namespace('Hippo.App');

Hippo.App.PropertiesPanel = Ext.extend(Ext.FormPanel, {
    initComponent:function() {
        Ext.apply(this, {
            autoHeight: true,
            bodyStyle: 'background-color:#fff;',
            border:false,
            padding: 10,
            autoScroll:true,
            labelWidth: 250,
            labelAlign: 'top',
            defaults:{
                width: 250
            },
            readonly: [1],
            path: ""

        });
        Hippo.App.PropertiesPanel.superclass.initComponent.apply(this, arguments);
    },

    submitForm:function () {
        this.getForm().submit({
            url: 'services/PropertiesService' + this.path,
            method: 'POST'
        });
    },

    onRender:function() {
        Hippo.App.PropertiesPanel.superclass.onRender.apply(this, arguments);
    },

    loadProperties:function(store, records, options) {
        this.removeAll();

        var length = records.length;
        if (length == 0) {
            this.add({
                html: "No Properties Found for this component",
                xtype: "panel"
            });
        } else {
            this.addButton({text: "Save"}, this.submitForm, this);
            for (var i = 0; i < length; ++i) {
                var property = records[i];
                this.add({
                    fieldLabel: property.get('label'),
                    xtype: 'textfield',
                    labelStyle: 'font-weight:bold;',
                    value: property.get('value'),
                    allowBlank: !property.get('required'),
                    name: property.get('name')
                });
            }
        }

        this.doLayout(false, true);
    },

    loadException:function(proxy, type, actions, options, response) {
        console.dir(arguments);
        this.removeAll();

        for (var i = 0; i < this.readonly.length; ++i) {
            var property = this.readonly[i];
            this.add({
                fieldLabel: property['name'],
                xtype: 'textfield',
                labelStyle: 'font-weight:bold;',
                value: property['value'],
                readOnly: true
            });
        }

        var errorText = 'Error during ' + actions + '. ';
        if (type == 'response') {
            errorText += '\nServer returned statusText: ' + response.statusText + ', statusCode: '
                    + response.status + ' for request.url=' + options.url;
        }

        this.add({
            xtype: 'label',
            text: errorText,
            labelStyle: 'font-weight:bold;',
            fieldLabel: 'Error information'
        });

        this.doLayout(false, true);
    },

    reload:function(id, name, path) {
        this.path = path;
        this.readonly[0] = {
            name: 'Name',
            value: name
        };

        var store = new Ext.data.JsonStore({
            autoLoad: true,
            method: 'GET',
            root: 'properties',
            fields:['name', 'value', 'label', 'required', 'description', 'value', 'type' ],
            url: 'services/PropertiesService' + path
        });
        store.on('load', this.loadProperties, this);
        store.on('exception', this.loadException, this);
    }

});
Ext.reg('h_properties_panel', Hippo.App.PropertiesPanel);
