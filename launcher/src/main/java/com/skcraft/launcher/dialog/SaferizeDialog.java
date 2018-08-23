/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.saferize.sdk.SaferizeClient;
import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.auth.AccountList;
import com.skcraft.launcher.swing.FormPanel;
import com.skcraft.launcher.swing.LinedBoxPanel;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.SharedLocale;
import com.skcraft.launcher.util.SwingExecutor;

import lombok.Getter;
import lombok.NonNull;

/**
 * The login dialog.
 */
public class SaferizeDialog extends JDialog {

    private final Launcher launcher;
    @Getter private final AccountList accounts;
    @Getter private String parentEmail = null;
    

    private final JTextField idParentEmail = new JTextField();
    private final JButton nextButton = new JButton(SharedLocale.tr("login.next"));
    private final JButton cancelButton = new JButton(SharedLocale.tr("button.cancel"));
    private final FormPanel formPanel = new FormPanel();
    private final JPanel outerPanel = new JPanel();
    private final LinedBoxPanel buttonsPanel = new LinedBoxPanel(true);


    

    /**
     * Create a new login dialog.
     *
     * @param owner the owner
     * @param launcher the launcher
     */
    public SaferizeDialog(Window owner, @NonNull Launcher launcher) {
        super(owner, ModalityType.DOCUMENT_MODAL);

        this.launcher = launcher;
        this.accounts = launcher.getAccounts();

        setTitle(SharedLocale.tr("login.title"));
        initComponents();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(420, 0));
        setResizable(false);
        pack();
        setLocationRelativeTo(owner);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                dispose();
            }
        });
    }

    
    
    private void initComponents() {

        
        

        nextButton.setFont(nextButton.getFont().deriveFont(Font.BOLD));
        
        add(outerPanel, BorderLayout.CENTER);
        GridBagLayout layout = new GridBagLayout();        
        outerPanel.setLayout(layout);
        JLabel stepsLabel = new JLabel("Step 1 of 2");
        stepsLabel.setFont(stepsLabel.getFont().deriveFont(Font.BOLD));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;        
        c.insets = new Insets(10, 0, 10, 0);
        outerPanel.add(stepsLabel, c);
        
        JLabel createParentLabel = new JLabel("Create your parent account");
        createParentLabel.setFont(createParentLabel.getFont().deriveFont(Font.BOLD));
        c = new GridBagConstraints();
        c.gridy = 1;                        
        c.insets = new Insets(2, 12, 2, 2);        
        c.anchor = GridBagConstraints.WEST;
        
        outerPanel.add(createParentLabel, c);
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;        
        outerPanel.add(formPanel, c);
        
        
        
        formPanel.addRow(new JLabel(SharedLocale.tr("login.parentEmail")), idParentEmail);
        
        
        if (launcher.getSaferizeToken() != null && launcher.getSaferizeToken().getParentEmail() != null) {
        	idParentEmail.setText(launcher.getSaferizeToken().getParentEmail());
        	if (launcher.getSaferizeToken().getUserToken() != null) {
        		idParentEmail.setEnabled(false);
        	}
        }
        
        
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(26, 13, 13, 13));

        buttonsPanel.addGlue();
        buttonsPanel.addElement(nextButton);
        buttonsPanel.addElement(cancelButton);
        
        //add(formPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(nextButton);


        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prepareLogin();
            }
        });


        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

    }



    private void prepareLogin() {
    	String email = idParentEmail.getText();
    	if (email == null || email.isEmpty()) {
    		SwingHelper.showErrorDialog(this, "Email cannot be blank", "Email Error");
    		return;
    	} 
    	
 		if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[0-9a-zA-Z]+$")) {
    		SwingHelper.showErrorDialog(this, "Email is invalid", "Email Error");
    		return;
		}    	 
         attemptSaferize(idParentEmail.getText());
    }

    
    private void attemptSaferize(String parentEmail) {
    	
        SaferizeCallable callable = new SaferizeCallable(parentEmail);        
        
        ObservableFuture<String> future = new ObservableFuture<String>(launcher.getExecutor().submit(callable), callable);
        
        Futures.addCallback(future, new FutureCallback<String>() {
            @Override
            public void onSuccess(String result) {            	
               setResult(result);            	
            }

            @Override
            public void onFailure(Throwable t) {
            }
        }, SwingExecutor.INSTANCE);        
        ProgressDialog.showProgress(this, future, SharedLocale.tr("login.saferizeCreating"), SharedLocale.tr("login.saferizeCreatingStatus"));
        SwingHelper.addErrorDialogCallback(this, future);
    }

    private void setResult(String parentEmail) {
    	this.parentEmail = parentEmail;
        dispose();
    }

    public static String showLoginRequest(Window owner, Launcher launcher) {
        SaferizeDialog dialog = new SaferizeDialog(owner, launcher);
        dialog.setVisible(true);
        return dialog.getParentEmail();
    }


    
    private class SaferizeCallable implements Callable<String>,ProgressObservable {

    	private String parentEmail;
    	
    	public SaferizeCallable(String parentEmail) {
			this.parentEmail = parentEmail;
			
		}
    	
		@Override
		public double getProgress() {
			return -1;
		}

		@Override
		public String getStatus() {
			return SharedLocale.tr("login.loggingInStatus");
		}

		@Override
		public String call() throws Exception {
			Properties prop = launcher.getProperties();
			com.saferize.sdk.Configuration saferizeConfig = new com.saferize.sdk.Configuration(new URI(prop.getProperty("saferizeUrl")), new URI(prop.getProperty("saferizeWebsocketUrl")), prop.getProperty("saferizeAccessKey"), prop.getProperty("saferizePrivateKey"));
			SaferizeClient saferizeClient = new SaferizeClient(saferizeConfig);
			saferizeClient.createProspect(parentEmail);
			return parentEmail;
		}
    	
    }

}
