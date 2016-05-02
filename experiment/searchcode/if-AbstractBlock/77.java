/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.rendering.block;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xwiki.rendering.block.match.AnyBlockMatcher;
import org.xwiki.rendering.block.match.BlockNavigatorTest;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;

/**
 * Unit tests for Block manipulation, testing {@link AbstractBlock}.
 * 
 * @version $Id: c6c04180d0cbc53f3c1709889219c4270440dce7 $
 * @since 1.5M2
 */
public class BlockTest
{
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testInsertChildAfter()
    {
        Block wb1 = new WordBlock("block1");
        Block wb2 = new WordBlock("block2");
        ParagraphBlock pb = new ParagraphBlock(Arrays.asList(wb1, wb2));

        Block wb = new WordBlock("block");

        pb.insertChildAfter(wb, wb1);
        Assert.assertSame(wb, pb.getChildren().get(1));
        Assert.assertSame(wb1, wb.getPreviousSibling());
        Assert.assertSame(wb2, wb.getNextSibling());
        Assert.assertSame(wb, wb1.getNextSibling());
        Assert.assertSame(wb, wb2.getPreviousSibling());

        pb.insertChildAfter(wb, wb2);
        Assert.assertSame(wb, pb.getChildren().get(3));
        Assert.assertSame(wb2, wb.getPreviousSibling());
        Assert.assertSame(wb, wb2.getNextSibling());
        Assert.assertNull(wb.getNextSibling());
    }

    @Test
    public void testInsertChildBefore()
    {
        Block wb1 = new WordBlock("block1");
        Block wb2 = new WordBlock("block2");

        List<Block> children = new ArrayList<Block>();
        children.add(wb1);
        children.add(wb2);

        ParagraphBlock pb = new ParagraphBlock(children);

        Block wb = new WordBlock("block");

        pb.insertChildBefore(wb, wb1);
        Assert.assertSame(wb, pb.getChildren().get(0));

        pb.insertChildBefore(wb, wb2);
        Assert.assertSame(wb, pb.getChildren().get(2));
    }

    @Test
    public void testReplaceBlock()
    {
        // It's important all blocks have same content to make sure replacement api don't find the position of the
        // old block using Object#equals
        Block word1 = new WordBlock("block1");
        Block word2 = new WordBlock("block2");
        Block word3 = new WordBlock("block3");

        Block parentBlock = new ParagraphBlock(Arrays.asList(word1, word2));

        // replace by one
        parentBlock.replaceChild(word3, word1);

        Assert.assertEquals(2, parentBlock.getChildren().size());
        Assert.assertSame(word3, parentBlock.getChildren().get(0));
        Assert.assertSame(word2, parentBlock.getChildren().get(1));
        Assert.assertSame(word2, word3.getNextSibling());
        Assert.assertSame(word3, word2.getPreviousSibling());

        // replace by nothing
        parentBlock.replaceChild(Collections.<Block> emptyList(), word2);

        Assert.assertEquals(1, parentBlock.getChildren().size());
        Assert.assertSame(word3, parentBlock.getChildren().get(0));
        Assert.assertNull(word3.getNextSibling());
        Assert.assertNull(word3.getPreviousSibling());

        // replace by several
        parentBlock.replaceChild(Arrays.asList(word1, word2), word3);

        Assert.assertEquals(2, parentBlock.getChildren().size());
        Assert.assertSame(word1, parentBlock.getChildren().get(0));
        Assert.assertSame(word2, parentBlock.getChildren().get(1));
        Assert.assertSame(word2, word1.getNextSibling());
        Assert.assertSame(word1, word2.getPreviousSibling());

        // Provide not existing block to replace
        this.thrown.expect(InvalidParameterException.class);
        parentBlock.replaceChild(word3, new WordBlock("not existing"));
    }

    @Test
    public void testClone()
    {
        WordBlock wb = new WordBlock("block");
        ImageBlock ib = new ImageBlock(new ResourceReference("document@attachment", ResourceType.ATTACHMENT), true);
        DocumentResourceReference linkReference = new DocumentResourceReference("reference");
        LinkBlock lb = new LinkBlock(Arrays.asList((Block) new WordBlock("label")), linkReference, false);
        Block pb = new ParagraphBlock(Arrays.<Block> asList(wb, ib, lb));
        XDOM rootBlock = new XDOM(Arrays.<Block> asList(pb));

        XDOM newRootBlock = rootBlock.clone();

        Assert.assertNotSame(rootBlock, newRootBlock);
        Assert.assertNotSame(rootBlock.getMetaData(), newRootBlock.getMetaData());

        Block newPB = newRootBlock.getChildren().get(0);

        Assert.assertNotSame(pb, newPB);

        Assert.assertNotSame(wb, newPB.getChildren().get(0));
        Assert.assertNotSame(ib, newPB.getChildren().get(1));
        Assert.assertNotSame(lb, newPB.getChildren().get(2));

        Assert.assertEquals(wb.getWord(), ((WordBlock) newPB.getChildren().get(0)).getWord());
        Assert.assertNotSame(ib.getReference(), ((ImageBlock) newPB.getChildren().get(1)).getReference());
        Assert.assertNotSame(lb.getReference(), ((LinkBlock) newPB.getChildren().get(2)).getReference());
    }

