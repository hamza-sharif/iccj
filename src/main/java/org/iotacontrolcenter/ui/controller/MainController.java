package org.iotacontrolcenter.ui.controller;


import org.iotacontrolcenter.ui.app.Constants;
import org.iotacontrolcenter.ui.dialog.ConfigureServerDialog;
import org.iotacontrolcenter.ui.dialog.IccSettingsDialog;
import org.iotacontrolcenter.ui.dialog.OpenServerDialog;
import org.iotacontrolcenter.ui.panel.ServerPanel;
import org.iotacontrolcenter.ui.panel.ServerTabPanel;
import org.iotacontrolcenter.ui.properties.locale.Localizer;
import org.iotacontrolcenter.ui.properties.source.PropertySource;
import org.iotacontrolcenter.ui.proxy.ServerProxy;
import org.iotacontrolcenter.ui.util.UiUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;

public class MainController implements ActionListener {

    private IccSettingsDialog iccSettingsDialog;
    private Localizer localizer;
    private ServerTabPanel serverTabPanel;
    private ConfigureServerDialog cfgServerDialog;
    private OpenServerDialog openServerDialog;
    private PropertySource propertySource = PropertySource.getInstance();

    public void init() {
        localizer = Localizer.getInstance();
    }

    public void initialPrompts() {
        Properties props = propertySource.getLocalServerProperties();

        if(props == null) {
            UiUtil.showInfoDialog(localizer.getLocalText("initialAddServerTitle"),
                    localizer.getLocalText("initialAddServerMsg"));
        }
        else {
            boolean addLocal = UiUtil.promptUserYorN(localizer.getLocalText("openServerPromptTitle"),
                    localizer.getLocalText("openServerPromptMsg") + " " +
                    props.getProperty(PropertySource.SERVER_NAME_PROP));
            if (addLocal) {
                addServerTabPanel(props);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        System.out.println(action);
        if(action.equals(Constants.MM_ADD_SERVER_ACTION)) {
            showAddOrEditServerDialog(localizer.getLocalText("dialogTitleAddServer"), null, null, true);
        }
        else if(action.equals(Constants.MM_OPEN_SERVER_ACTION)) {
            showOpenServerDialog();
        }
        else if(action.equals(Constants.MM_ICC_SETTINGS_ACTION)) {
            showIccSettingsDialog();
        }
        else if(action.equals(Constants.DIALOG_CONFIG_ADD_SERVER_SAVE)) {
            cfgServerDialogSave(action);
        }
        else if(action.equals(Constants.DIALOG_CONFIG_EDIT_SERVER_SAVE)) {
            cfgServerDialogSave(action);
        }
        else if(action.equals(Constants.DIALOG_CONFIG_SERVER_CANCEL)) {
            cfgServerDialogClose();
        }
        else if(action.equals(Constants.DIALOG_ICC_SETTINGS_CANCEL)) {
            iccSettingsDialogClose();
        }
        else if(action.equals(Constants.DIALOG_ICC_SETTINGS_SAVE)) {
            iccSettingsDialogSave();
        }
        else if(action.equals(Constants.DIALOG_OPEN_SERVER_OPEN)) {
            String serverName = openServerDialog.serverList.getSelectedValue();
            if(serverName == null || serverName.isEmpty()) {
                UiUtil.showErrorDialog(localizer.getLocalText("dialogOpenServerErrorTitle"),
                        localizer.getLocalText("dialogServerErrorUnselected"));
                return;
            }
            openSelectedServer(serverName);
        }
        else if(action.equals(Constants.DIALOG_OPEN_SERVER_EDIT)) {
            String serverName = openServerDialog.serverList.getSelectedValue();
            if(serverName == null || serverName.isEmpty()) {
                UiUtil.showErrorDialog(localizer.getLocalText("dialogEditServerErrorTitle"),
                        localizer.getLocalText("dialogServerErrorUnselected"));
                return;
            }

            openServerDialogClose();

            Properties serverProps = propertySource.getServerPropertiesForServerName(serverName);

            showAddOrEditServerDialog(localizer.getLocalText("dialogTitleEditServer"), serverName, serverProps, false);
        }
        else if(action.equals(Constants.DIALOG_OPEN_SERVER_ADD_SERVER)) {
            openServerDialogClose();
            showAddOrEditServerDialog(localizer.getLocalText("dialogTitleAddServer"), null, null, true);
        }
        else if(action.equals(Constants.DIALOG_OPEN_SERVER_REMOVE)) {
            String serverName = openServerDialog.serverList.getSelectedValue();
            if(serverName == null || serverName.isEmpty()) {
                UiUtil.showErrorDialog(localizer.getLocalText("dialogRemoveServerErrorTitle"),
                        localizer.getLocalText("dialogServerErrorUnselected"));
                return;
            }
            removeSelectedServer(serverName);
        }
        else if(action.equals(Constants.DIALOG_OPEN_SERVER_CANCEL)) {
            openServerDialogClose();
        }
    }

    public void setServerTabPanel(ServerTabPanel serverTabPanel) {
        this.serverTabPanel = serverTabPanel;
    }

    private boolean iccSettingsDialogSave() {
        if(iccSettingsDialog == null) {
            System.out.println("iccSettingsDialog not found!");

            // TODO: localization
            UiUtil.showErrorDialog("ICC Settings Save Error", "Dialog not found!");
            return false;
        }

        String nbrRefresh = iccSettingsDialog.nbrRefreshTimeTextField.getText();
        String nodeInfoRefresh = iccSettingsDialog.nodeInfoRefreshTimeTextField.getText();
        String iotaDldLink = iccSettingsDialog.iotaDownloadLinkTextField.getText();
        String iotaDldFile = iccSettingsDialog.iotaDownloadFileTextField.getText();

        String errors = "";
        String sep = "";
        boolean isError = false;
        if(nbrRefresh == null || nbrRefresh.isEmpty() || !UiUtil.isValidPositiveNumber(nbrRefresh)) {
            isError = true;
            errors += sep + localizer.getLocalText("dialogSaveErrorInvalidFieldValue") + " " +
                    iccSettingsDialog.nbrRefreshTimeTextField.getName();
            if(sep.isEmpty()) {
                sep = "\n";
            }
        }
        if(nodeInfoRefresh == null || nodeInfoRefresh.isEmpty() ||
                !UiUtil.isValidPositiveNumber(nodeInfoRefresh)) {
            isError = true;
            errors += sep + localizer.getLocalText("dialogSaveErrorInvalidFieldValue") + " " +
                    iccSettingsDialog.nodeInfoRefreshTimeTextField.getName();
            if(sep.isEmpty()) {
                sep = "\n";
            }
        }
        if(iotaDldLink == null || iotaDldLink.isEmpty()) {
            isError = true;
            errors += sep + localizer.getLocalText("dialogSaveErrorInvalidFieldValue") + " " +
                    iccSettingsDialog.iotaDownloadLinkTextField.getName();
            if(sep.isEmpty()) {
                sep = "\n";
            }
        }
        if(iotaDldFile == null || iotaDldFile.isEmpty()) {
            isError = true;
            errors += sep + localizer.getLocalText("dialogSaveErrorInvalidFieldValue") + " " +
                    iccSettingsDialog.iotaDownloadFileTextField.getName();
            if(sep.isEmpty()) {
                sep = "\n";
            }
        }

        if(isError) {
            UiUtil.showErrorDialog(localizer.getLocalText("dialogSaveIccErrorTitle"), errors);
            return false;
        }

        propertySource.setProperty(PropertySource.REFRESH_NBRS_PROP, nbrRefresh);
        propertySource.setProperty(PropertySource.REFRESH_NODEINFO_PROP, nodeInfoRefresh);
        propertySource.setProperty(PropertySource.IOTA_DLD_LINK_PROP, iotaDldLink);
        propertySource.setProperty(PropertySource.IOTA_DLD_FILENAME_PROP, iotaDldFile);

        propertySource.storeProperties();

        iccSettingsDialogClose();

        return true;
    }

    private void iccSettingsDialogClose() {
        if(iccSettingsDialog != null) {
            iccSettingsDialog.dispose();
            iccSettingsDialog = null;
        }
    }

    private void openSelectedServer(String serverName) {
        try {
            addServerTabPanel(propertySource.getServerPropertiesForServerName(serverName));
        }
        catch(IllegalStateException ise) {
            System.out.println("Ill State Exception opening server: " + ise.getLocalizedMessage());
            UiUtil.showErrorDialog(localizer.getLocalText("dialogOpenServerErrorTitle"), ise.getMessage());
            return;
        }
        openServerDialogClose();
    }

    private void removeSelectedServer(String serverName) {
        serverTabPanel.removeServerTabByName(serverName);

        openServerDialogClose();
    }

    private void showIccSettingsDialog() {
        iccSettingsDialog = new IccSettingsDialog(localizer, propertySource, this);
        iccSettingsDialog.setLocationRelativeTo(serverTabPanel);

        iccSettingsDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                iccSettingsDialog = null;
            }
        });

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                iccSettingsDialog.nbrRefreshTimeTextField.requestFocusInWindow();
            }
        });

        iccSettingsDialog.setVisible(true);
    }

    private void showAddOrEditServerDialog(String title, String serverName, Properties serverProps, boolean isAdd) {
        // TODO: localization
        if(serverName != null && serverProps == null) {
            System.out.println("server properties is null for serverName "  + serverName);
            UiUtil.showErrorDialog("Settings Error",
                    "Server properties not found!");
            return;
        }

        cfgServerDialog = new ConfigureServerDialog(localizer, title, this, serverProps, isAdd);
        cfgServerDialog.setLocationRelativeTo(serverTabPanel);

        cfgServerDialog.addWindowListener(new WindowAdapter() {
                                        @Override
                                        public void windowClosed(WindowEvent e) {
                                            super.windowClosed(e);
                                            cfgServerDialog = null;
                                        }
                                    });

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                cfgServerDialog.serverIpTextField.requestFocusInWindow();
            }
        });

        if(isAdd) {
            cfgServerDialog.iccrPortTextField.setText(propertySource.getIccrDefaultPortNumber());
            cfgServerDialog.iccrPwdTextField.setText(propertySource.getIccrDefaultApiKey());
        }

        cfgServerDialog.setVisible(true);
    }

    private void cfgServerDialogSave(String action) {
        if(cfgServerDialog == null) {
            System.out.println("cfgServerDialog not found!");

            // TODO: localization
            UiUtil.showErrorDialog(
                    action.equals(Constants.DIALOG_CONFIG_ADD_SERVER_SAVE) ? "Add Server Save Error" :
                            "Edit Server Save Error",
                    "Dialog not found!");
            return;
        }

        Properties newProps = new Properties();
        String ip = cfgServerDialog.serverIpTextField.getText();
        newProps.setProperty(PropertySource.SERVER_IP_PROP, ip);

        String port = cfgServerDialog.iccrPortTextField.getText();
        newProps.setProperty(PropertySource.SERVER_ICCR_PORT_NUM_PROP, port);

        String apiKey = cfgServerDialog.iccrPwdTextField.getText();
        newProps.setProperty(PropertySource.SERVER_ICCR_API_KEY_PROP, apiKey);

        String name = cfgServerDialog.serverNameTextField.getText();
        newProps.setProperty(PropertySource.SERVER_NAME_PROP, name);

        String walletCmd = cfgServerDialog.walletCmdTextField.getText();
        newProps.setProperty(PropertySource.SERVER_WALLET_CMD_PROP, walletCmd);

        String errors = "";
        String sep = "";
        boolean isError = false;
        if(ip == null || ip.isEmpty() || !UiUtil.isValidIpV4(ip)) {
            isError = true;
            errors += sep + localizer.getLocalText("dialogSaveErrorInvalidFieldValue") + " " +
                    cfgServerDialog.serverIpTextField.getName();
            if(sep.isEmpty()) {
                sep = "\n";
            }
        }
        if(port == null || port.isEmpty() || !UiUtil.isValidPositiveNumber(port)) {
            isError = true;
            errors += sep + localizer.getLocalText("dialogSaveErrorInvalidFieldValue") + " " +
                    cfgServerDialog.iccrPortTextField.getName();
            if(sep.isEmpty()) {
                sep = "\n";
            }
        }
        if(apiKey == null || apiKey.isEmpty()) {
            isError = true;
            errors += sep + localizer.getLocalText("dialogSaveErrorInvalidFieldValue") + " " +
                    cfgServerDialog.iccrPwdTextField.getName();
            if(sep.isEmpty()) {
                sep = "\n";
            }
        }
        if(name == null || name.isEmpty()) {
            isError = true;
            errors += sep + localizer.getLocalText("dialogSaveErrorInvalidFieldValue") + " " +
                    cfgServerDialog.serverNameTextField.getName();
            if(sep.isEmpty()) {
                sep = "\n";
            }
        }
        else if(cfgServerDialog.isAdd && propertySource.isServerNameTaken(name)) {
            isError = true;
            errors += sep + localizer.getLocalText("dialogSaveErrorInvalidFieldValue") + " " +
                    localizer.getLocalText("dialogSaveErrorServerNameTaken");
            if(sep.isEmpty()) {
                sep = "\n";
            }
        }
        if(!isError) {
            // That last line just insured that we have a valid IP
            if (UiUtil.isLocalhostIp(ip) && (walletCmd == null || walletCmd.isEmpty())) {
                isError = true;
                errors += sep + localizer.getLocalText("dialogSaveErrorInvalidFieldValue") + " " +
                        cfgServerDialog.walletCmdTextField.getName();
                if (sep.isEmpty()) {
                    sep = "\n";
                }
            }
        }
        if(isError) {
            UiUtil.showErrorDialog(
                    action.equals(Constants.DIALOG_CONFIG_ADD_SERVER_SAVE) ? "Add Server Save Error" :
                            "Edit Server Save Error",
                    errors);
            return;
        }

        boolean apiKeyChange = false;
        if(cfgServerDialog.isAdd) {
            newProps.setProperty(PropertySource.SERVER_ID_PROP, UiUtil.genServerId(name));
        }
        else {
            newProps.setProperty(PropertySource.SERVER_ID_PROP, UiUtil.genServerId(name));
            apiKeyChange = !newProps.getProperty(PropertySource.SERVER_ICCR_API_KEY_PROP).equals(
                    cfgServerDialog.serverProps.getProperty(PropertySource.SERVER_ICCR_API_KEY_PROP));

            System.out.println("server configuration edit: apiKeyChange: " + apiKeyChange);
        }

        persistCfgServerSettings(newProps, cfgServerDialog.serverProps, cfgServerDialog.isAdd);
        
        if(!cfgServerDialog.isAdd && propertySource.isServerNameChange(newProps, cfgServerDialog.serverProps)) {
            serverTabPanel.serverNameChange(cfgServerDialog.serverProps.getProperty(PropertySource.SERVER_NAME_PROP), name);
        }

        if(apiKeyChange) {
            serverTabPanel.serverApiKeyChange(
                    cfgServerDialog.serverProps.getProperty(PropertySource.SERVER_NAME_PROP),
                    newProps.getProperty(PropertySource.SERVER_ICCR_API_KEY_PROP));
        }

        cfgServerDialogClose();
    }

    private void persistCfgServerSettings(Properties newProps, Properties prevProps, boolean isAdd) {
        if(isAdd) {
            propertySource.addServerProperties(newProps);
        }
        else {
            propertySource.setServerProperties(newProps, prevProps);
        }
    }

    private void openServerDialogClose() {
        if(openServerDialog != null) {
            openServerDialog.dispose();
            openServerDialog = null;
        }
    }

    private void cfgServerDialogClose() {
        if(cfgServerDialog != null) {
            cfgServerDialog.dispose();
            cfgServerDialog = null;
        }
    }

    private void showOpenServerDialog() {
        openServerDialog = new OpenServerDialog(localizer,
                localizer.getLocalText("dialogTitleOpenServer"),
                this);
        openServerDialog.setLocationRelativeTo(serverTabPanel);

        openServerDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                openServerDialog = null;
            }
        });

        openServerDialog.setVisible(true);
    }

    private void addServerTabPanel(Properties serverProps) {
        String name = serverProps.getProperty(PropertySource.SERVER_NAME_PROP);
        if(serverTabPanel.serverIsOpen(name)) {
            throw new IllegalStateException(localizer.getLocalText("dialogServerErrorAlreadyOpen"));
        }
        ServerProxy proxy = new ServerProxy(serverProps);
        ServerController ctlr = new ServerController(localizer, proxy, serverProps);

        ServerPanel server = new ServerPanel(serverProps.getProperty(PropertySource.SERVER_ID_PROP), localizer, ctlr);
        ctlr.setServerPanel(server);
        serverTabPanel.add(name, server);

        SwingUtilities.invokeLater(() -> {
            ctlr.serverSetup();
        });
    }
}
