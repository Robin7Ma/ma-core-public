/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.rt.event.detectors;

import com.serotonin.m2m2.vo.event.detector.AbstractEventDetectorVO;

/**
 * 
 * Abstraction for Event Detector Runtimes
 * @author Terry Packer
 *
 */
public abstract class AbstractEventDetectorRT<T extends AbstractEventDetectorVO<?>> {

	protected T vo;
	
	public AbstractEventDetectorRT(T vo){
		this.vo = vo;
	}
	
	public T getVO(){
		return vo;
	}
}
