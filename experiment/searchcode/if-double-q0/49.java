loadIRI(SWSChallConstants.shipmentOntoNS+&quot;Package&quot;));
if (packageInst != null){
package_quantity = &quot;&quot;+ new Double(Math.ceil(new Double(Helper.getInstanceAttribute(packageInst,loadIRI(SWSChallConstants.shipmentOntoNS+&quot;quantity&quot;))))).intValue();
result+=&quot;  </q0:packageInformation>&quot;;
result+=&quot;</q0:invokePriceRequest>&quot;;
} else if (id.toString().contains(SWSChallConstants.WSMullerOrderRequest) )

