package fr.voxeet.sdk.sample.main_screen;


import androidx.annotation.DrawableRes;

import com.voxeet.sdk.json.ParticipantInfo;

public class ParticipantItem {

    @DrawableRes
    private int _drawable;

    private ParticipantInfo _user_info;
    private boolean _selected;

    private ParticipantItem() {
    }

    public ParticipantItem(@DrawableRes int drawable, ParticipantInfo user_info) {
        this();

        _drawable = drawable;
        _user_info = user_info;
        _selected = false;
    }

    public int getDrawable() {
        return _drawable;
    }

    public ParticipantInfo getParticipantInfo() {
        return _user_info;
    }

    public void setSelected(boolean selected) {
        _selected = selected;
    }

    public boolean isSelected() {
        return _selected;
    }
}
