/*============================================================================
 * Copyright 2009 VMware Inc.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy 
 * of the License at: http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations 
 * under the License.
 ============================================================================*/

package com.vmware.vcloud.adapter;

import java.util.StringTokenizer;

/**
 * @author Steve Jin (sjin@vmware.com)
 * Each vApp has a public IP, on which multiple Internet services can be defined.
 * Each Internet Service could include multiple vApp Nodes. 
 * NOTE: For the moment, we only allow one vApp Node in an Internet Service.
 * 
 * To remove networking of a vApp, vAppNodes has to be removed first, then InternetService,
 * and finally publicIP.
 */

public class VAppNetworkInfo
{
	public String vAppUrl;
	public String publicIP;
	public String privateIP;
	public String publicIpUrl;
	public String[] internetServiceUrls;
	public String[] nodeUrls;

	public VAppNetworkInfo()
	{
	}
	
	public VAppNetworkInfo(String id)
	{
		StringTokenizer st = new StringTokenizer(id, ",");
		int total = st.countTokens();

		vAppUrl = st.nextToken();
		publicIP = st.nextToken();
		privateIP = st.nextToken();
		publicIpUrl = st.nextToken();
		
		int arrayLength = (total-4)/2;
		internetServiceUrls = new String[arrayLength];
		nodeUrls = new String[arrayLength];

		for(int i=0; i<arrayLength; i++)
		{
			internetServiceUrls[i] = st.nextToken();
			nodeUrls[i] = st.nextToken();
		}
	}

	public String toString()
	{
		String str = vAppUrl + "," + publicIP + "," + privateIP + "," + publicIpUrl;
		
		for(int i=0; i< internetServiceUrls.length; i++)
		{
			str += "," + internetServiceUrls[i] + "," + nodeUrls[i];
		}
		return str;
	}
	
	public static void main(String[] args)
	{
		String id = "https:vapp,10.20.101.201,10.90.22.22,https:ip,https://is,https:node,http://is/2,http:node/2";
		VAppNetworkInfo net = new VAppNetworkInfo(id);
		String genid = net.toString();
		if(genid.equals(id))
		{
			System.out.println("same");
		}
	}
}
