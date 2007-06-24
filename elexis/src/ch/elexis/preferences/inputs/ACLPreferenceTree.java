/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: ACLPreferenceTree.java 1859 2007-02-20 09:52:42Z rgw_ch $
 *******************************************************************************/
package ch.elexis.preferences.inputs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.Hub;
import ch.elexis.data.Anwender;
import ch.elexis.util.SWTHelper;
import ch.elexis.util.Tree;
import ch.rgw.tools.StringTool;

public class ACLPreferenceTree extends Composite {
	Tree<String> acls;
	//String[] acls;
	TreeViewer tv;
	org.eclipse.swt.widgets.List lbGroups;
	org.eclipse.swt.widgets.List lbUsers;
	List<Anwender> lUsers;
	
	public ACLPreferenceTree(Composite parent, String...a ){
		super(parent,SWT.NONE);
		acls=new Tree<String>(null,null);
		for(String s:a){
			String[] path=s.split("/");
			Tree<String> t=acls;
			for(String p:path){
				Tree<String> ch=t.find(p, false);
				if(ch==null){
					ch=new Tree<String>(t,p);
				}
				t=ch;
			}
		}
		
		setLayout(new GridLayout());
		setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tv=new TreeViewer(this);
		tv.setContentProvider(new ITreeContentProvider(){

			public Object[] getChildren(Object parentElement) {
				Tree tree=(Tree)parentElement;
				return tree.getChildren().toArray();
			}

			public Object getParent(Object element) {
				return ((Tree)element).getParent();
			}

			public boolean hasChildren(Object element) {
				Tree tree=(Tree)element;
				return tree.hasChildren();
			}

			public Object[] getElements(Object inputElement) {
				return acls.getChildren().toArray();
			}

			public void dispose() {
				// TODO Auto-generated method stub
				
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// TODO Auto-generated method stub
				
			}});
		tv.setLabelProvider(new LabelProvider(){

			@Override
			public String getText(Object element) {
				return (String)((Tree)element).contents;
			}
			
		});
		tv.getControl().setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		Composite cBottom=new Composite(this,SWT.NONE);
		cBottom.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		cBottom.setLayout(new GridLayout(2,true));
		new Label(cBottom,SWT.NONE).setText("Gruppen");
		new Label(cBottom,SWT.NONE).setText("Anwender");
		lbGroups=new org.eclipse.swt.widgets.List(cBottom,SWT.MULTI|SWT.V_SCROLL);
		lbUsers=new org.eclipse.swt.widgets.List(cBottom,SWT.MULTI|SWT.V_SCROLL);
		lbUsers.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		lbGroups.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		lUsers=Hub.getUserList();
		for(Anwender an:lUsers){
			lbUsers.add(an.getLabel());
		}
		List<String> lGroups=Hub.acl.getGroups();
		for(String s:lGroups){
			lbGroups.add(s);
		}
		tv.addSelectionChangedListener(new ISelectionChangedListener(){

			/**
			 * if the user selects an ACL from the TreeViewer, we want to select users and groups
			 * that are granted this acl in the lbGroups and lbUsers ListBoxes
			 */
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel=(IStructuredSelection)event.getSelection();
				lbGroups.deselectAll();	
				lbUsers.deselectAll();
				if(!sel.isEmpty()){
					Tree acl=(Tree)sel.getFirstElement();
					String right=getAclName(acl);
					List<String> grps=Hub.acl.groupsForGrant(right);
					List<Anwender> users=Hub.acl.usersForGrant(right);
					for(String g:grps){
						int idx=StringTool.getIndex(lbGroups.getItems(),g);
						if(idx!=-1){
							lbGroups.select(idx);
						}
					}
					for(Anwender an:users){
						int idx=StringTool.getIndex(lbUsers.getItems(), an.getLabel());
						if(idx!=-1){
							lbUsers.select(idx);
						}
					}
				}
				
			}
			
		});
		lbGroups.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent arg0) {
				IStructuredSelection sel=(IStructuredSelection)tv.getSelection();
				if(!sel.isEmpty()){
					Tree acl=(Tree)sel.getFirstElement();
					String right=getAclName(acl);
					String[] gsel=lbGroups.getSelection();
					for(String g:lbGroups.getItems()){
						Hub.acl.revoke(g, right);
					}
					for(String g:gsel){
						Hub.acl.grant(g, right);
					}
				}
			}
			
		});
		lbUsers.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent arg0) {
				IStructuredSelection sel=(IStructuredSelection)tv.getSelection();
				if(!sel.isEmpty()){
					Tree acl=(Tree)sel.getFirstElement();
					String right=getAclName(acl);
					int[] uSel=lbUsers.getSelectionIndices();
					for(Anwender an:lUsers){
						Hub.acl.revoke(an, right);
					}
					for(int i:uSel){
						Hub.acl.grant(lUsers.get(i), right);
					}
				}
			}
		});
		tv.setInput(this);
		
	}

	private String getAclName(Tree tree){
		StringBuilder sb=new StringBuilder();
		sb.append((String)tree.contents);
		while(!(tree=tree.getParent()).equals(acls)){
			sb.insert(0, "/");	
			sb.insert(0, (String)tree.contents);
		}
		return sb.toString();
	}
	
	public void reload(){
		
	}
	public void flush(){
		Hub.acl.flush();
	}
}
