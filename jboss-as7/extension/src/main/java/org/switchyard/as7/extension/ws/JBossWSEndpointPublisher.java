/* 
 * JBoss, Home of Professional Open Source 
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved. 
 * See the copyright.txt in the distribution for a 
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use, 
 * modify, copy, or redistribute it subject to the terms and conditions 
 * of the GNU Lesser General Public License, v. 2.1. 
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details. 
 * You should have received a copy of the GNU Lesser General Public License, 
 * v.2.1 along with this distribution; if not, write to the Free Software 
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 */
 
package org.switchyard.as7.extension.ws;

import java.util.HashMap;
import java.util.Map;

import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerChainMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerChainsMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData;
import org.jboss.wsf.spi.metadata.webservices.PortComponentMetaData;
import org.jboss.wsf.spi.metadata.webservices.WebserviceDescriptionMetaData;
import org.jboss.wsf.spi.metadata.webservices.WebservicesMetaData;
import org.switchyard.component.soap.InboundHandler;
import org.switchyard.component.soap.WebServicePublishException;
import org.switchyard.component.soap.config.model.SOAPBindingModel;
import org.switchyard.component.soap.endpoint.AbstractEndpointPublisher;
import org.switchyard.component.soap.endpoint.WSEndpoint;

/**
 * Handles publishing of Webservice Endpoints on JBossWS stack.
 *
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2012 Red Hat Inc.
 */
public class JBossWSEndpointPublisher extends AbstractEndpointPublisher {

    private static final String SEI = "org.switchyard.component.soap.endpoint.BaseWebService";
    private static final String RESPONSE_STATUS_HANDLER = "ResponseStatusHandler";
    private static final String INBOUND_RESPONSE_HANDLER = "org.switchyard.component.soap.InboundResponseHandler";

    /**
     * {@inheritDoc}
     */
    public synchronized WSEndpoint publish(final SOAPBindingModel config, final String bindingId, final InboundHandler handler) {
        JBossWSEndpoint wsEndpoint = null;
        try {
            initialize(config);
            Map<String,String> map = new HashMap<String, String>();
            map.put("/" + config.getPort().getServiceName(), SEI);

            WebservicesMetaData wsMetadata = new WebservicesMetaData();
            WebserviceDescriptionMetaData wsDescMetaData = new WebserviceDescriptionMetaData(wsMetadata);
            wsDescMetaData.setWsdlFile(getWsdlLocation());
            PortComponentMetaData portComponent = new PortComponentMetaData(wsDescMetaData);
            portComponent.setPortComponentName(config.getServiceName() 
                                                + ":" + config.getPort().getServiceQName().getLocalPart() 
                                                + ":" + config.getPort().getPortQName().getLocalPart()); //unique ID
            portComponent.setServiceEndpointInterface(SEI);
            portComponent.setWsdlPort(config.getPort().getPortQName());
            portComponent.setWsdlService(config.getPort().getServiceQName());
             // Should be the WSDL's service name and not the SwitchYard config's service name
            portComponent.setServletLink(config.getPort().getServiceQName().getLocalPart());
            UnifiedHandlerChainsMetaData chains = new UnifiedHandlerChainsMetaData();
            UnifiedHandlerChainMetaData chainMetadata = new UnifiedHandlerChainMetaData();
            UnifiedHandlerMetaData handlerMetadata = new UnifiedHandlerMetaData();
            handlerMetadata.setHandlerName(RESPONSE_STATUS_HANDLER);
            handlerMetadata.setHandlerClass(INBOUND_RESPONSE_HANDLER);
            chainMetadata.addHandler(handlerMetadata);
            chains.addHandlerChain(chainMetadata);
            portComponent.setHandlerChains(chains);
            wsDescMetaData.addPortComponent(portComponent);
            wsMetadata.addWebserviceDescription(wsDescMetaData);

            wsEndpoint = new JBossWSEndpoint();
            if (config.getContextPath() != null) {
                wsEndpoint.publish(getContextRoot(), map, wsMetadata, config, handler);
            } else {
                wsEndpoint.publish(getContextPath(), map, wsMetadata, config, handler);
            }
        } catch (Exception e) {
            throw new WebServicePublishException(e);
        }
        return wsEndpoint;
    }
}