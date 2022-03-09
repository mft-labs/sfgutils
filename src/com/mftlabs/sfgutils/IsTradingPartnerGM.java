package com.mftlabs.sfgutils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import com.sterlingcommerce.woodstock.workflow.WorkFlowContext;

public class IsTradingPartnerGM {
	
	public static void execute(WorkFlowContext wfc) {
		HashMap<String, String> config = SfgApiClient.GetConfig();
		try {
		
			String partner = (String)wfc.getWFContent("Cigna/Adapter/PARM1");
			 if (partner.split("/").length==1) {
				 partner = (String)wfc.getWFContent("Cigna/Adapter/PARM2");
			 }			 
			 String[] arr = partner.split("/");
			 IsTradingPartnerGM utils = new IsTradingPartnerGM();
			 HashMap<String,Object> result = utils.isTradingPartnerGM(arr[1]);
			 if ((Boolean)result.get("GMUser")) {
				 wfc.setWFContent("globalMailboxParter", "true"); 
			 } else {
				 wfc.setWFContent("globalMailboxParter", "false");
			 }
			 if(config.get("DEBUG").equalsIgnoreCase("true")) {
				 wfc.setWFContent("globalMailboxPartnerLog",result.get("details"));
			 }
		} catch(Exception e) {
			 StringWriter sw = new StringWriter();
	         e.printStackTrace(new PrintWriter(sw));
			wfc.setWFContent("ExceptionRaised", sw.toString());
			throw new RuntimeException(e);
		}
	}
	
	public HashMap<String, Object> isTradingPartnerGM(String partner) throws Exception {
		HashMap<String, String> config = SfgApiClient.GetConfig();
		String useApi = config.get("SFG_USE_API");
		if (useApi.equalsIgnoreCase("true")) {
			return SfgApiClient.isTradingPartnerGM(partner);
		}
		return  SfgApiClient.isGMUser(partner);
	}

}
