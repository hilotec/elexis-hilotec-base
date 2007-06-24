/*******************************************************************************
 * Copyright (c) 2005-2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 * $Id: ApplicationActionBarAdvisor.java 2404 2007-05-20 13:39:30Z rgw_ch $
 *******************************************************************************/

package ch.elexis;

import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

import ch.elexis.actions.GlobalActions;
import static ch.elexis.actions.GlobalActions.*;
import ch.elexis.preferences.PreferenceConstants;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

/**
 * Konstruktion der globalen Aktionen (Menu, Toolbar etc.)
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {
	public static final String IMPORTER_GROUP="elexis.FileImports";
	public static final String ADDITIONS="elexis.fileAdditions";
	
	// Actions - important to allocate these only in makeActions, and then use
	// them
	// in the fill methods. This ensures that the actions aren't recreated
	// when fillActionBars is called with FILL_PROXY.
	
	private IWorkbenchWindow window;
	private IAction[] openPerspectiveActions = null;
	public static MenuManager fileMenu, editMenu,windowMenu,helpMenu;
	
	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	protected void makeActions(final IWorkbenchWindow win) {
		// Creates the actions and registers them.
		// Registering is needed to ensure that key bindings work.
		// The corresponding commands keybindings are defined in the plugin.xml
		// file.
		// Registering also provides automatic disposal of the actions when
		// the window is closed.
		window=win;
		Hub.mainActions=new GlobalActions(window);
		register(GlobalActions.exitAction);
		//register(GlobalActions.updateAction);
		/*
		register(GlobalActions.newWindowAction);
        register(GlobalActions.copyAction);
        register(GlobalActions.cutAction);
        register(GlobalActions.pasteAction);
        register(GlobalActions.loginAction);
        register(GlobalActions.importAction);
        register(GlobalActions.aboutAction);
        register(GlobalActions.helpAction);
        register(GlobalActions.prefsAction);
        register(GlobalActions.connectWizardAction);
        */
        //register(GlobalActions.changeMandantAction);
        //register(GlobalActions.savePerspectiveAction);
        //register(GlobalActions.savePerspectiveAsAction);
        //register(GlobalActions.resetPerspectiveAction);
        //register(savePerspectiveAsDefaultAction);
        //register(MainMenuActions.showViewAction);
        //register(MainMenuActions.showPerspectiveAction);

        // create open perspective actions according to the list of Sidebar
        if(Hub.localCfg.get(PreferenceConstants.SHOWTOOLBARITEMS,"true").equalsIgnoreCase("true")){
			String sbdef=Hub.localCfg.get(PreferenceConstants.SIDEBAR,""); //$NON-NLS-1$
			String[] pers=sbdef.split(","); //$NON-NLS-1$
			openPerspectiveActions = new IAction[pers.length];
			int i = 0;
			for(String per:pers){
				if (!StringTool.isNothing(per)) {
					String[] def=per.split(":"); //$NON-NLS-1$
					String perspectiveId = def[1];
				
					IPerspectiveDescriptor perspectiveDescriptor = PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(perspectiveId);
					if (perspectiveDescriptor != null) {
						openPerspectiveActions[i] = new OpenPerspectiveAction(perspectiveDescriptor);
					}
				}
	
				i++;
			}
        }
	}

	protected void fillMenuBar(IMenuManager menuBar) {
		 
		fileMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_3,IWorkbenchActionConstants.M_FILE);
		editMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_4,IWorkbenchActionConstants.M_EDIT);
		windowMenu= new MenuManager(Messages.ApplicationActionBarAdvisor_5,IWorkbenchActionConstants.M_WINDOW);
		helpMenu= new MenuManager(Messages.ApplicationActionBarAdvisor_6,IWorkbenchActionConstants.M_HELP);
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(windowMenu);
		menuBar.add(helpMenu);
		
		fileMenu.add(GlobalActions.loginAction);
		fileMenu.add(GlobalActions.changeMandantAction);
		fileMenu.add(GlobalActions.connectWizardAction);
		fileMenu.add(GlobalActions.prefsAction);
		fileMenu.add(new Separator());
		fileMenu.add(GlobalActions.importAction);
		fileMenu.add(new GroupMarker(IMPORTER_GROUP));
		fileMenu.add(new Separator());
		//fileMenu.add(GlobalActions.updateAction);
		fileMenu.add(new GroupMarker(ADDITIONS));
		fileMenu.add(new Separator());
		fileMenu.add(GlobalActions.exitAction);
		
		editMenu.add(GlobalActions.copyAction);
		editMenu.add(GlobalActions.cutAction);
		editMenu.add(GlobalActions.pasteAction);
		
		//windowMenu.add(MainMenuActions.newWindowAction);
		/*
		windowMenu.add(GlobalActions.savePerspectiveAction);
		windowMenu.add(GlobalActions.savePerspectiveAsAction);
		windowMenu.add(GlobalActions.resetPerspectiveAction);
		*/
		windowMenu.add(GlobalActions.fixLayoutAction);
		GlobalActions.perspectiveMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_7, "openPerspective"); //$NON-NLS-2$
    	GlobalActions.perspectiveList = ContributionItemFactory.PERSPECTIVES_SHORTLIST.create(window);
    	GlobalActions.perspectiveMenu.add(savePerspectiveAction);
    	GlobalActions.perspectiveMenu.add(savePerspectiveAsAction);
    	perspectiveMenu.add(resetPerspectiveAction);
    	perspectiveMenu.add(savePerspectiveAsDefaultAction);
    	perspectiveMenu.add(new Separator());
        GlobalActions.perspectiveMenu.add(GlobalActions.perspectiveList);
		windowMenu.add(GlobalActions.perspectiveMenu);
        
		GlobalActions.viewMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_9);
		GlobalActions.viewList = ContributionItemFactory.VIEWS_SHORTLIST.create(window);
		GlobalActions.viewMenu.add(GlobalActions.viewList);
		windowMenu.add(GlobalActions.viewMenu);
		
		/*helpMenu.add(testAction);*/
		helpMenu.add(GlobalActions.helpAction);
		helpMenu.add(new Separator());
		helpMenu.add(GlobalActions.aboutAction);
	}


    /* (non-Javadoc)
     * @see org.eclipse.ui.application.ActionBarAdvisor#fillCoolBar(org.eclipse.jface.action.ICoolBarManager)
     */
    protected void fillCoolBar(ICoolBarManager coolBar)
    {
        ToolBarContributionItem ci=new ToolBarContributionItem();
        ci.getToolBarManager().add(GlobalActions.homeAction);
        //ci.getToolBarManager().add(GlobalActions.resetPerspectiveAction);
        //ci.getToolBarManager().add(GlobalActions.savePerspectiveAction);
        ci.getToolBarManager().add(new Separator());
        ci.getToolBarManager().add(GlobalActions.printEtikette);
        ci.getToolBarManager().add(GlobalActions.printVersionedEtikette);
        ci.getToolBarManager().add(GlobalActions.printAdresse);
    	coolBar.add(ci);
        //coolBar.add(MainMenuActions.exitAction);
        //coolBar.add(newAction);
    	
    	// add actions for opening perspectives
    	if(Hub.localCfg.get(PreferenceConstants.SHOWTOOLBARITEMS,"true").equalsIgnoreCase("true")){
	        ci.getToolBarManager().add(new Separator());
	    	for (IAction action:openPerspectiveActions) {
	        	if (action != null) {
	        		ci.getToolBarManager().add(action);
	        	}
	    	}
    	}
    }

    /**
     * Action for opening a perspective
     * 
     * @author danlutz
     */
    class OpenPerspectiveAction extends Action {
    	private IPerspectiveDescriptor perspectiveDescriptor;
    	
    	/**
    	 * Create a new action for opening a perspective
    	 * @param perspectiveDescriptor the perspective to be opened
    	 */
    	OpenPerspectiveAction(IPerspectiveDescriptor perspectiveDescriptor) {
    		super(perspectiveDescriptor.getLabel());
    		
			setId(perspectiveDescriptor.getId());
			//setActionDefinitionId(Hub.COMMAND_PREFIX
			//		+ perspectiveDescriptor.getId());
			setToolTipText(perspectiveDescriptor.getLabel() + Messages.ApplicationActionBarAdvisor_10);
			setImageDescriptor(perspectiveDescriptor.getImageDescriptor());
			//setImageDescriptor(Hub.getImageDescriptor(perspectiveDescriptor.getImageDescriptor().));

    		this.perspectiveDescriptor = perspectiveDescriptor;
    	}
    	
		public void run() {
			try{
				IWorkbenchWindow win=PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				PlatformUI.getWorkbench().showPerspective(perspectiveDescriptor.getId(),win);
			}catch(Exception ex){
				ExHandler.handle(ex);
			}
		}
    }
}
