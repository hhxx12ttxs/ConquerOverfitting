package com.bagatelle.zplanner.workitem.story;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.bagatelle.zplanner.TestData;
import com.bagatelle.zplanner.account.UserAccount;
import com.bagatelle.zplanner.core.HibernateUtil;
import com.bagatelle.zplanner.core.HibernateUtil.HibernateConfig;
import com.bagatelle.zplanner.estimate.EstimateUpdate;
import com.bagatelle.zplanner.workitem.WorkItemManager;
import com.bagatelle.zplanner.workitem.project.Project;
import com.bagatelle.zplanner.workitem.task.Task;


public class StoryTest {
	Story story = null;
	Session session = null;
	double sumEstimate = 0.0;
	WorkItemManager workItemMgr = new WorkItemManager();
	
	@Before
	public void setup() {
		session = HibernateUtil.openSession(HibernateConfig.TEST_CONFIG);
		
		//create a single story with three tasks and three subtasks.
		//each subtask has estimate of 5 hours, so total is 45
		story = TestData.createStoryAndTasks(); 
		sumEstimate = 45.0; //hard-coded for purpose of tests
	}
	
	@After
	public void teardown() {
		HibernateUtil.closeSession(session);
	}
	
	@Test
	public void testCalcEstimate() {
		Long storyId = workItemMgr.save(session, story);
		Assert.assertNotNull(storyId);
		
		Story retrievedStory = workItemMgr.loadStory(session, storyId);
		
		Assert.assertEquals("Estimate is correct", sumEstimate, retrievedStory.getEstimate(), 0.0);	
	}	
	
	@Test
	public void testSaveAndUpdateStory() {
		Long storyId = workItemMgr.save(session, story);
		Assert.assertNotNull(storyId);

		//reload our story from teh DB and make sure it's what we expect
		Story retrievedStory = workItemMgr.loadStory(session, storyId);
		Assert.assertEquals("Story name is correct", TestData.STORY_NAME, retrievedStory.getName());
		Assert.assertEquals("Story description is correct", TestData.STORY_DESC, retrievedStory.getDescription());
		Assert.assertEquals("Story estimate is correct", sumEstimate, retrievedStory.getEstimate(), 0.0);
		Assert.assertEquals("Story task name is correct", TestData.TASK_NAME, retrievedStory.getTasks().get(0).getName());
		
		//now update the story...
		retrievedStory.setName(TestData.UPDATED_STORY_NAME);
		workItemMgr.save(session, retrievedStory);

		//reload the story again
		retrievedStory = workItemMgr.loadStory(session, storyId);
		Assert.assertEquals("Name got updated", TestData.UPDATED_STORY_NAME, retrievedStory.getName());
		
		//Former bug:  delete a subitem and make sure the parent doesn't get deleted
		//FIXME:  move this test to WorkItemTest???
		Task subItemToDelete = retrievedStory.getTasks().get(0);
		Long subItemToDeleteId = subItemToDelete.getId();
		Double subItemEst = subItemToDelete.getEstimate(); //need to make sure parent est is updated
		
		workItemMgr.delete(session, subItemToDelete);

		//test that parent estimate was updated correctly
		Assert.assertEquals(sumEstimate - subItemEst, retrievedStory.getEstimate(), 0.0);
		
		//should get null if we try to load deleted item
		Task reloadedDeletedItem = workItemMgr.loadTask(session, subItemToDeleteId);
		Assert.assertNull(reloadedDeletedItem);
		
		//retrieve the parent again and make sure it's still there
		retrievedStory = workItemMgr.loadStory(session, storyId);
		Assert.assertNotNull(retrievedStory);
		
		//now test delete of parent story
		//Firt, get a list of subids to ensure cascade of delete worked
		ArrayList<Long> subItemIds = new ArrayList<Long>();
		for(Task task : retrievedStory.getTasks()) {
			subItemIds.add(task.getId());
		}
		
		//do the delete
		workItemMgr.delete(session, retrievedStory);
		
		//test that if we try to reload the story isn't there anymore
		Story storyAfterDelete = workItemMgr.loadStory(session, storyId);
		Assert.assertNull(storyAfterDelete);
	
		//make sure all the subitems were deleted too
		Assert.assertTrue(subItemIds.size() > 0);
		for(Long id : subItemIds) {
			Task subItem = workItemMgr.loadTask(session, id);
			Assert.assertNull(subItem);
		}
		
	}
	
	
	@Test
	public void testUpdateEstimate() {
		//create a first story
		Story storyToUpdate = new Story(TestData.STORY_NAME, TestData.STORY_DESC, 
				TestData.ESTIMATE);
		Long storyId = workItemMgr.save(session, storyToUpdate);
		Assert.assertNotNull(storyId);
		
		//check that it has no updates
		Story retrievedStory = workItemMgr.loadStory(session, storyId);
		List<EstimateUpdate> estTxs = retrievedStory.getEstimateUpdates();
		Assert.assertEquals(1, estTxs.size());
		Assert.assertEquals(TestData.ESTIMATE, retrievedStory.getEstimate(), 0);
		
		//now add a new sub-task
		Task subTask = new Task(TestData.STORY_NAME, TestData.STORY_DESC, 
				TestData.ESTIMATE);
		retrievedStory.addTask(subTask);
		workItemMgr.save(session, retrievedStory);
		
		//we should have only 1 update (the one when the estimate was created) since although we added a task, 
		//it's estimate was the same as the original story, so no change
		estTxs = retrievedStory.getEstimateUpdates(); 
		Assert.assertEquals(1, estTxs.size());
		Assert.assertEquals(TestData.ESTIMATE, retrievedStory.getEstimate(), 0);
		
		//add another task and test that we have a history of one update and sumEstimate of 2*estimate
		estTxs = addNewTask(retrievedStory);
		Assert.assertEquals(2, estTxs.size()); 
		Assert.assertEquals(TestData.ESTIMATE*2, retrievedStory.getEstimate(), 0);
		
		//now remove a subnode which should make the sumEstimate the same as it was
		//initially
		workItemMgr.delete(session, retrievedStory.getTasks().get(0));
		estTxs = retrievedStory.getEstimateUpdates();
		Assert.assertEquals(3, estTxs.size());  //we've now made three updates to the est
		Assert.assertEquals(TestData.ESTIMATE, retrievedStory.getEstimate(), 0);
	}
	
