package presentation.formbeans.oWorking_hoursSys;




import utils.formbeans.FormBeanContainer;
import utils.formbeans.BOAdderFormBean;

import utils.validator.ValidatorFormAndAction;

import presentation.formbeans.objects.*;

/**
 * This class is a FormBeanContainer for the action action_new_new_page_working_hours.
 * 
 * @author Automatika - JustInMind SL
 */
public class Action_new_new_page_working_hours_Form extends FormBeanContainer {

	
	//FormBean for the action
			
			/**
			* nested-pojo-form-bean contained in this container
			**/
			private Working_hours_Form working_hours_Form = null;
	
	
	

	
	
	
		
		/**
		 * @return returns the code of the current working_hours_Form (a shortcut of getWorking_hours_Form().getWorking_hourscode())
		 */
		public String getWorking_hourscode() {
			
			return getWorking_hours_Form().getWorking_hourscode();
		}

		/**
		 * Sets the code of the current working_hours_Form (a shortcut of getWorking_hours_Form().setWorking_hourscode(String))
		 * @param working_hourscode the new code
		 */
		
		public void setWorking_hourscode(String working_hourscode){
			
			getWorking_hours_Form().setWorking_hourscode(working_hourscode);
		}

		/**
		 * @return the current working_hours_Form
		 */
		
		public Working_hours_Form getWorking_hours_Form() {
			
			if(working_hours_Form==null)
				
				working_hours_Form = new Working_hours_Form();
			
			return working_hours_Form;
		}

		/**
		 * set the current working_hours_Form
		 * @param working_hours_Form the new working_hours_Form
		 */
		
		public void setWorking_hours_Form(Working_hours_Form working_hours_Form){
			
			this.working_hours_Form=working_hours_Form;
		}
		
		
			/* (non-Javadoc)
			 * @see utils.formbeans.FormBeanContainer#getMainFormBeanCode()
			 */
			@Override
			public String getMainFormBeanCode() {
				return getWorking_hourscode();
			}
			
			
			
	
	
	
	
	
	
}
