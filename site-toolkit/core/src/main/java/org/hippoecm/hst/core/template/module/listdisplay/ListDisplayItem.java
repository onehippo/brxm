package org.hippoecm.hst.core.template.module.listdisplay;

import javax.jcr.Node;

import org.hippoecm.hst.core.template.node.el.AbstractELNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListDisplayItem extends AbstractELNode{
	private static final Logger log = LoggerFactory.getLogger(ListDisplayItem.class);
	
    public ListDisplayItem(Node node) {
    	super(null, node); 
    }
    
}
