/**
 * Copyright (C) 2013 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.db.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowMapper;

import com.infiniteautomation.mango.db.query.JoinClause;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.db.pair.IntStringPair;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DeltamationCommon;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.EventTypeDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.rt.event.type.DataPointEventType;
import com.serotonin.m2m2.rt.event.type.DataSourceEventType;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.rt.event.type.PublisherEventType;
import com.serotonin.m2m2.rt.event.type.SystemEventType;
import com.serotonin.m2m2.vo.comment.UserCommentVO;
import com.serotonin.m2m2.vo.event.EventInstanceVO;

/**
 * @author Terry Packer
 *
 */
public class EventInstanceDao extends AbstractDao<EventInstanceVO> {

	public static final EventInstanceDao instance = new EventInstanceDao();
	
	/**
	 * @param typeName
	 */
	private EventInstanceDao() {
		super(ModuleRegistry.getWebSocketHandlerDefinition("EVENT_INSTANCE"), null,"evt",
				new String[]{
					"u.username",
					"(select count(1) from userComments where commentType=" + UserCommentVO.TYPE_EVENT +" and typeKey=evt.id) as cnt ",
					"ue.silenced"});
		LOG = LogFactory.getLog(EventInstanceDao.class);
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.db.dao.AbstractDao#getTableName()
	 */
	@Override
	protected String getTableName() {
		return SchemaDefinition.EVENTS_TABLE;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.db.dao.AbstractDao#getXidPrefix()
	 */
	@Override
	protected String getXidPrefix() {
		return null; //No XIDs
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.db.dao.AbstractDao#voToObjectArray(com.serotonin.m2m2.vo.AbstractVO)
	 */
	@Override
	protected Object[] voToObjectArray(EventInstanceVO vo) {
		return new Object[]{
				vo.getId(),
				
		};
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.db.dao.AbstractDao#getNewVo()
	 */
	@Override
	public EventInstanceVO getNewVo() {
		return new EventInstanceVO();
	}


	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.db.dao.AbstractBasicDao#getPropertyTypeMap()
	 */
	@Override
	protected LinkedHashMap<String,Integer> getPropertyTypeMap(){
		LinkedHashMap<String,Integer> map = new LinkedHashMap<String,Integer>();
		map.put("id", Types.INTEGER);
		map.put("typeName", Types.VARCHAR);
		map.put("subtypeName", Types.VARCHAR);
		map.put("typeRef1", Types.INTEGER);
		map.put("typeRef2", Types.INTEGER);
		map.put("activeTs", Types.BIGINT);
		map.put("rtnApplicable", Types.CHAR);
		map.put("rtnTs", Types.BIGINT);
		map.put("rtnCause", Types.INTEGER);
		map.put("alarmLevel", Types.INTEGER);
		map.put("message", Types.LONGVARCHAR);
		map.put("ackTs", Types.BIGINT);
		map.put("ackUserId", Types.INTEGER);
		map.put("alternateAckSource", Types.LONGVARCHAR);
		
		return map;
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.db.dao.AbstractBasicDao#getPropertiesMap()
	 */
	@Override
	protected Map<String, IntStringPair> getPropertiesMap() {
		Map<String,IntStringPair> map = new HashMap<String,IntStringPair>();
		map.put("activeTimestamp", new IntStringPair(Types.BIGINT, "activeTs"));
		map.put("activeTimestampString", new IntStringPair(Types.BIGINT, "activeTs"));
		map.put("rtnTimestampString", new IntStringPair(Types.BIGINT, "rtnTs"));
		
        /*
         * IF(evt.rtnTs=null,
         * 		IF(evt.rtnApplicable='Y',
         * 			(NOW() - evt.activeTs),
         * 			-1),
         * 		IF(evt.rtnApplicable='Y',
         * 			(evt.rtnTs - evt.activeTs),
         * 			-1)
         *  )
         */
		switch(Common.databaseProxy.getType()){
			case MYSQL:
			case MSSQL:
				map.put("totalTimeString", new IntStringPair(Types.BIGINT, "IF(evt.rtnTs is null,IF(evt.rtnApplicable='Y',(? - evt.activeTs),-1),IF(evt.rtnApplicable='Y',(evt.rtnTs - evt.activeTs),-1))"));
			break;
			
			case H2:
			case DERBY:
				map.put("totalTimeString", new IntStringPair(Types.BIGINT, "CASE WHEN evt.rtnTs IS NULL THEN "
	                    + "CASE WHEN evt.rtnApplicable='Y' THEN (? - evt.activeTs) ELSE -1 END "
	                    + "ELSE CASE WHEN evt.rtnApplicable='Y' THEN (evt.rtnTs - evt.activeTs) ELSE -1 END END"));
			break;
			default:
				throw new ShouldNeverHappenException("Unsupported database for Alarms.");
		}	
		map.put("messageString", new IntStringPair(Types.VARCHAR, "message"));
		map.put("rtnTimestampString", new IntStringPair(Types.BIGINT, "rtnTs"));
		map.put("userNotified", new IntStringPair(Types.CHAR, "silenced"));
		map.put("acknowledged", new IntStringPair(Types.BIGINT, "ackTs"));
		map.put("userId", new IntStringPair(Types.INTEGER, "ue.userId")); //Mapping for user 
		return map;
	}

	@Override
	protected Map<String, PropertyArguments> getPropertyArgumentsMap(){
		Map<String,PropertyArguments> map = new HashMap<String,PropertyArguments>();
		map.put("totalTimeString", new PropertyArguments(){
			public Object[] getArguments(){
				return new Object[]{new Date().getTime()};
			}
			});
		
		return map;
	}
	
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.db.dao.AbstractBasicDao#getJoins()
     */
    @Override
    protected List<JoinClause> getJoins() {
    	List<JoinClause> joins = new ArrayList<JoinClause>();
    	joins.add(new JoinClause(LEFT_JOIN, "users", "u", "evt.ackUserId = u.id"));
    	joins.add(new JoinClause(LEFT_JOIN, "userEvents", "ue", "evt.id=ue.eventId"));
    	return joins;
    }
	
	@Override
	protected Map<String, Comparator<EventInstanceVO>> getComparatorMap() {
		HashMap<String,Comparator<EventInstanceVO>> comparatorMap = new HashMap<String,Comparator<EventInstanceVO>>();
		
//		comparatorMap.put("messageString", new Comparator<EventInstanceVO>(){
//			public int compare(EventInstanceVO lhs, EventInstanceVO rhs){
//				return lhs.getMessageString().compareTo(rhs.getMessageString());
//			}
//		});

//		comparatorMap.put("totalTimeString", new Comparator<EventInstanceVO>(){
//			public int compare(EventInstanceVO lhs, EventInstanceVO rhs){
//				return lhs.getTotalTime().compareTo(rhs.getTotalTime());
//			}
//		});

		
		return comparatorMap;
	}

	@Override
	protected Map<String, IFilter<EventInstanceVO>> getFilterMap(){
		HashMap<String, IFilter<EventInstanceVO>> filterMap = new HashMap<String,IFilter<EventInstanceVO>>();
		
		filterMap.put("messageString", new IFilter<EventInstanceVO>(){
			
			private String regex;
			@Override
			public boolean filter(EventInstanceVO vo) {
				return !vo.getMessageString().matches(regex);
			}

			@Override
			public void setFilter(Object matches) {
				this.regex = "(?i)"+(String)matches;
				
			}
			
		});
		
		filterMap.put("rtnTimestampString", new IFilter<EventInstanceVO>(){
			
			private String regex;
			@Override
			public boolean filter(EventInstanceVO vo) {
				String rtnTimestampString;
				if(vo.isActive())
					rtnTimestampString = Common.translate("common.active");
				else if(!vo.isRtnApplicable())
					rtnTimestampString = Common.translate("common.nortn");
				else
					rtnTimestampString = vo.getRtnTimestampString() + " - " + vo.getRtnMessageString();
				
				return !rtnTimestampString.matches(regex);
			}

			@Override
			public void setFilter(Object matches) {
				this.regex = "(?i)"+(String)matches;
			}
			
		});
		
		
		filterMap.put("totalTimeString", new IFilter<EventInstanceVO>(){
			
			private Long duration;
			private int operator;
			
			@Override
			public boolean filter(EventInstanceVO vo) {
				//Remember filter means to remove from list if true
				if(operator == 1){
					return vo.getTotalTime() < duration;
				}else if (operator == 2){
					return vo.getTotalTime() > duration; 
				}else{
					return !vo.getTotalTime().equals(duration);
				}
			}

			@Override
			public void setFilter(Object matches) {
				String condition = (String)matches;
		    	//Parse the value as Duration:operatorvalue - Duration:>1:00:00
            	String durationString = condition.substring(10,condition.length());
            	String compare = condition.substring(9, 10);
            	this.duration = DeltamationCommon.unformatDuration(durationString);
            	
            	if(compare.equals(">")){
            		operator = 1;
            	}else if(compare.equals("<")){
            		operator = 2;
            	}else if(compare.equals("=")){
            		operator = 3; 
            	}
			}
			
		});
		return filterMap;
	}
	
	
	
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.db.dao.AbstractBasicDao#getRowMapper()
	 */
	@Override
	public RowMapper<EventInstanceVO> getRowMapper() {
		return new UserEventInstanceVORowMapper();
	}
	
    public static class EventInstanceVORowMapper implements RowMapper<EventInstanceVO> {
        @Override
        public EventInstanceVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            EventInstanceVO event = new EventInstanceVO();
            event.setId(rs.getInt(1));

            EventType type = createEventType(rs, 2);
            event.setEventType(type);
            event.setActiveTimestamp(rs.getLong(6));
            event.setRtnApplicable(charToBool(rs.getString(7)));
            event.setAlarmLevel(rs.getInt(10));
            TranslatableMessage message = BaseDao.readTranslatableMessage(rs, 11);
            if(message == null)
            	event.setMessage(new TranslatableMessage("common.noMessage"));
            else
            	event.setMessage(message);
 
            //Set the Return to normal
            long rtnTs = rs.getLong(8);
            if (!rs.wasNull()){
            	//if(event.isActive()){ Probably don't need this
            		event.setRtnTimestamp(rtnTs);
            		event.setRtnCause(rs.getInt(9));
            	//}
            	if(event.isRtnApplicable()){
            		event.setTotalTime(rtnTs - event.getActiveTimestamp());
            	}else{
            		event.setTotalTime(-1L);
            	}            		
            }else{
            	if(event.isRtnApplicable()){
            		//Has not been acknowledged yet
            		Date now = new Date();
            		event.setTotalTime(now.getTime() -event.getActiveTimestamp());
            	}else{
            		//Won't ever be
            		event.setTotalTime(-1L);
            	}
            }
            
            long ackTs = rs.getLong(12);
            if (!rs.wasNull()) {
            	//Compute total time
                event.setAcknowledgedTimestamp(ackTs);
                event.setAcknowledgedByUserId(rs.getInt(13));
                if (!rs.wasNull())
                    event.setAcknowledgedByUsername(rs.getString(15));
                event.setAlternateAckSource(BaseDao.readTranslatableMessage(rs, 14));
            }
            event.setHasComments(rs.getInt(16) > 0);         
            
            //This makes another query!
            this.attachRelationalInfo(event);
            
            
            return event;
        }
        
        private static final String EVENT_COMMENT_SELECT = UserCommentDao.USER_COMMENT_SELECT //
                + "where uc.commentType= " + UserCommentVO.TYPE_EVENT //
                + " and uc.typeKey=? " //
                + "order by uc.ts";

        void attachRelationalInfo(EventInstanceVO event) {
            if (event.isHasComments())
                event.setEventComments(EventInstanceDao.instance.query(EVENT_COMMENT_SELECT, new Object[] { event.getId() },
                        UserCommentDao.instance.getRowMapper()));
        }

        
    }
	
    class UserEventInstanceVORowMapper extends EventInstanceVORowMapper {
        @Override
        public EventInstanceVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            EventInstanceVO event = super.mapRow(rs, rowNum);
            event.setSilenced(charToBool(rs.getString(17)));
            if (!rs.wasNull())
                event.setUserNotified(true);
            return event;
        }
    }
    

    
    
    
	/**
	 * @param userId
	 * @param level 
	 * @return
	 */
	public EventInstanceVO getHighestUnsilencedEvent(int userId, int level) {
        return ejt.queryForObject(SELECT_ALL
                + "where ue.silenced=? and ue.userId=? and evt.alarmLevel=? ORDER BY evt.activeTs DESC LIMIT 1", new Object[] { boolToChar(false), userId, level },getRowMapper(), null);
	}

	/**
	 * @param userId
	 * @return
	 */
	public List<EventInstanceVO> getUnsilencedEvents(int userId) {
        return ejt.query(SELECT_ALL
                + "where ue.silenced=? and ue.userId=?", new Object[] { boolToChar(false), userId },getRowMapper());

	}
	
	
	
    static EventType createEventType(ResultSet rs, int offset) throws SQLException {
        String typeName = rs.getString(offset);
        String subtypeName = rs.getString(offset + 1);
        EventType type;
        if (typeName.equals(EventType.EventTypeNames.DATA_POINT))
            type = new DataPointEventType(rs.getInt(offset + 2), rs.getInt(offset + 3));
        else if (typeName.equals(EventType.EventTypeNames.DATA_SOURCE))
            type = new DataSourceEventType(rs.getInt(offset + 2), rs.getInt(offset + 3));
        else if (typeName.equals(EventType.EventTypeNames.SYSTEM))
            type = new SystemEventType(subtypeName, rs.getInt(offset + 2));
        else if (typeName.equals(EventType.EventTypeNames.PUBLISHER))
            type = new PublisherEventType(rs.getInt(offset + 2), rs.getInt(offset + 3));
        else if (typeName.equals(EventType.EventTypeNames.AUDIT))
           throw new ShouldNeverHappenException("AUDIT events should not exist here. Consider running the SQL: DELETE FROM events WHERE typeName='AUDIT';");
        else {
            EventTypeDefinition def = ModuleRegistry.getEventTypeDefinition(typeName);
            if (def == null)
                throw new ShouldNeverHappenException("Unknown event type: " + typeName);
            type = def.createEventType(subtypeName, rs.getInt(offset + 2), rs.getInt(offset + 3));
            if (type == null)
                throw new ShouldNeverHappenException("Unknown event type: " + typeName);
        }
        return type;
    }

	/**
	 * @param lifeSafety
	 * @return
	 */
	public int countUnsilencedEvents(int userId, int level) {
		//return ejt.queryForInt("SELECT COUNT(*) FROM events AS evt left join userEvents ue on evt.id=ue.eventId where ue.silenced=? and evt.ackUserId=? and evt.alarmLevel=?", new Object[] { boolToChar(false), userId, level }, 0);
		return ejt.queryForInt(COUNT + " where ue.silenced=? and ue.userId=? and evt.alarmLevel=?", new Object[] { boolToChar(false), userId, level }, 0);
	}    
    
}
