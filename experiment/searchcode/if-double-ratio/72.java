private Integer number;
private Double  weight;
private Double  value;
private Double  ratio;


public Item(Integer n, Double v, Double w){
number = n;
weight = w;
value  = v;
ratio = v/w;
}

public Item(Double v, Double w){

