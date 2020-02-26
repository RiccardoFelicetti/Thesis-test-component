package org.eclipse.kura.wire.component.test.provider;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.kura.configuration.ConfigurableComponent;

import org.eclipse.kura.type.TypedValue;
import org.eclipse.kura.wire.WireComponent;
import org.eclipse.kura.wire.WireEmitter;
import org.eclipse.kura.wire.WireEnvelope;
import org.eclipse.kura.wire.WireHelperService;
import org.eclipse.kura.wire.WireReceiver;
import org.eclipse.kura.wire.WireRecord;
import org.eclipse.kura.wire.WireSupport;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.wireadmin.Wire;

import org.slf4j.LoggerFactory;




public final class Test implements WireReceiver, WireEmitter, ConfigurableComponent {
	
	 private static final Logger logger = LogManager.getLogger(Test.class);
	
	 private volatile WireHelperService wireHelperService;
	 
	 private WireSupport wireSupport;
	 private Map<String, Object> properties;
	 private WireEnvelope old_envelope;
	
	 
	 
	 
	 /**
	     * Binds the Wire Helper Service.
	     *
	     * @param wireHelperService
	     *            the new Wire Helper Service
	     */
	    public void bindWireHelperService(final WireHelperService wireHelperService) {
            logger.debug("SET WIRE HELPER SERVICE");

	    	if (isNull(this.wireHelperService)) {
	            this.wireHelperService = wireHelperService;
	            logger.debug("SET WIRE HELPER SERVICE");
	        }
	    }

	    /**
	     * Unbinds the Wire Helper Service.
	     *
	     * @param wireHelperService
	     *            the new Wire Helper Service
	     */
	    public void unbindWireHelperService(final WireHelperService wireHelperService) {
	        if (this.wireHelperService == wireHelperService) {
	            this.wireHelperService = null;
	        }
	    }
	    
	    /**
	     * OSGi Service Component callback for activation.
	     *
	     * @param componentContext
	     *            the component context
	     * @param properties
	     *            the properties
	     */
	    protected void activate(final ComponentContext componentContext, final Map<String, Object> properties) {
	        logger.debug("Activating Test Wire Component...");
	        this.properties = properties;
	        this.wireSupport = this.wireHelperService.newWireSupport(this,
	                (ServiceReference<WireComponent>) componentContext.getServiceReference());
	        logger.debug("Activating Test Wire Component... Done");
	    }
	    

	    /**
	     * OSGi Service Component callback for updating.
	     *
	     * @param properties
	     *            the updated properties
	     */
	    public void updated(final Map<String, Object> properties) {
	        logger.debug("Updating Test Wire Component...");
	        this.properties = properties;
	        logger.debug("Updating Test Wire Component... Done");
	    }

	    
	    /**
	     * OSGi Service Component callback for deactivation.
	     *
	     * @param componentContext
	     *            the component context
	     */
	    protected void deactivate(final ComponentContext componentContext) {
	        logger.debug("Deactivating Test Wire Component...");
	        // remained for debugging purposes	        
	        logger.debug("Deactivating Test Wire Component... Done");
	    }
	    
	    /** {@inheritDoc} */
	    @Override
	    public void onWireReceive(final WireEnvelope wireEnvelope) {
	    	requireNonNull(wireEnvelope, "Wire Envelope cannot be null");	    		    	
	    	logger.info("Test: Received WireEnvelope from " + wireEnvelope.getEmitterPid());
	        
	    	//If envelopes are different, log and emit values
	    	if(isNull(old_envelope)|| !equal(old_envelope, wireEnvelope)) {
	    	  	       
	    		logger.info("Record List content: ");
	            
	        	for (WireRecord record : wireEnvelope.getRecords()) {
	                logger.info("  Record content: ");

	                for (Entry<String, TypedValue<?>> entry : record.getProperties().entrySet()) {
	                    logger.info("    "+ entry.getKey()+ ": " +entry.getValue().getValue());
	                }
	            }
	            logger.info("");
	            this.emitEnvelope(wireEnvelope);
	            
	    	}else {
	    		
	    		logger.info("NO CHANGE");
	    	}
	    	
	        old_envelope= wireEnvelope;
	    }
	    

	    /** {@inheritDoc} */
	    @Override
	    public void producersConnected(final Wire[] wires) {
	        this.wireSupport.producersConnected(wires);
	    }
	    
	    /** {@inheritDoc} */
	    @Override
	    public void updated(final Wire wire, final Object value) {
	        this.wireSupport.updated(wire, value);
	    }
	
	   
	    /** {@inheritDoc} */
	    @Override
	    public void consumersConnected(final Wire[] wires) {
	        this.wireSupport.consumersConnected(wires);
	    }
	    
	    /** {@inheritDoc} */
	    @Override
	    public Object polled(final Wire wires) {
	        return this.wireSupport.polled(wires);
	    }
	    
	    
	    
	    //Compare method	    
	    private boolean equal(WireEnvelope wireEnvelope1, WireEnvelope wireEnvelope2) {
	    	boolean result=true;
	    	
	    	
	    	if (isNull(wireEnvelope1)) return false;
	    	
	    	
	    	
	    	//Loop on record of first envelope	    	
	    	for(WireRecord record: wireEnvelope1.getRecords()) {
	    		for(Entry<String, TypedValue<?>> entry : record.getProperties().entrySet()) {
	    			
	    			//Get index and key of record and entry of the first envelope
	    			int record_index = wireEnvelope1.getRecords().indexOf(record);
	    			String key = entry.getKey();
	    			
	    			//Get entry of second envelope
	    			Map<String, TypedValue<?>> entry2 = wireEnvelope2.getRecords().get(record_index).getProperties();
	    			
	    			//Entry name-value
	    			//Entry2 value
	    			
	    			//logger.info("ENTRY1 "+ entry.getValue().toString());
	    			//logger.info("ENTRY2 "+ entry2.get(key).toString() );
	    			
	    			
	    			//Compare entries
	    			if(!entry.getValue().equals(entry2.get(key))) {
	    				
	    				logger.info("Test: change on "+ key);
	    				return false;
	    				
	    			}
	    			
	    		}
	    	}
	    	
	    	
	    	return result;
	    }
	    
	    private void emitEnvelope(WireEnvelope wireEnvelope) {    	
	    	
	    	
	    	this.wireSupport.emit(wireEnvelope.getRecords());    	
	    	
	    		
	    	}

}
