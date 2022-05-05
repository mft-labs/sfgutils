package com.mftlabs.sfgutils.bpexec;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.mftlabs.sfgutils.SfgApiClient;
import com.sterlingcommerce.woodstock.workflow.WorkFlowContext;

public class GetKnownHostKeyList {
	public static void execute(WorkFlowContext wfc) {
		try {	
			String result1 = SfgApiClient.getKnownHostKeyList(wfc);
			wfc.setWFContent("KnownHostKeyList", result1); 
			
		} catch(Exception e) {
			StringWriter sw = new StringWriter();
	         e.printStackTrace(new PrintWriter(sw));
			wfc.setWFContent("ExceptionRaised", sw.toString());
			throw new RuntimeException(e);
		}
	}
}
