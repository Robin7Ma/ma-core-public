/**
 * Copyright (C) 2013 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.dwr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import com.infiniteautomation.mango.db.query.SortOption;
import com.serotonin.db.pair.StringStringPair;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.LicenseViolatedException;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.ResultsWithTotal;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.EventDetectorDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueFacade;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.util.UnitUtil;
import com.serotonin.m2m2.view.ImplDefinition;
import com.serotonin.m2m2.view.chart.BaseChartRenderer;
import com.serotonin.m2m2.view.text.BaseTextRenderer;
import com.serotonin.m2m2.vo.DataPointNameComparator;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.event.detector.AbstractEventDetectorVO;
import com.serotonin.m2m2.vo.event.detector.AbstractPointEventDetectorVO;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.dojo.DojoMemoryStoreListItem;
import com.serotonin.m2m2.web.dwr.beans.RenderedPointValueTime;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;
import com.serotonin.m2m2.web.taglib.Functions;

/**
 * @author Terry Packer
 *
 */
public class DataPointDwr extends AbstractDwr<DataPointVO, DataPointDao> {

    /**
     * Default Constructor
     */
    public DataPointDwr() {
        super(DataPointDao.instance, "dataPoints");
        LOG = LogFactory.getLog(DataPointDwr.class);
    }

    @DwrPermission(user = true)
    public ProcessResult getPoints() {
        ProcessResult result = new ProcessResult();

        User user = Common.getUser();
        if (user == null) {
            result.addData("list", new ArrayList<DataPointVO>());
            return result;
        }

        DataSourceVO<?> ds = user.getEditDataSource();
        if (ds.getId() == Common.NEW_ID) {
            result.addData("list", new ArrayList<DataPointVO>());
            return result;
        }

        List<DataPointVO> points = DataPointDao.instance
                .getDataPoints(ds.getId(), DataPointNameComparator.instance, false);
        result.addData("list", points);

        return result;
    }