    @Test
    public void testGetNextSibling()
    {
        WordBlock b1 = new WordBlock("b1");
        WordBlock b2 = new WordBlock("b2");
        ParagraphBlock p = new ParagraphBlock(Arrays.<Block> asList(b1, b2));

        Assert.assertSame(b2, b1.getNextSibling());
        Assert.assertNull(b2.getNextSibling());
        Assert.assertNull(p.getNextSibling());
        Assert.assertNull(new ParagraphBlock(Collections.<Block> emptyList()).getNextSibling());
    }

    @Test
    public void testRemoveBlock()
    {
        WordBlock b1 = new WordBlock("b1");
        WordBlock b2 = new WordBlock("b2");
        ParagraphBlock p1 = new ParagraphBlock(Arrays.<Block> asList(b1, b2));

        p1.removeBlock(b1);
        Assert.assertEquals(1, p1.getChildren().size());
        Assert.assertSame(b2, p1.getChildren().get(0));
        Assert.assertNull(b1.getPreviousSibling());
        Assert.assertNull(b1.getNextSibling());
        Assert.assertNull(b2.getPreviousSibling());

        p1.removeBlock(b2);
        Assert.assertEquals(0, p1.getChildren().size());
        Assert.assertNull(b2.getPreviousSibling());
        Assert.assertNull(b2.getNextSibling());
    }

    @Test
    public void testGetBlocks()
    {
        Assert.assertEquals(Arrays.asList(BlockNavigatorTest.parentBlock, BlockNavigatorTest.rootBlock),
            BlockNavigatorTest.contextBlock.getBlocks(AnyBlockMatcher.ANYBLOCKMATCHER, Block.Axes.ANCESTOR));
    }

    @Test
    public void testGetFirstBlock()
    {
        Assert.assertSame(BlockNavigatorTest.parentBlock,
            BlockNavigatorTest.contextBlock.getFirstBlock(AnyBlockMatcher.ANYBLOCKMATCHER, Block.Axes.ANCESTOR));
    }

    @Test
    public void testSetChildren()
    {
        ParagraphBlock paragraphBlock = new ParagraphBlock(Collections.EMPTY_LIST);

        List<Block> blocks = Arrays.<Block> asList(new WordBlock("1"), new WordBlock("2"));
        paragraphBlock.setChildren(blocks);

        Assert.assertArrayEquals(blocks.toArray(), paragraphBlock.getChildren().toArray());

        blocks = Arrays.<Block> asList(new WordBlock("3"), new WordBlock("4"));
        paragraphBlock.setChildren(blocks);

        Assert.assertArrayEquals(blocks.toArray(), paragraphBlock.getChildren().toArray());

        blocks = Arrays.<Block> asList();
        paragraphBlock.setChildren(blocks);

        Assert.assertArrayEquals(blocks.toArray(), paragraphBlock.getChildren().toArray());
    }

    @Test
    public void testSetGetParameter()
    {
        WordBlock wordBlock = new WordBlock("word");

        wordBlock.setParameter("param", "value");

        Assert.assertEquals("value", wordBlock.getParameter("param"));

        wordBlock.setParameter("param", "value2");

        Assert.assertEquals("value2", wordBlock.getParameter("param"));
    }

    @Test
    public void testSetGetParameters()
    {
        WordBlock wordBlock = new WordBlock("word");

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("param1", "value1");
        parameters.put("param2", "value2");

        wordBlock.setParameters(parameters);

        Assert.assertEquals(parameters, wordBlock.getParameters());

        Map<String, String> parameters2 = new HashMap<String, String>();
        parameters.put("param21", "value21");
        parameters.put("param22", "value22");

        wordBlock.setParameters(parameters2);

        Assert.assertEquals(parameters2, wordBlock.getParameters());
    }

    @Test
    public void testGetRoot()
    {
        Assert.assertSame(BlockNavigatorTest.rootBlock, BlockNavigatorTest.rootBlock.getRoot());
        Assert.assertSame(BlockNavigatorTest.rootBlock, BlockNavigatorTest.contextBlock.getRoot());
        Assert.assertSame(BlockNavigatorTest.rootBlock, BlockNavigatorTest.contextBlockChild1.getRoot());
        Assert.assertSame(BlockNavigatorTest.rootBlock, BlockNavigatorTest.contextBlockChild11.getRoot());
    }
}

