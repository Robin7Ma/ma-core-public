/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

import com.infiniteautomation.mango.io.serial.SerialPortConfigException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.vo.DataPointExtendedNameComparator;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.permission.Permissions;

/**
 * @author Terry Packer
 *
 */
public abstract class BaseDataSourceController extends ParameterizableViewController {
	
    BaseDataSourceController(String viewName, String errorViewName) {
        setViewName(viewName);
        setErrorViewName(errorViewName);
    }

    private String errorViewName;

    public void setErrorViewName(String errorViewName) {
        this.errorViewName = errorViewName;
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        DataSourceVO<?> dataSourceVO = null;
        User user = Common.getUser(request);
        // Create the model.
        Map<String, Object> model = new HashMap<>();

        // Get the id.
        int id = Common.NEW_ID;
        String idStr = request.getParameter("dsid");
        // Check for a data point id
        String pidStr = request.getParameter("dpid");
        //Get the Type
        String type = request.getParameter("typeId");

        //For legacy use of PID from the point details page
        if (request.getParameter("pid") != null)
            pidStr = request.getParameter("pid");

        //If type and dsid and pid are null then don't perform any actions here
        if ((idStr == null) && (pidStr == null) && (type == null)) {
            return new ModelAndView(getViewName(), model);
        }

        //Is this a new datasource?
        if (type != null) {
            if (StringUtils.isBlank(type)) {
                model.put("key", "dsEdit.error.noTypeProvided");
                return new ModelAndView(new RedirectView(errorViewName), model);
            }

            //Prepare the DS        
            Permissions.ensureDataSourcePermission(user);

            // A new data source
            DataSourceDefinition def = ModuleRegistry.getDataSourceDefinition(type);
            if (def == null)
                return new ModelAndView(new RedirectView(errorViewName));
            dataSourceVO = def.baseCreateDataSourceVO();
            dataSourceVO.setId(Common.NEW_ID);
            dataSourceVO.setXid(DataSourceDao.instance.generateUniqueXid());
        }

        //Are we going to be making a copy?
        String copyStr = request.getParameter("copy");

        //Are we editing a point?
        if (pidStr != null) {
            int pid = Integer.parseInt(pidStr);
            DataPointVO dp = DataPointDao.instance.getDataPoint(pid);
            if (dp == null) {
                // The requested data point doesn't exist. Return to the list page.
                model.put("key", "dsEdit.error.pointDNE");
                model.put("params", pid);
                return new ModelAndView(new RedirectView(errorViewName), model);
            }

            id = dp.getDataSourceId();

            //Now check to see if we are making a copy of this

            if (copyStr != null) {
                Boolean copy = Boolean.parseBoolean(copyStr);
                model.put("copy", copy);
                if (copy) {
                    String name = StringUtils.abbreviate(
                            TranslatableMessage.translate(Common.getTranslations(), "common.copyPrefix", dp.getName()),
                            40);

                    //Setup the Copy
                    DataPointVO copyDp = dp.copy();
                    copyDp.setId(Common.NEW_ID);
                    copyDp.setName(name);
                    copyDp.setXid(DataPointDao.instance.generateUniqueXid());
                    model.put("dataPoint", copyDp);
                }

            }
            else
                model.put("dataPoint", dp);
        }

        if (idStr != null) {
            // An existing configuration or copy
            id = Integer.parseInt(idStr);

        }

        if (id != Common.NEW_ID) {
            dataSourceVO = Common.runtimeManager.getDataSource(id);

            if (copyStr != null) {
                Boolean copy = Boolean.parseBoolean(copyStr);
                model.put("copy", copy);
                if (copy) {
                    String name = StringUtils.abbreviate(
                            TranslatableMessage.translate(Common.getTranslations(), "common.copyPrefix",
                                    dataSourceVO.getName()), 40);

                    //Setup the Copy
                    dataSourceVO = dataSourceVO.copy();
                    dataSourceVO.setId(Common.NEW_ID);
                    dataSourceVO.setName(name);
                    dataSourceVO.setXid(DataSourceDao.instance.generateUniqueXid());
                }
            }

            if (dataSourceVO == null) {
                // The requested data source doesn't exist. Return to the list page.
                model.put("key", "dsEdit.error.dataSourceDNE");
                model.put("params", id);
                return new ModelAndView(new RedirectView(errorViewName), model);

            }
            Permissions.ensureDataSourcePermission(user, dataSourceVO);
        }

        // Set the id of the data source in the user object for the DWR.
        user.setEditDataSource(dataSourceVO);

        // The data source
        if (dataSourceVO != null) {
            model.put("dataSource", dataSourceVO);
            model.put("modulePath", dataSourceVO.getDefinition().getModule().getWebPath());
            dataSourceVO.addEditContext(model);
        }

        // Reference data
        try {
            model.put("commPorts", Common.serialPortManager.getFreeCommPorts());
        }
        catch (SerialPortConfigException e) {
            model.put("commPortError", e.getMessage());
        }

        List<DataPointVO> allPoints = DataPointDao.instance.getDataPoints(DataPointExtendedNameComparator.instance, false);
        List<DataPointVO> userPoints = new LinkedList<>();
        List<DataPointVO> analogPoints = new LinkedList<>();
        for (DataPointVO dp : allPoints) {
            if (Permissions.hasDataPointReadPermission(user, dp)) {
                userPoints.add(dp);
                if (dp.getPointLocator().getDataTypeId() == DataTypes.NUMERIC)
                    analogPoints.add(dp);
            }
        }
        model.put("userPoints", userPoints);
        model.put("analogPoints", analogPoints);

        return new ModelAndView(getViewName(), model);
    }
}
