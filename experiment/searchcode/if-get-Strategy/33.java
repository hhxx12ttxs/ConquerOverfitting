package recommender.strategy;

import java.util.HashMap;
import java.util.Map;
import recommender.models.StrategyType;
public static Strategy createStrategy(StrategyType type){
Strategy strategy = map.get(type);

if(strategy==null){
switch(type){
case UserBasedStrategy: strategy = new UserBasedStrategy(); break;

