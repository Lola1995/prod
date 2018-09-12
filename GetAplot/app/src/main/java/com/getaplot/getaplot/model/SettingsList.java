package com.getaplot.getaplot.model;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Elia on 9/27/2017.
 */

public class SettingsList {
    String settingText;
    private CircleImageView icon;

    public SettingsList() {
    }

    public SettingsList(CircleImageView icon, String settingText) {
        this.icon = icon;
        this.settingText = settingText;
    }

    public CircleImageView getIcon() {
        return icon;
    }

    public void setIcon(CircleImageView icon) {
        this.icon = icon;
    }

    public String getSettingText() {
        return settingText;
    }

    public void setSettingText(String settingText) {
        this.settingText = settingText;
    }
}