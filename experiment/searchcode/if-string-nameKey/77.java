import models.Bin;
@JsonIgnoreProperties(ignoreUnknown = true)
public class WarehouseQueryVo {
public String warehouse;
public String storageType;
public String storageArea;
public String storageBin;
public double totalCapacity;