	//helper method for testing cases in which we wnt to add a subtask to exercise functionality
	private List<EstimateUpdate> addNewTask(Story theStory) {
		Task subTask = new Task(TestData.STORY_NAME, TestData.STORY_DESC, 
				TestData.ESTIMATE);
		theStory.addTask(subTask);
		workItemMgr.save(session, theStory);
		return theStory.getEstimateUpdates();
	}
	
	@Test
	public void testMoveItem() {
		Long storyOneId = workItemMgr.save(session, story);
		Assert.assertNotNull(storyOneId);
		
		Story storyTwo = TestData.createStoryAndTasks();
		Long storyTwoId = workItemMgr.save(session, storyTwo);
		Assert.assertNotNull(storyTwoId);
		
		Assert.assertNotSame(storyTwo, story);
		
		//grab the initial number of tasks for each story, though both should just be 3
		int initialNumStoryOneTasks = story.getTasks().size();
		int initialNumStoryTwoTasks = storyTwo.getTasks().size();

		Task storyOneTask = story.getTasks().get(0);
		storyTwo.addTask(storyOneTask);
		
		int finalNumStoryOneTasks = story.getTasks().size();
		Assert.assertEquals(initialNumStoryOneTasks-1, finalNumStoryOneTasks);
		
		int finalNumStoryTwoTasks = storyTwo.getTasks().size();
		Assert.assertEquals(initialNumStoryTwoTasks+1, finalNumStoryTwoTasks);
	
		Assert.assertEquals("Task now has storytwo as parent", storyTwo, storyOneTask.getStory());
	}
	
	/**
	 * Try deleting a story which has an owner.  This currently causes an exception when hibernate
	 * tries to resave the user
	 * 
	 */
	@Test
	public void testAssignUser() {
		UserAccount user = TestData.createUser();
		user.save(session);
	
		story.setOwner(user);
		Long storyOneId = workItemMgr.save(session, story);
		Assert.assertNotNull(storyOneId);
	
		Story story2 = TestData.createStory();
		story2.setOwner(user);
		Long storyTwoId = workItemMgr.save(session, story2);
		Assert.assertNotNull(storyTwoId);
		
		workItemMgr.delete(session, story2);
	}
	
	@Test
	public void testCreateStoryFromTask() {
		Project project = TestData.createFullProject();
		
		//make one of the tasks into another story instead
		Story parentStory = project.getIterations().get(0).getStories().get(0); 
		Task task = parentStory.getTasks().get(0);
		//task.detachFromParent();
		
		Story newStory = new Story(task);
	}
	
	/*
	@Test
	public void testLogTimeAgainstStory() {
		Double workDone = 5.0;
		Double estimate = story.getEstimate();
		
		//log work directly against story
		story.logWork(workDone);
		Assert.assertEquals(estimate-workDone, story.getRemainingWork(),0);
	
		//log time against the child task and see that it rolls up to story
		Task childTask = story.getTasks().get(0);
		childTask.logWork(workDone);
		Assert.assertEquals(estimate-workDone*2,story.getRemainingWork(), 0);
	}*/
}

