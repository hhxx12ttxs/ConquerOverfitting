import il.ac.technion.ie.exception.NotImplementedYetException;
import il.ac.technion.ie.model.AbstractBlock;
import il.ac.technion.ie.model.Block;

import java.util.ArrayList;
public BlockPotential(AbstractBlock abstractBlock) {
this.potential = new HashMap<>();
this.blockID = abstractBlock.getId();
if (abstractBlock instanceof Block) {

