/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.vo.event;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.handlers.EventHandlerRT;
import com.serotonin.m2m2.rt.event.handlers.ProcessHandlerRT;
import com.serotonin.m2m2.rt.event.type.AuditEventType;
import com.serotonin.util.SerializationHelper;

/**
 * @author Terry Packer
 *
 */
public class ProcessEventHandlerVO extends AbstractEventHandlerVO{

	private String activeProcessCommand;
    private int activeProcessTimeout = 15;
    private String inactiveProcessCommand;
    private int inactiveProcessTimeout = 15;
    
	public String getActiveProcessCommand() {
        return activeProcessCommand;
    }

    public void setActiveProcessCommand(String activeProcessCommand) {
        this.activeProcessCommand = activeProcessCommand;
    }

    public int getActiveProcessTimeout() {
        return activeProcessTimeout;
    }

    public void setActiveProcessTimeout(int activeProcessTimeout) {
        this.activeProcessTimeout = activeProcessTimeout;
    }

    public String getInactiveProcessCommand() {
        return inactiveProcessCommand;
    }

    public void setInactiveProcessCommand(String inactiveProcessCommand) {
        this.inactiveProcessCommand = inactiveProcessCommand;
    }

    public int getInactiveProcessTimeout() {
        return inactiveProcessTimeout;
    }

    public void setInactiveProcessTimeout(int inactiveProcessTimeout) {
        this.inactiveProcessTimeout = inactiveProcessTimeout;
    }
    
    @Override
    public void validate(ProcessResult response) {
    	super.validate(response);
        if (StringUtils.isBlank(activeProcessCommand) && StringUtils.isBlank(inactiveProcessCommand))
            response.addGenericMessage("eventHandlers.invalidCommands");

        if (!StringUtils.isBlank(activeProcessCommand) && activeProcessTimeout <= 0)
            response.addGenericMessage("validate.greaterThanZero");

        if (!StringUtils.isBlank(inactiveProcessCommand) && inactiveProcessTimeout <= 0)
            response.addGenericMessage("validate.greaterThanZero");
    }
    
    @Override
    public void addProperties(List<TranslatableMessage> list) {
    	super.addProperties(list);
        AuditEventType.addPropertyMessage(list, "eventHandlers.activeCommand", activeProcessCommand);
        AuditEventType.addPropertyMessage(list, "eventHandlers.activeTimeout", activeProcessTimeout);
        AuditEventType.addPropertyMessage(list, "eventHandlers.inactiveCommand", inactiveProcessCommand);
        AuditEventType.addPropertyMessage(list, "eventHandlers.inactiveTimeout", inactiveProcessTimeout);
    }
    
    @Override
    public void addPropertyChanges(List<TranslatableMessage> list, AbstractEventHandlerVO vo) {
        super.addPropertyChanges(list, vo);
        
        ProcessEventHandlerVO from = (ProcessEventHandlerVO)vo;
    	AuditEventType.maybeAddPropertyChangeMessage(list, "eventHandlers.activeCommand",
                from.activeProcessCommand, activeProcessCommand);
        AuditEventType.maybeAddPropertyChangeMessage(list, "eventHandlers.activeTimeout",
                from.activeProcessTimeout, activeProcessTimeout);
        AuditEventType.maybeAddPropertyChangeMessage(list, "eventHandlers.inactiveCommand",
                from.inactiveProcessCommand, inactiveProcessCommand);
        AuditEventType.maybeAddPropertyChangeMessage(list, "eventHandlers.inactiveTimeout",
                from.inactiveProcessTimeout, inactiveProcessTimeout);
    }
    
    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
    	out.writeInt(version);
        SerializationHelper.writeSafeUTF(out, activeProcessCommand);
        out.writeInt(activeProcessTimeout);
        SerializationHelper.writeSafeUTF(out, inactiveProcessCommand);
        out.writeInt(inactiveProcessTimeout);
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();
        if(ver == 1){
		    activeProcessCommand = SerializationHelper.readSafeUTF(in);
		    activeProcessTimeout = in.readInt();
		    inactiveProcessCommand = SerializationHelper.readSafeUTF(in);
		    inactiveProcessTimeout = in.readInt();
        }
    }
    
    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
    	super.jsonWrite(writer);
        writer.writeEntry("activeProcessCommand", activeProcessCommand);
        writer.writeEntry("activeProcessTimeout", activeProcessTimeout);
        writer.writeEntry("inactiveProcessCommand", inactiveProcessCommand);
        writer.writeEntry("inactiveProcessTimeout", inactiveProcessTimeout);
    }
    
    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
    	super.jsonRead(reader, jsonObject);
        String text = jsonObject.getString("activeProcessCommand");
        if (text != null)
            activeProcessCommand = text;

        Integer i = jsonObject.getInt("activeProcessTimeout");
        if (i != null)
            activeProcessTimeout = i;

        text = jsonObject.getString("inactiveProcessCommand");
        if (text != null)
            inactiveProcessCommand = text;

        i = jsonObject.getInt("inactiveProcessTimeout");
        if (i != null)
            inactiveProcessTimeout = i;

    }
    
    @Override
    public EventHandlerRT<ProcessEventHandlerVO> createRuntime(){
    	return new ProcessHandlerRT(this);
    }

}