    @DwrPermission(user = true)
    public ProcessResult toggle(int dataPointId) {
        DataPointVO dataPoint = DataPointDao.instance.getFull(dataPointId);
        Permissions.ensureDataSourcePermission(Common.getUser(), dataPoint.getDataSourceId());

        dataPoint.setEnabled(!dataPoint.isEnabled());
        Common.runtimeManager.saveDataPoint(dataPoint);

        ProcessResult response = new ProcessResult();
        response.addData("id", dataPointId);
        response.addData("enabled", dataPoint.isEnabled());
        return response;

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.deltamation.mango.downtime.web.AbstractBasicDwr#getFull(int)
     */
    @DwrPermission(user = true)
    @Override
    public ProcessResult get(int id) {
        return this.getFull(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.deltamation.mango.downtime.web.AbstractBasicDwr#getFull(int)
     */
    @DwrPermission(user = true)
    @Override
    public ProcessResult getFull(int id) {
        DataPointVO vo;
        User user = Common.getUser();

        if (id == Common.NEW_ID) {
            vo = dao.getNewVo();
            //TODO Need to sort this out another way, this will wreck 
            // when users have mulitple tabs open in a browser
            DataSourceVO<?> ds = user.getEditDataSource();
            vo.setXid(dao.generateUniqueXid());
            vo.setPointLocator(ds.createPointLocator());
            vo.setDataSourceId(ds.getId());
            vo.setDataSourceName(ds.getName());
            vo.setDataSourceTypeName(ds.getDefinition().getDataSourceTypeName());
            vo.setDataSourceXid(ds.getXid());
            vo.setDeviceName(ds.getName());
            vo.setEventDetectors(new ArrayList<AbstractPointEventDetectorVO<?>>(0));
            vo.defaultTextRenderer();
        }
        else {
            vo = dao.getFull(id);
        }

        //Should check permissions?
        //Permissions.ensureDataSourcePermission(user, vo.getDataSourceId());
        user.setEditPoint(vo);
        //TODO Need to deal with point value defaulter

        ProcessResult response = new ProcessResult();
        response.addData("vo", vo);

        return response;
    }

    /**
     * Delete a VO
     * 
     * @param id
     * @return
     */
    @Override
    @DwrPermission(user = true)
    public ProcessResult remove(int id) {
        ProcessResult response = new ProcessResult();
        try {
            DataPointVO dp = dao.get(id);
            if (dp != null)
                Common.runtimeManager.deleteDataPoint(dp);
        }
        catch (Exception e) {
            // Handle the exceptions.
            LOG.error(e);
            DataPointVO vo = dao.get(id);
            if (e instanceof DataIntegrityViolationException)
                response.addContextualMessage(vo.getName(), "table.edit.unableToDeleteDueToConstraints");
            else
                response.addContextualMessage(vo.getName(), "table.edit.unableToDelete", e.getMessage());
        }

        response.addData("id", id);
        return response;
    }

    /**
     * Not currently being used, see DataSourceEditDwr
     * 
     * Save the VO AND FDAO Data
     * 
     * Conversion for the VO must be added by extending DwrConversionDefinition
     * 
     * @return
     */
    @Override
    @DwrPermission(user = true)
    public ProcessResult saveFull(DataPointVO vo) { // TODO combine with save()
        ProcessResult response = new ProcessResult();

        if (vo.getXid() == null) {
            vo.setXid(dao.generateUniqueXid());
        }
        vo.validate(response);

        if (!response.getHasMessages()) {

            //When potential for the defaulter is available one must use the DataSourceEditDwr.validate method and store/pull
            // the data point vo into/outof the session (Common.User.data)
            //TODO            DataPointDefaulter defaulter = null;
            try {
                Common.runtimeManager.saveDataPoint(vo);
                //TODO             if (defaulter != null)
                //                    defaulter.postSave(vo);

            } catch(LicenseViolatedException e) {
            	LOG.error(e);
            	response.addMessage(e.getErrorMessage());
            } catch (Exception e) {
                // Handle the exceptions.
                LOG.error(e);

                String context = vo.getName();
                if (context == null) {
                    context = vo.getXid();
                }
                if (context == null) {
                    context = vo.getXid();
                }
                if (context == null) {
                    context = Integer.toString(vo.getId());
                }

                if (e instanceof DuplicateKeyException)
                    response.addContextualMessage(context, "table.edit.alreadyExists");
                else
                    response.addContextualMessage(context, "table.edit.unableToSave", e.getMessage());
            }
        }
        response.addData("vo", vo);
        response.addData("id", vo.getId()); //Add in case it fails
        return response;
    }

    /**
     * Override the copy method as to manage the User.editingPoint so it is available on copy
     */
    @Override
    @DwrPermission(user = true)
    public ProcessResult getCopy(int id) {
        ProcessResult result = super.getCopy(id);

        //Store the edit point
        DataPointVO editPoint = (DataPointVO) result.getData().get("vo");
        Common.getUser().setEditPoint(editPoint);

        return result;
    }

    /**
     * Get a list of available Chart Renderers for this point
     * 
     * @param vo
     * @return
     */
    @DwrPermission(user = true)
    public ProcessResult getChartRendererOptions(int dataTypeId) {
        ProcessResult response = new ProcessResult();
        List<ImplDefinition> list = BaseChartRenderer.getImplementations(dataTypeId);
        response.addData("options", list);
        return response;

    }

    /**
     * Get a list of available Chart Renderers for this point
     * 
     * @param vo
     * @return
     */
    @DwrPermission(user = true)
    public ProcessResult getTextRendererOptions(int dataTypeId) {
        ProcessResult response = new ProcessResult();
        List<ImplDefinition> list = BaseTextRenderer.getImplementation(dataTypeId);
        response.addData("options", list);
        return response;

    }

    /**
     * Get a list of available Point Event Detectors for this point
     * 
     * @param vo
     * @return
     */
    @DwrPermission(user = true)
    public ProcessResult getEventDetectorOptions(int dataTypeId) {
        ProcessResult response = new ProcessResult();
        List<EventDetectorDefinition> definitions = ModuleRegistry.getEventDetectorDefinitions();
        List<StringStringPair> list = new ArrayList<StringStringPair>();
        for(EventDetectorDefinition definition : definitions){
        	AbstractEventDetectorVO<?> vo = definition.baseCreateEventDetectorVO();
        	if(vo instanceof AbstractPointEventDetectorVO){
	        	AbstractPointEventDetectorVO<?> ped = (AbstractPointEventDetectorVO<?>)vo;
	        	if(ped.supports(dataTypeId)){
	        		list.add(new StringStringPair(definition.getDescriptionKey(), definition.getEventDetectorTypeName()));
	        	}
        	}
        }
        response.addData("options", list);
        return response;
    }

    /**
     * Store the logging properties into the
     * current user's edit point.
     * 
     * This is still being used on the page, but could be brought forward by putting the logging properties
     * into the DWR system
     * 
     * @param type
     * @param period
     * @param periodType
     * @param intervalType
     * @param tolerance
     * @param discardExtremeValues
     * @param discardHighLimit
     * @param discardLowLimit
     * @param purgeOverride
     * @param purgeType
     * @param purgePeriod
     * @param defaultCacheSize
     */

    @DwrPermission(user = true)
    public void storeEditLoggingProperties(int type, int period, int periodType, int intervalType, double tolerance,
            boolean discardExtremeValues, double discardHighLimit, double discardLowLimit, boolean purgeOverride,
            int purgeType, int purgePeriod, int defaultCacheSize, boolean overrideIntervalLoggingSamples,
            int intervalLoggingSampleWindowSize) {

        DataPointVO dp = Common.getUser().getEditPoint();
        if (dp != null) {
            dp.setLoggingType(type);
            dp.setIntervalLoggingPeriod(period);
            dp.setIntervalLoggingPeriodType(periodType);
            dp.setIntervalLoggingType(intervalType);
            dp.setTolerance(tolerance);
            dp.setDiscardExtremeValues(discardExtremeValues);
            dp.setDiscardHighLimit(discardHighLimit);
            dp.setDiscardLowLimit(discardLowLimit);
            dp.setPurgeOverride(purgeOverride);
            dp.setPurgeType(purgeType);
            dp.setPurgePeriod(purgePeriod);
            dp.setDefaultCacheSize(defaultCacheSize);
            dp.setOverrideIntervalLoggingSamples(overrideIntervalLoggingSamples);
            dp.setIntervalLoggingSampleWindowSize(intervalLoggingSampleWindowSize);
        }

    }

    /**
     * This method is used to pre-stage the vo for saving by the custom modules.
     * 
     * All of the general properties are saved into the "Session" here for use in the modules.
     * 
     * @param newDp
     */
    @SuppressWarnings("deprecation")
    @DwrPermission(user = true)
    public void storeEditProperties(DataPointVO newDp) {
        DataPointVO dp = Common.getUser().getEditPoint();
        if (dp != null) {
            //Do we want the details set here? (The ID Name,XID and Locator are stored via the modules)
            dp.setId(newDp.getId());
            dp.setXid(newDp.getXid());
            //dp.setPointLocator(newDp.getPointLocator());
            dp.setDeviceName(newDp.getDeviceName());
            dp.setEnabled(newDp.isEnabled());
            dp.setReadPermission(newDp.getReadPermission());
            dp.setSetPermission(newDp.getSetPermission());
            dp.setTemplateId(newDp.getTemplateId());

            dp.setDataSourceId(newDp.getDataSourceId());

            //General Properties
            dp.setEngineeringUnits(newDp.getEngineeringUnits());
            dp.setUseIntegralUnit(newDp.isUseIntegralUnit());
            dp.setUseRenderedUnit(newDp.isUseRenderedUnit());
            try{
            	dp.setUnit(UnitUtil.parseLocal(newDp.getUnitString())); //These won't come back from the UI as they aren't converted
            }catch(Exception e){
            	LOG.warn(e.getMessage(), e);
            	dp.setUnit(null); //For validation
            }
            dp.setUnitString(newDp.getUnitString());
            try{
            	dp.setRenderedUnit(UnitUtil.parseLocal(newDp.getRenderedUnitString()));
            }catch(Exception e){
            	LOG.warn(e.getMessage(), e);
            	dp.setRenderedUnit(null);
            }
            dp.setRenderedUnitString(newDp.getRenderedUnitString());
            try{
            	dp.setIntegralUnit(UnitUtil.parseLocal(newDp.getIntegralUnitString()));
            }catch(Exception e){
            	LOG.warn(e.getMessage(), e);
            	dp.setIntegralUnit(null);
            }
            dp.setIntegralUnitString(newDp.getIntegralUnitString());
            dp.setChartColour(newDp.getChartColour());
            dp.setPlotType(newDp.getPlotType());

            //Logging Properties
            dp.setLoggingType(newDp.getLoggingType());
            dp.setIntervalLoggingPeriod(newDp.getIntervalLoggingPeriod());
            dp.setIntervalLoggingPeriodType(newDp.getIntervalLoggingPeriodType());
            dp.setIntervalLoggingType(newDp.getIntervalLoggingType());
            dp.setTolerance(newDp.getTolerance());
            dp.setDiscardExtremeValues(newDp.isDiscardExtremeValues());
            dp.setDiscardHighLimit(newDp.getDiscardHighLimit());
            dp.setDiscardLowLimit(newDp.getDiscardLowLimit());
            dp.setPurgeOverride(newDp.isPurgeOverride());
            dp.setPurgeType(newDp.getPurgeType());
            dp.setPurgePeriod(newDp.getPurgePeriod());
            dp.setDefaultCacheSize(newDp.getDefaultCacheSize());
            dp.setOverrideIntervalLoggingSamples(newDp.isOverrideIntervalLoggingSamples());
            dp.setIntervalLoggingSampleWindowSize(newDp.getIntervalLoggingSampleWindowSize());

            //Chart Renderer
            dp.setChartRenderer(newDp.getChartRenderer());
            dp.setRollup(newDp.getRollup());

            //Text Renderer 
            dp.setTextRenderer(newDp.getTextRenderer());
            
            //Extreme sets
            dp.setPreventSetExtremeValues(newDp.isPreventSetExtremeValues());
            dp.setSetExtremeLowLimit(newDp.getSetExtremeLowLimit());
            dp.setSetExtremeHighLimit(newDp.getSetExtremeHighLimit());
        }
    }

    @DwrPermission(user = true)
    public ProcessResult validateUnit(String unit) {
        ProcessResult result = new ProcessResult();
        try {
            UnitUtil.parseUcum(unit);
            result.addData("validUnit", true);
            result.addData("message", new TranslatableMessage("validate.unitValid").translate(getTranslations()));
        }
        catch (Exception e) {
            result.addData("validUnit", false);
            if (e instanceof IllegalArgumentException) {
                result.addData("message", ((IllegalArgumentException) e).getCause().getMessage());
            }
            else {
                result.addData("message", e.getMessage());
            }
        }
        return result;
    }

    @DwrPermission(user = true)
    public ProcessResult getUnitsList() {
        ProcessResult result = new ProcessResult();
        List<DojoMemoryStoreListItem> pairs = new ArrayList<>();

        //Get SI Units
        int id = 0;
        for (Unit<?> unit : SI.getInstance().getUnits()) {
            pairs.add(new DojoMemoryStoreListItem(unit.toString(), id++));
        }

        //Get US Units
        for (Unit<?> unit : NonSI.getInstance().getUnits()) {
            pairs.add(new DojoMemoryStoreListItem(unit.toString(), id++));
        }
        
        List<String> addedUnits = UnitUtil.getAddedUnitLabels();
        for (String unit : addedUnits) {
        	pairs.add(new DojoMemoryStoreListItem(unit, id++));
        }
        
        Collections.sort(pairs, new Comparator<DojoMemoryStoreListItem>() {
			@Override
			public int compare(DojoMemoryStoreListItem arg0, DojoMemoryStoreListItem arg1) {
				return arg0.getName().compareToIgnoreCase(arg1.getName());
			}
        });

        result.addData("units", pairs);
        return result;
    }

    /**
     * Helper to get the most recent value for a point
     * 
     * @param id
     * @return
     */
    @DwrPermission(user = true)
    public ProcessResult getMostRecentValue(int id) {
        ProcessResult result = new ProcessResult();

        if (Common.runtimeManager.isDataPointRunning(id)) {
            DataPointRT rt = Common.runtimeManager.getDataPoint(id);
            //Check to see if the data source is running
            if (Common.runtimeManager.isDataSourceRunning(rt.getDataSourceId())) {
                PointValueFacade facade = new PointValueFacade(rt.getVO().getId());
                PointValueTime value = facade.getPointValue();
                if (value != null) {
                    RenderedPointValueTime rpvt = new RenderedPointValueTime();
                    rpvt.setValue(Functions.getHtmlText(rt.getVO(), value));
                    rpvt.setTime(Functions.getTime(value));
                    result.getData().put("pointValue", rpvt.getValue()); //Could return time and value?
                }
                else
                    result.getData().put("pointValue", translate("event.setPoint.activePointValue"));
            }
            else {
                result.getData().put("pointValue", translate("common.pointWarning"));
            }
        }
        else {
            result.getData().put("pointValue", translate("common.pointWarning"));
        }

        return result;
    }

    /**
     * Load a list of VOs
     * 
     * Overridden to provide security
     * 
     * @return
     */
    @Override
    @DwrPermission(user = true)
    public ProcessResult dojoQuery(Map<String, String> query, List<SortOption> sort, Integer start, Integer count,
            boolean or) {
        ProcessResult response = new ProcessResult();

        ResultsWithTotal results = dao.dojoQuery(query, sort, start, count, or);
        List<DataPointVO> vos = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<DataPointVO> filteredPoints = (List<DataPointVO>) results.getResults();

        //Filter list on User Permissions
        User user = Common.getUser();
        for (DataPointVO vo : filteredPoints) {
            if (Permissions.hasDataSourcePermission(user, vo.getDataSourceId())){ //.hasDataPointReadPermission(user, vo)) {
                vos.add(vo);
            }
        }

        //Since we have removed some, we need to review our totals here,,
        // this will be a bit buggy because we don't know how many of the remaining items 
        // are actually viewable by this user.
        int total = results.getTotal() - (filteredPoints.size() - vos.size());
        response.addData("list", vos);
        response.addData("total", total);

        return response;
    }

    /**
     * Export VOs based on a filter
     * 
     * @param id
     * @return
     */
    @SuppressWarnings("unchecked")
    @DwrPermission(user = true)
    public String jsonExportUsingFilter(Map<String, String> query, List<SortOption> sort, Integer start, Integer count,
            boolean or) {
        Map<String, Object> data = new LinkedHashMap<>();
        List<DataPointVO> vos = new ArrayList<>();

        ResultsWithTotal results = dao.dojoQuery(query, sort, start, count, or);
        List<DataPointVO> filteredPoints = (List<DataPointVO>) results.getResults();

        //Filter list on User Permissions
        User user = Common.getUser();
        for (DataPointVO vo : filteredPoints) {
            if (Permissions.hasDataPointReadPermission(user, vo)) {
            	dao.setEventDetectors(vo);
                vos.add(vo);
            }
        }

        //Get the Full VO for the export
        data.put(keyName, vos);

        return EmportDwr.export(data, 3);
    }

}
